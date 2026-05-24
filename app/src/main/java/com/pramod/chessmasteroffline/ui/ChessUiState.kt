package com.pramod.chessmasteroffline.ui

import com.pramod.chessmasteroffline.data.AiDifficulty
import com.pramod.chessmasteroffline.data.AppSettings
import com.pramod.chessmasteroffline.data.GameMode
import com.pramod.chessmasteroffline.data.UserProfile
import com.pramod.chessmasteroffline.engine.ChessEngine
import com.pramod.chessmasteroffline.engine.GameState
import com.pramod.chessmasteroffline.engine.PieceColor
import com.pramod.chessmasteroffline.engine.PieceType
import com.pramod.chessmasteroffline.engine.Square

data class PendingPromotion(
    val from: Square,
    val to: Square,
    val choices: List<PieceType>,
)

enum class LocalTimeControl(
    val label: String,
    val durationMillis: Long?,
) {
    BLITZ("Blitz 3m", 180_000L),
    RAPID("Rapid 10m", 600_000L),
    CLASSIC("Classic 30m", 1_800_000L),
    NO_LIMIT("No Limit", null),
}

data class ChessUiState(
    val screen: AppScreen = AppScreen.SPLASH,
    val gameState: GameState = ChessEngine.initialState(),
    val settings: AppSettings = AppSettings(),
    val selectedSquare: Square? = null,
    val legalTargets: Set<Square> = emptySet(),
    val pendingPromotion: PendingPromotion? = null,
    val gameMode: GameMode = GameMode.LOCAL_PLAYER,
    val newGameMode: GameMode = GameMode.LOCAL_PLAYER,
    val newGameDifficulty: AiDifficulty = AiDifficulty.MEDIUM,
    val localWhiteName: String = "",
    val localBlackName: String = "",
    val localWhiteIcon: String = "\u2654",
    val localBlackIcon: String = "\u265A",
    val localTimeControl: LocalTimeControl = LocalTimeControl.RAPID,
    val whiteClockMillis: Long = LocalTimeControl.RAPID.durationMillis ?: 0L,
    val blackClockMillis: Long = LocalTimeControl.RAPID.durationMillis ?: 0L,
    val clockStarted: Boolean = false,
    val clockExpiredColor: PieceColor? = null,
    val aiColor: PieceColor = PieceColor.BLACK,
    val isAiThinking: Boolean = false,
    val isSigningIn: Boolean = false,
    val userProfile: UserProfile? = null,
    val hasSavedGame: Boolean = false,
    val moveSoundTick: Int = 0,
    val message: String? = null,
)
