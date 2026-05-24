package com.pramod.chessmasteroffline.data

data class UserProfile(
    val id: String,
    val displayName: String,
    val email: String,
    val photoUrl: String?,
) {
    val initials: String
        get() {
            val source = displayName.ifBlank { email }
            val parts = source
                .replace("@", " ")
                .replace(".", " ")
                .split(" ")
                .filter { it.isNotBlank() }
            return parts
                .take(2)
                .joinToString("") { it.first().uppercaseChar().toString() }
                .ifBlank { "GM" }
        }
}
