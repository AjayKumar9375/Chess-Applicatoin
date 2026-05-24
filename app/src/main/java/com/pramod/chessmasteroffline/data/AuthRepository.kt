package com.pramod.chessmasteroffline.data

import android.content.Context
import android.util.Base64
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.pramod.chessmasteroffline.R
import java.security.SecureRandom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authPreferences by preferencesDataStore(name = "chess_master_auth")

class AuthRepository(
    private val appContext: Context,
) {
    private object Keys {
        val id = stringPreferencesKey("id")
        val displayName = stringPreferencesKey("display_name")
        val email = stringPreferencesKey("email")
        val photoUrl = stringPreferencesKey("photo_url")
    }

    val userProfile: Flow<UserProfile?> = appContext.authPreferences.data.map { preferences ->
        val id = preferences[Keys.id].orEmpty()
        val email = preferences[Keys.email].orEmpty()
        if (id.isBlank() || email.isBlank()) {
            null
        } else {
            UserProfile(
                id = id,
                displayName = preferences[Keys.displayName].orEmpty().ifBlank { email.substringBefore("@") },
                email = email,
                photoUrl = preferences[Keys.photoUrl],
            )
        }
    }

    suspend fun signInWithGoogle(activityContext: Context): Result<UserProfile> {
        val clientId = appContext.getString(R.string.google_web_client_id)
        if (!clientId.endsWith(".apps.googleusercontent.com")) {
            return Result.failure(
                IllegalStateException(
                    "Google Sign-In needs GOOGLE_WEB_CLIENT_ID in gradle.properties before it can authenticate.",
                ),
            )
        }

        val credentialManager = CredentialManager.create(activityContext)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId)
            .setNonce(generateSecureRandomNonce())
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        return try {
            val response = credentialManager.getCredential(
                context = activityContext,
                request = request,
            )
            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val profile = UserProfile(
                    id = googleCredential.id,
                    displayName = googleCredential.displayName.orEmpty().ifBlank { googleCredential.id.substringBefore("@") },
                    email = googleCredential.id,
                    photoUrl = googleCredential.profilePictureUri?.toString(),
                )
                save(profile)
                Result.success(profile)
            } else {
                Result.failure(IllegalStateException("Unexpected Google credential response."))
            }
        } catch (exception: GetCredentialCancellationException) {
            Result.failure(IllegalStateException("Google sign-in was canceled."))
        } catch (exception: GoogleIdTokenParsingException) {
            Result.failure(IllegalStateException("Google returned an invalid identity token."))
        } catch (exception: GetCredentialException) {
            Result.failure(IllegalStateException(exception.message ?: "Google sign-in failed."))
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun signOut(activityContext: Context) {
        runCatching {
            CredentialManager.create(activityContext).clearCredentialState(ClearCredentialStateRequest())
        }
        appContext.authPreferences.edit { it.clear() }
    }

    private suspend fun save(profile: UserProfile) {
        appContext.authPreferences.edit { preferences ->
            preferences[Keys.id] = profile.id
            preferences[Keys.displayName] = profile.displayName
            preferences[Keys.email] = profile.email
            profile.photoUrl?.let { preferences[Keys.photoUrl] = it }
        }
    }

    private fun generateSecureRandomNonce(byteLength: Int = 32): String {
        val randomBytes = ByteArray(byteLength)
        SecureRandom().nextBytes(randomBytes)
        return Base64.encodeToString(
            randomBytes,
            Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING,
        )
    }
}
