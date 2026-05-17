package com.pramod.chessmasteroffline.data

enum class BoardTheme {
    CLASSIC,
    OCEAN,
    SLATE,
}

enum class PieceStyle {
    CLASSIC,
    MINIMAL,
}

enum class AiDifficulty {
    EASY,
    MEDIUM,
    HARD,
}

enum class GameMode {
    LOCAL_PLAYER,
    VS_AI,
}

data class AppSettings(
    val boardTheme: BoardTheme = BoardTheme.CLASSIC,
    val pieceStyle: PieceStyle = PieceStyle.CLASSIC,
    val soundEnabled: Boolean = true,
    val aiDifficulty: AiDifficulty = AiDifficulty.MEDIUM,
)
