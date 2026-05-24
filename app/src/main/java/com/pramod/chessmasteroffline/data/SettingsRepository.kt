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
        val holographicThemeEnabled = booleanPreferencesKey("holographic_theme_enabled")
        val scanlineEffectEnabled = booleanPreferencesKey("scanline_effect_enabled")
        val pieceGlowEnabled = booleanPreferencesKey("piece_glow_enabled")
        val aiAnalysisOverlayEnabled = booleanPreferencesKey("ai_analysis_overlay_enabled")
        val soundEnabled = booleanPreferencesKey("sound_enabled")
        val hapticFeedbackEnabled = booleanPreferencesKey("haptic_feedback_enabled")
        val twoFactorAuthEnabled = booleanPreferencesKey("two_factor_auth_enabled")
        val aiDifficulty = stringPreferencesKey("ai_difficulty")
    }

    val settings: Flow<AppSettings> = context.appPreferences.data.map { preferences ->
        AppSettings(
            boardTheme = preferences[Keys.boardTheme]?.toEnumOrDefault(BoardTheme.CLASSIC) ?: BoardTheme.CLASSIC,
            pieceStyle = preferences[Keys.pieceStyle]?.toEnumOrDefault(PieceStyle.CLASSIC) ?: PieceStyle.CLASSIC,
            holographicThemeEnabled = preferences[Keys.holographicThemeEnabled] ?: true,
            scanlineEffectEnabled = preferences[Keys.scanlineEffectEnabled] ?: true,
            pieceGlowEnabled = preferences[Keys.pieceGlowEnabled] ?: true,
            aiAnalysisOverlayEnabled = preferences[Keys.aiAnalysisOverlayEnabled] ?: true,
            soundEnabled = preferences[Keys.soundEnabled] ?: true,
            hapticFeedbackEnabled = preferences[Keys.hapticFeedbackEnabled] ?: true,
            twoFactorAuthEnabled = preferences[Keys.twoFactorAuthEnabled] ?: false,
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

    suspend fun setHolographicThemeEnabled(enabled: Boolean) {
        context.appPreferences.edit { it[Keys.holographicThemeEnabled] = enabled }
    }

    suspend fun setScanlineEffectEnabled(enabled: Boolean) {
        context.appPreferences.edit { it[Keys.scanlineEffectEnabled] = enabled }
    }

    suspend fun setPieceGlowEnabled(enabled: Boolean) {
        context.appPreferences.edit { it[Keys.pieceGlowEnabled] = enabled }
    }

    suspend fun setAiAnalysisOverlayEnabled(enabled: Boolean) {
        context.appPreferences.edit { it[Keys.aiAnalysisOverlayEnabled] = enabled }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.appPreferences.edit { it[Keys.hapticFeedbackEnabled] = enabled }
    }

    suspend fun setTwoFactorAuthEnabled(enabled: Boolean) {
        context.appPreferences.edit { it[Keys.twoFactorAuthEnabled] = enabled }
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
