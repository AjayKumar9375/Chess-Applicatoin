package com.pramod.chessmasteroffline.ui

import com.pramod.chessmasteroffline.data.AiDifficulty
import com.pramod.chessmasteroffline.data.AppSettings
import com.pramod.chessmasteroffline.data.GameMode
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
    val aiColor: PieceColor = PieceColor.BLACK,
    val isAiThinking: Boolean = false,
    val hasSavedGame: Boolean = false,
    val moveSoundTick: Int = 0,
    val message: String? = null,
)
