package com.pramod.chessmasteroffline.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
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
import com.pramod.chessmasteroffline.data.AiDifficulty
import com.pramod.chessmasteroffline.data.GameMode
import com.pramod.chessmasteroffline.ui.screens.BoardScreen
import com.pramod.chessmasteroffline.ui.screens.HomeScreen
import com.pramod.chessmasteroffline.ui.screens.LocalSetupScreen
import com.pramod.chessmasteroffline.ui.screens.PlayerProfileScreen
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
            AppScreen.LOCAL_SETUP,
            AppScreen.PROFILE,
            AppScreen.SETTINGS,
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
                    activeScreen = AppScreen.HOME,
                    onQuickMatch = { viewModel.startNewGame(GameMode.VS_AI, AiDifficulty.MEDIUM) },
                    onChallengePlayer = { viewModel.navigate(AppScreen.LOCAL_SETUP) },
                    onTrainingMode = { viewModel.startNewGame(GameMode.VS_AI, AiDifficulty.HARD) },
                    onSignIn = { viewModel.signInWithGoogle(activity ?: it) },
                    onSignOut = { viewModel.signOut(activity ?: it) },
                    onNavigate = viewModel::navigate,
                )
                AppScreen.LOCAL_SETUP -> LocalSetupScreen(
                    uiState = uiState,
                    contentPadding = padding,
                    onWhiteNameChange = viewModel::updateLocalWhiteName,
                    onBlackNameChange = viewModel::updateLocalBlackName,
                    onWhiteIconSelected = viewModel::updateLocalWhiteIcon,
                    onBlackIconSelected = viewModel::updateLocalBlackIcon,
                    onTimeControlSelected = viewModel::updateLocalTimeControl,
                    onLaunch = viewModel::launchLocalVoidMatch,
                    onBack = { viewModel.navigate(AppScreen.HOME) },
                )
                AppScreen.BOARD -> BoardScreen(
                    uiState = uiState,
                    contentPadding = padding,
                    activeScreen = AppScreen.BOARD,
                    onSquareTapped = viewModel::onSquareTapped,
                    onPromotion = viewModel::choosePromotion,
                    onUndo = viewModel::undoMove,
                    onRestart = viewModel::restartGame,
                    onSave = viewModel::saveCurrentGame,
                    onNavigate = viewModel::navigate,
                )
                AppScreen.PROFILE -> PlayerProfileScreen(
                    uiState = uiState,
                    contentPadding = padding,
                    activeScreen = AppScreen.PROFILE,
                    onSignIn = { viewModel.signInWithGoogle(activity ?: it) },
                    onSignOut = { viewModel.signOut(activity ?: it) },
                    onNavigate = viewModel::navigate,
                )
                AppScreen.SETTINGS -> SettingsScreen(
                    uiState = uiState,
                    contentPadding = padding,
                    activeScreen = AppScreen.SETTINGS,
                    onHolographicTheme = viewModel::updateHolographicThemeEnabled,
                    onScanlines = viewModel::updateScanlineEffectEnabled,
                    onPieceGlow = viewModel::updatePieceGlowEnabled,
                    onAiOverlay = viewModel::updateAiAnalysisOverlayEnabled,
                    onSound = viewModel::updateSoundEnabled,
                    onHaptic = viewModel::updateHapticFeedbackEnabled,
                    onTwoFactor = viewModel::updateTwoFactorAuthEnabled,
                    onDifficulty = viewModel::updateAiDifficulty,
                    onResetGame = viewModel::restartGame,
                    onNavigate = viewModel::navigate,
                )
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("EXIT VOIDBOARD?") },
            text = { Text("CURRENT MATCH STATE IS STORED LOCALLY.") },
            confirmButton = {
                TextButton(onClick = { activity?.finish() }) {
                    Text("EXIT")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("STAY")
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
