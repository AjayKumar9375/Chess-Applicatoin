package com.pramod.chessmasteroffline.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Scaffold
import com.pramod.chessmasteroffline.ui.screens.AboutScreen
import com.pramod.chessmasteroffline.ui.screens.BoardScreen
import com.pramod.chessmasteroffline.ui.screens.HomeScreen
import com.pramod.chessmasteroffline.ui.screens.NewGameScreen
import com.pramod.chessmasteroffline.ui.screens.PremiumScreen
import com.pramod.chessmasteroffline.ui.screens.SettingsScreen
import com.pramod.chessmasteroffline.ui.screens.SplashScreen

@Composable
fun ChessMasterApp(viewModel: ChessViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val activity = LocalContext.current.findActivity()
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.dismissMessage()
    }

    BackHandler(enabled = true) {
        when (uiState.screen) {
            AppScreen.SPLASH -> Unit
            AppScreen.HOME -> showExitDialog = true
            AppScreen.BOARD,
            AppScreen.NEW_GAME,
            AppScreen.SETTINGS,
            AppScreen.ABOUT,
            AppScreen.PREMIUM,
            -> viewModel.navigate(AppScreen.HOME)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Surface {
            when (uiState.screen) {
                AppScreen.SPLASH -> SplashScreen(
                    contentPadding = padding,
                    onFinished = viewModel::finishSplash,
                )
                AppScreen.HOME -> HomeScreen(
                    uiState = uiState,
                    contentPadding = padding,
                    onNewGame = { viewModel.navigate(AppScreen.NEW_GAME) },
                    onResume = viewModel::resumeLastGame,
                    onSettings = { viewModel.navigate(AppScreen.SETTINGS) },
                    onAbout = { viewModel.navigate(AppScreen.ABOUT) },
                    onPremium = { viewModel.navigate(AppScreen.PREMIUM) },
                )
                AppScreen.NEW_GAME -> NewGameScreen(
                    uiState = uiState,
                    contentPadding = padding,
                    onBack = { viewModel.navigate(AppScreen.HOME) },
                    onModeSelected = viewModel::setNewGameMode,
                    onDifficultySelected = viewModel::setNewGameDifficulty,
                    onStart = { viewModel.startNewGame() },
                )
                AppScreen.BOARD -> BoardScreen(
                    uiState = uiState,
                    contentPadding = padding,
                    onBackHome = { viewModel.navigate(AppScreen.HOME) },
                    onSquareTapped = viewModel::onSquareTapped,
                    onPromotion = viewModel::choosePromotion,
                    onUndo = viewModel::undoMove,
                    onRestart = viewModel::restartGame,
                    onSave = viewModel::saveCurrentGame,
                    onSettings = { viewModel.navigate(AppScreen.SETTINGS) },
                )
                AppScreen.SETTINGS -> SettingsScreen(
                    uiState = uiState,
                    contentPadding = padding,
                    onBack = { viewModel.navigate(AppScreen.HOME) },
                    onBoardTheme = viewModel::updateBoardTheme,
                    onPieceStyle = viewModel::updatePieceStyle,
                    onSound = viewModel::updateSoundEnabled,
                    onDifficulty = viewModel::updateAiDifficulty,
                    onResetGame = viewModel::restartGame,
                    onClearSaved = viewModel::clearSavedGame,
                    onResetSettings = viewModel::resetSettings,
                )
                AppScreen.ABOUT -> AboutScreen(
                    contentPadding = padding,
                    onBack = { viewModel.navigate(AppScreen.HOME) },
                )
                AppScreen.PREMIUM -> PremiumScreen(
                    contentPadding = padding,
                    onBack = { viewModel.navigate(AppScreen.HOME) },
                )
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Chess Master Offline?") },
            text = { Text("Your current game is saved locally. You can resume it from the home screen.") },
            confirmButton = {
                TextButton(onClick = { activity?.finish() }) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Stay")
                }
            },
        )
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
