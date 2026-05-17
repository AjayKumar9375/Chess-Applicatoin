package com.pramod.chessmasteroffline.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appPreferences by preferencesDataStore(name = "chess_master_preferences")

class SettingsRepository(
    private val context: Context,
) {
    private object Keys {
        val boardTheme = stringPreferencesKey("board_theme")
        val pieceStyle = stringPreferencesKey("piece_style")
        val soundEnabled = booleanPreferencesKey("sound_enabled")
        val aiDifficulty = stringPreferencesKey("ai_difficulty")
    }

    val settings: Flow<AppSettings> = context.appPreferences.data.map { preferences ->
        AppSettings(
            boardTheme = preferences[Keys.boardTheme]?.toEnumOrDefault(BoardTheme.CLASSIC) ?: BoardTheme.CLASSIC,
            pieceStyle = preferences[Keys.pieceStyle]?.toEnumOrDefault(PieceStyle.CLASSIC) ?: PieceStyle.CLASSIC,
            soundEnabled = preferences[Keys.soundEnabled] ?: true,
            aiDifficulty = preferences[Keys.aiDifficulty]?.toEnumOrDefault(AiDifficulty.MEDIUM) ?: AiDifficulty.MEDIUM,
        )
    }

    suspend fun setBoardTheme(theme: BoardTheme) {
        context.appPreferences.edit { it[Keys.boardTheme] = theme.name }
    }

    suspend fun setPieceStyle(style: PieceStyle) {
        context.appPreferences.edit { it[Keys.pieceStyle] = style.name }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.appPreferences.edit { it[Keys.soundEnabled] = enabled }
    }

    suspend fun setAiDifficulty(difficulty: AiDifficulty) {
        context.appPreferences.edit { it[Keys.aiDifficulty] = difficulty.name }
    }

    suspend fun reset() {
        context.appPreferences.edit { it.clear() }
    }
}

private inline fun <reified T : Enum<T>> String.toEnumOrDefault(default: T): T {
    return enumValues<T>().firstOrNull { it.name == this } ?: default
}
