package com.pramod.chessmasteroffline.ui

import android.app.Application
import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pramod.chessmasteroffline.ai.ChessAi
import com.pramod.chessmasteroffline.data.AiDifficulty
import com.pramod.chessmasteroffline.data.AuthRepository
import com.pramod.chessmasteroffline.data.BoardTheme
import com.pramod.chessmasteroffline.data.GameMode
import com.pramod.chessmasteroffline.data.PieceStyle
import com.pramod.chessmasteroffline.data.SavedGameRepository
import com.pramod.chessmasteroffline.data.SettingsRepository
import com.pramod.chessmasteroffline.engine.ChessEngine
import com.pramod.chessmasteroffline.engine.GameState
import com.pramod.chessmasteroffline.engine.Move
import com.pramod.chessmasteroffline.engine.PieceColor
import com.pramod.chessmasteroffline.engine.PieceType
import com.pramod.chessmasteroffline.engine.Square
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChessViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application.applicationContext)
    private val savedGameRepository = SavedGameRepository(application.applicationContext)
    private val authRepository = AuthRepository(application.applicationContext)
    private val ai = ChessAi()

    private val _uiState = MutableStateFlow(ChessUiState())
    val uiState: StateFlow<ChessUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update {
                    it.copy(
                        settings = settings,
                        newGameDifficulty = settings.aiDifficulty,
                    )
                }
            }
        }
        viewModelScope.launch {
            savedGameRepository.hasSavedGame.collect { hasSaved ->
                _uiState.update { it.copy(hasSavedGame = hasSaved) }
            }
        }
        viewModelScope.launch {
            authRepository.userProfile.collect { profile ->
                _uiState.update { it.copy(userProfile = profile) }
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                tickMatchClock()
            }
        }
    }

    fun finishSplash() {
        _uiState.update { it.copy(screen = AppScreen.HOME) }
    }

    fun navigate(screen: AppScreen) {
        _uiState.update { it.copy(screen = screen, message = null) }
    }

    fun setNewGameMode(mode: GameMode) {
        _uiState.update { it.copy(newGameMode = mode) }
    }

    fun setNewGameDifficulty(difficulty: AiDifficulty) {
        _uiState.update { it.copy(newGameDifficulty = difficulty) }
        viewModelScope.launch { settingsRepository.setAiDifficulty(difficulty) }
    }

    fun updateLocalWhiteName(name: String) {
        _uiState.update { it.copy(localWhiteName = name.take(16)) }
    }

    fun updateLocalBlackName(name: String) {
        _uiState.update { it.copy(localBlackName = name.take(16)) }
    }

    fun updateLocalWhiteIcon(icon: String) {
        _uiState.update { it.copy(localWhiteIcon = icon) }
    }

    fun updateLocalBlackIcon(icon: String) {
        _uiState.update { it.copy(localBlackIcon = icon) }
    }

    fun updateLocalTimeControl(timeControl: LocalTimeControl) {
        val startingTime = timeControl.durationMillis ?: 0L
        _uiState.update {
            it.copy(
                localTimeControl = timeControl,
                whiteClockMillis = startingTime,
                blackClockMillis = startingTime,
                clockStarted = false,
                clockExpiredColor = null,
            )
        }
    }

    fun launchLocalVoidMatch() {
        val ui = _uiState.value
        if (ui.localWhiteName.isBlank() || ui.localBlackName.isBlank()) {
            _uiState.update { it.copy(message = "Enter both player callsigns.") }
            return
        }
        startNewGame(GameMode.LOCAL_PLAYER, ui.settings.aiDifficulty)
    }

    fun startNewGame(mode: GameMode = _uiState.value.newGameMode, difficulty: AiDifficulty = _uiState.value.newGameDifficulty) {
        val state = ChessEngine.initialState()
        val startingTime = if (mode == GameMode.LOCAL_PLAYER) {
            _uiState.value.localTimeControl.durationMillis ?: 0L
        } else {
            0L
        }
        _uiState.update {
            it.copy(
                screen = AppScreen.BOARD,
                gameState = state,
                selectedSquare = null,
                legalTargets = emptySet(),
                pendingPromotion = null,
                gameMode = mode,
                newGameMode = mode,
                newGameDifficulty = difficulty,
                isAiThinking = false,
                whiteClockMillis = startingTime,
                blackClockMillis = startingTime,
                clockStarted = false,
                clockExpiredColor = null,
                message = null,
            )
        }
        viewModelScope.launch {
            settingsRepository.setAiDifficulty(difficulty)
            savedGameRepository.save(state, mode, difficulty)
        }
    }

    fun resumeLastGame() {
        viewModelScope.launch {
            val saved = savedGameRepository.load()
            if (saved == null) {
                _uiState.update { it.copy(message = "No saved game found.") }
                return@launch
            }
            settingsRepository.setAiDifficulty(saved.aiDifficulty)
            _uiState.update {
                it.copy(
                    screen = AppScreen.BOARD,
                    gameState = saved.state,
                    gameMode = saved.mode,
                    newGameMode = saved.mode,
                    newGameDifficulty = saved.aiDifficulty,
                    selectedSquare = null,
                    legalTargets = emptySet(),
                    pendingPromotion = null,
                    isAiThinking = false,
                    whiteClockMillis = saved.state.initialClockForCurrentMode(),
                    blackClockMillis = saved.state.initialClockForCurrentMode(),
                    clockStarted = saved.state.history.isNotEmpty() && _uiState.value.localTimeControl.durationMillis != null,
                    clockExpiredColor = null,
                    message = "Game resumed.",
                )
            }
            maybeLaunchAi(saved.state)
        }
    }

    fun onSquareTapped(square: Square) {
        val ui = _uiState.value
        val state = ui.gameState
        if (state.isTerminal || ui.isAiThinking || isAiTurn(ui) || ui.pendingPromotion != null || ui.clockExpiredColor != null) return

        val selected = ui.selectedSquare
        val tappedPiece = state.board[square]

        if (selected == null) {
            if (tappedPiece?.color == state.sideToMove) selectSquare(square)
            return
        }

        if (selected == square) {
            clearSelection()
            return
        }

        val matchingMoves = ChessEngine.legalMovesForSquare(state, selected).filter { it.to == square }
        if (matchingMoves.isNotEmpty()) {
            val promotionMoves = matchingMoves.filter { it.promotion != null }
            if (promotionMoves.size > 1) {
                _uiState.update {
                    it.copy(
                        pendingPromotion = PendingPromotion(
                            from = selected,
                            to = square,
                            choices = promotionMoves.mapNotNull { move -> move.promotion },
                        ),
                    )
                }
            } else {
                commitMove(matchingMoves.first())
            }
            return
        }

        if (tappedPiece?.color == state.sideToMove) {
            selectSquare(square)
        } else {
            clearSelection()
        }
    }

    fun choosePromotion(pieceType: PieceType) {
        val pending = _uiState.value.pendingPromotion ?: return
        commitMove(Move(pending.from, pending.to, pieceType))
    }

    fun undoMove() {
        val ui = _uiState.value
        if (ui.isAiThinking) return
        var previous = ChessEngine.undo(ui.gameState) ?: return
        if (ui.gameMode == GameMode.VS_AI && previous.sideToMove == ui.aiColor) {
            previous = ChessEngine.undo(previous) ?: previous
        }
        _uiState.update {
            it.copy(
                gameState = previous,
                selectedSquare = null,
                legalTargets = emptySet(),
                pendingPromotion = null,
                isAiThinking = false,
                clockStarted = previous.history.isNotEmpty() && it.localTimeControl.durationMillis != null,
                clockExpiredColor = null,
                message = "Move undone.",
            )
        }
        persistGame(previous, ui.gameMode, ui.settings.aiDifficulty)
    }

    fun restartGame() {
        startNewGame(_uiState.value.gameMode, _uiState.value.settings.aiDifficulty)
    }

    fun saveCurrentGame() {
        val ui = _uiState.value
        viewModelScope.launch {
            savedGameRepository.save(ui.gameState, ui.gameMode, ui.settings.aiDifficulty)
            _uiState.update { it.copy(message = "Game saved.") }
        }
    }

    fun clearSavedGame() {
        viewModelScope.launch {
            savedGameRepository.clear()
            _uiState.update { it.copy(message = "Saved game cleared.") }
        }
    }

    fun updateBoardTheme(theme: BoardTheme) {
        viewModelScope.launch { settingsRepository.setBoardTheme(theme) }
    }

    fun updatePieceStyle(style: PieceStyle) {
        viewModelScope.launch { settingsRepository.setPieceStyle(style) }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSoundEnabled(enabled) }
    }

    fun updateHolographicThemeEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHolographicThemeEnabled(enabled) }
    }

    fun updateScanlineEffectEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setScanlineEffectEnabled(enabled) }
    }

    fun updatePieceGlowEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setPieceGlowEnabled(enabled) }
    }

    fun updateAiAnalysisOverlayEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAiAnalysisOverlayEnabled(enabled) }
    }

    fun updateHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHapticFeedbackEnabled(enabled) }
    }

    fun updateTwoFactorAuthEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setTwoFactorAuthEnabled(enabled) }
    }

    fun updateAiDifficulty(difficulty: AiDifficulty) {
        setNewGameDifficulty(difficulty)
    }

    fun resetSettings() {
        viewModelScope.launch {
            settingsRepository.reset()
            _uiState.update { it.copy(message = "Settings reset.") }
        }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun signInWithGoogle(activityContext: Context) {
        if (_uiState.value.isSigningIn) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningIn = true, message = null) }
            val result = authRepository.signInWithGoogle(activityContext)
            _uiState.update {
                it.copy(
                    isSigningIn = false,
                    message = result.fold(
                        onSuccess = { profile -> "Signed in as ${profile.email}." },
                        onFailure = { error -> error.message ?: "Google sign-in failed." },
                    ),
                )
            }
        }
    }

    fun signOut(activityContext: Context) {
        viewModelScope.launch {
            authRepository.signOut(activityContext)
            _uiState.update { it.copy(message = "Signed out. Offline mode remains available.") }
        }
    }

    private fun selectSquare(square: Square) {
        val targets = ChessEngine.legalMovesForSquare(_uiState.value.gameState, square).map { it.to }.toSet()
        _uiState.update { it.copy(selectedSquare = square, legalTargets = targets, message = null) }
    }

    private fun clearSelection() {
        _uiState.update { it.copy(selectedSquare = null, legalTargets = emptySet()) }
    }

    private fun commitMove(move: Move) {
        val ui = _uiState.value
        val next = ChessEngine.makeMove(ui.gameState, move)
        if (next == null) {
            _uiState.update { it.copy(message = "Illegal move.") }
            return
        }

        _uiState.update {
            it.copy(
                gameState = next,
                selectedSquare = null,
                legalTargets = emptySet(),
                pendingPromotion = null,
                moveSoundTick = it.moveSoundTick + 1,
                clockStarted = it.clockStarted || it.localTimeControl.durationMillis != null,
                message = null,
            )
        }
        persistGame(next, ui.gameMode, ui.settings.aiDifficulty)
        maybeLaunchAi(next)
    }

    private fun maybeLaunchAi(stateAfterMove: GameState) {
        val ui = _uiState.value
        if (ui.gameMode != GameMode.VS_AI || stateAfterMove.sideToMove != ui.aiColor || stateAfterMove.isTerminal) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAiThinking = true) }
            val difficulty = _uiState.value.settings.aiDifficulty
            val startedAt = SystemClock.elapsedRealtime()
            val chosenMove = withContext(Dispatchers.Default) {
                ai.chooseMove(stateAfterMove, difficulty)
            }
            val elapsed = SystemClock.elapsedRealtime() - startedAt
            val minimumThinkTime = difficulty.minimumThinkTimeMillis()
            if (elapsed < minimumThinkTime) {
                delay(minimumThinkTime - elapsed)
            }

            val current = _uiState.value
            if (current.gameState != stateAfterMove || current.gameMode != GameMode.VS_AI) {
                _uiState.update { it.copy(isAiThinking = false) }
                return@launch
            }

            val aiState = chosenMove?.let { ChessEngine.makeMove(current.gameState, it) }
            _uiState.update {
                it.copy(
                    gameState = aiState ?: current.gameState,
                    isAiThinking = false,
                    selectedSquare = null,
                    legalTargets = emptySet(),
                    moveSoundTick = if (aiState != null) it.moveSoundTick + 1 else it.moveSoundTick,
                    clockStarted = if (aiState != null) it.clockStarted || it.localTimeControl.durationMillis != null else it.clockStarted,
                )
            }
            if (aiState != null) {
                savedGameRepository.save(aiState, GameMode.VS_AI, difficulty)
            }
        }
    }

    private fun isAiTurn(ui: ChessUiState): Boolean {
        return ui.gameMode == GameMode.VS_AI && ui.gameState.sideToMove == ui.aiColor
    }

    private fun persistGame(state: GameState, mode: GameMode, difficulty: AiDifficulty) {
        viewModelScope.launch {
            savedGameRepository.save(state, mode, difficulty)
        }
    }

    private fun tickMatchClock() {
        _uiState.update { ui ->
            val duration = ui.localTimeControl.durationMillis
            if (
                ui.screen != AppScreen.BOARD ||
                ui.gameMode != GameMode.LOCAL_PLAYER ||
                duration == null ||
                !ui.clockStarted ||
                ui.gameState.isTerminal ||
                ui.clockExpiredColor != null
            ) {
                ui
            } else {
                val whiteClock = if (ui.gameState.sideToMove == PieceColor.WHITE) {
                    (ui.whiteClockMillis - 1_000L).coerceAtLeast(0L)
                } else {
                    ui.whiteClockMillis
                }
                val blackClock = if (ui.gameState.sideToMove == PieceColor.BLACK) {
                    (ui.blackClockMillis - 1_000L).coerceAtLeast(0L)
                } else {
                    ui.blackClockMillis
                }
                val expiredColor = when {
                    ui.gameState.sideToMove == PieceColor.WHITE && whiteClock == 0L -> PieceColor.WHITE
                    ui.gameState.sideToMove == PieceColor.BLACK && blackClock == 0L -> PieceColor.BLACK
                    else -> null
                }
                ui.copy(
                    whiteClockMillis = whiteClock,
                    blackClockMillis = blackClock,
                    clockExpiredColor = expiredColor,
                    message = expiredColor?.let { color -> "${color.clockLabel()} time expired." } ?: ui.message,
                )
            }
        }
    }

    private fun GameState.initialClockForCurrentMode(): Long {
        return _uiState.value.localTimeControl.durationMillis ?: 0L
    }

    private fun PieceColor.clockLabel(): String {
        return if (this == PieceColor.WHITE) "White" else "Black"
    }

    private fun AiDifficulty.minimumThinkTimeMillis(): Long {
        return when (this) {
            AiDifficulty.EASY -> 850L
            AiDifficulty.MEDIUM -> 1_250L
            AiDifficulty.HARD -> 1_700L
        }
    }
}
