package com.pramod.chessmasteroffline.ui.screens

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pramod.chessmasteroffline.BuildConfig
import com.pramod.chessmasteroffline.R
import com.pramod.chessmasteroffline.data.AiDifficulty
import com.pramod.chessmasteroffline.data.GameMode
import com.pramod.chessmasteroffline.engine.GameStatus
import com.pramod.chessmasteroffline.engine.MoveRecord
import com.pramod.chessmasteroffline.engine.PieceColor
import com.pramod.chessmasteroffline.engine.PieceType
import com.pramod.chessmasteroffline.ui.AppScreen
import com.pramod.chessmasteroffline.ui.ChessUiState
import com.pramod.chessmasteroffline.ui.LocalTimeControl
import com.pramod.chessmasteroffline.ui.components.ChessBoard
import com.pramod.chessmasteroffline.ui.components.boardStatusText
import com.pramod.chessmasteroffline.ui.theme.HudAmber
import com.pramod.chessmasteroffline.ui.theme.HudBackground
import com.pramod.chessmasteroffline.ui.theme.HudBlue
import com.pramod.chessmasteroffline.ui.theme.HudBorder
import com.pramod.chessmasteroffline.ui.theme.HudElevated
import com.pramod.chessmasteroffline.ui.theme.HudGreen
import com.pramod.chessmasteroffline.ui.theme.HudMuted
import com.pramod.chessmasteroffline.ui.theme.HudSurface
import com.pramod.chessmasteroffline.ui.theme.HudText
import com.pramod.chessmasteroffline.ui.theme.HudViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(contentPadding: PaddingValues, onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1_200)
        onFinished()
    }

    HudRoot(
        contentPadding = contentPadding,
        scanlines = true,
        bottomBar = {},
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.ars_gaming_logo),
                    contentDescription = "ARS Gaming logo",
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .aspectRatio(1f),
                    contentScale = ContentScale.Fit,
                )
                HudText("VOIDBOARD", color = HudBlue, style = MaterialTheme.typography.headlineMedium)
                HudText("ARS GAMING PRESENTS", color = HudMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun HomeScreen(
    uiState: ChessUiState,
    contentPadding: PaddingValues,
    activeScreen: AppScreen,
    onQuickMatch: () -> Unit,
    onChallengePlayer: () -> Unit,
    onTrainingMode: () -> Unit,
    onSignIn: (Context) -> Unit,
    onSignOut: (Context) -> Unit,
    onNavigate: (AppScreen) -> Unit,
) {
    val context = LocalContext.current
    HudRoot(
        contentPadding = contentPadding,
        scanlines = uiState.settings.scanlineEffectEnabled,
        bottomBar = { MainHudNav(activeScreen = activeScreen, onNavigate = onNavigate) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HudStatusBar(statusText = accountStatus(uiState))
            HudScreenFrame {
                HudText("BUILD ${BuildConfig.VERSION_NAME}", color = HudMuted, style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(8.dp))
                HudText("VOIDBOARD", color = HudBlue, style = MaterialTheme.typography.headlineMedium)
                HudText("INITIALIZING NEURAL MATCH ENGINE...", color = HudMuted, style = MaterialTheme.typography.bodySmall)
                AccountHudCard(
                    uiState = uiState,
                    onSignIn = { onSignIn(context) },
                    onSignOut = { onSignOut(context) },
                )
                MiniVoidGrid(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 10.dp),
                    size = 64.dp,
                )
                HudDivider()
                HudButton("QUICK MATCH", ButtonStyle.PRIMARY, uiState.settings.hapticFeedbackEnabled, onQuickMatch)
                HudButton("CHALLENGE PLAYER", ButtonStyle.SECONDARY, uiState.settings.hapticFeedbackEnabled, onChallengePlayer)
                HudButton("AI TRAINING MODE", ButtonStyle.GHOST, uiState.settings.hapticFeedbackEnabled, onTrainingMode)
                HudDivider()
                CurrentSessionPanel(uiState)
            }
        }
    }
}

@Composable
fun LocalSetupScreen(
    uiState: ChessUiState,
    contentPadding: PaddingValues,
    onWhiteNameChange: (String) -> Unit,
    onBlackNameChange: (String) -> Unit,
    onWhiteIconSelected: (String) -> Unit,
    onBlackIconSelected: (String) -> Unit,
    onTimeControlSelected: (LocalTimeControl) -> Unit,
    onLaunch: () -> Unit,
    onBack: () -> Unit,
) {
    val ready = uiState.localWhiteName.isNotBlank() && uiState.localBlackName.isNotBlank()
    HudRoot(
        contentPadding = contentPadding,
        scanlines = uiState.settings.scanlineEffectEnabled,
        bottomBar = {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HudStatusBar(statusText = "LOCAL MATCH")
            HudScreenFrame {
                HudText("VOID MATCH SETUP", color = HudBlue, style = MaterialTheme.typography.headlineSmall)
                HudText("ENTER BOTH CALLSIGNS BEFORE LAUNCH", color = HudMuted, style = MaterialTheme.typography.bodySmall)
                PlayerSetupCard(
                    title = "PLAYER 1 - WHITE \u2659",
                    forceLabel = "CYAN FORCE",
                    name = uiState.localWhiteName,
                    selectedIcon = uiState.localWhiteIcon,
                    icons = whiteSetupIcons,
                    accent = HudBlue,
                    onNameChange = onWhiteNameChange,
                    onIconSelected = onWhiteIconSelected,
                )
                PlayerSetupCard(
                    title = "PLAYER 2 - BLACK \u265F",
                    forceLabel = "VIOLET FORCE",
                    name = uiState.localBlackName,
                    selectedIcon = uiState.localBlackIcon,
                    icons = blackSetupIcons,
                    accent = HudViolet,
                    onNameChange = onBlackNameChange,
                    onIconSelected = onBlackIconSelected,
                )
                TimeControlSelector(
                    selected = uiState.localTimeControl,
                    onSelected = onTimeControlSelected,
                )
                HudDivider()
                HudButton(
                    label = "LAUNCH VOID MATCH",
                    style = ButtonStyle.PRIMARY,
                    hapticEnabled = uiState.settings.hapticFeedbackEnabled,
                    onClick = onLaunch,
                    enabled = ready,
                )
                HudButton(
                    label = "BACK TO LOBBY",
                    style = ButtonStyle.GHOST,
                    hapticEnabled = uiState.settings.hapticFeedbackEnabled,
                    onClick = onBack,
                )
            }
        }
    }
}

@Composable
fun BoardScreen(
    uiState: ChessUiState,
    contentPadding: PaddingValues,
    activeScreen: AppScreen,
    onSquareTapped: (com.pramod.chessmasteroffline.engine.Square) -> Unit,
    onPromotion: (PieceType) -> Unit,
    onUndo: () -> Unit,
    onRestart: () -> Unit,
    onSave: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
) {
    val tone = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 48) }
    val checkedColor = checkedPlayerColor(uiState)
    DisposableEffect(Unit) {
        onDispose { tone.release() }
    }
    LaunchedEffect(uiState.moveSoundTick) {
        if (uiState.moveSoundTick > 0 && uiState.settings.soundEnabled) {
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        }
    }

    HudRoot(
        contentPadding = contentPadding,
        scanlines = uiState.settings.scanlineEffectEnabled,
        bottomBar = {
            BoardActionNav(
                hapticEnabled = uiState.settings.hapticFeedbackEnabled,
                onUndo = onUndo,
                onResign = onRestart,
                onMoves = onSave,
                onAnalysis = { onNavigate(AppScreen.SETTINGS) },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HudStatusBar(
                statusText = "MOVE ${uiState.gameState.fullMoveNumber}",
                isCheck = checkedColor != null,
            )
            PlayerCard(
                initials = opponentInitials(uiState),
                name = opponentName(uiState),
                subtitle = if (checkedColor == PieceColor.BLACK) checkPlayerSubtitle else opponentSubtitle(uiState),
                status = playerCardStatus(uiState, PieceColor.BLACK),
                color = HudAmber,
                isChecked = checkedColor == PieceColor.BLACK,
                isActive = uiState.gameState.sideToMove == PieceColor.BLACK,
            )
            HudCard(modifier = Modifier.fillMaxWidth()) {
                ChessBoard(
                    state = uiState.gameState,
                    selectedSquare = uiState.selectedSquare,
                    legalTargets = uiState.legalTargets,
                    pieceGlowEnabled = uiState.settings.pieceGlowEnabled,
                    hapticFeedbackEnabled = uiState.settings.hapticFeedbackEnabled,
                    onSquareTapped = onSquareTapped,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                )
                FileLabels()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    HudText(
                        text = "${sideLabel(uiState.gameState.sideToMove)} TO MOVE",
                        color = if (uiState.gameState.sideToMove == PieceColor.BLACK) HudAmber else HudBlue,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            if (checkedColor != null) {
                CheckAlertBar()
            }
            PlayerCard(
                initials = whitePlayerInitials(uiState),
                name = whitePlayerName(uiState),
                subtitle = if (checkedColor == PieceColor.WHITE) checkPlayerSubtitle else whitePlayerSubtitle(uiState),
                status = playerCardStatus(uiState, PieceColor.WHITE),
                color = HudBlue,
                isChecked = checkedColor == PieceColor.WHITE,
                isActive = uiState.gameState.sideToMove == PieceColor.WHITE,
            )
            GameStatusStrip(uiState)
            MoveHistory(records = uiState.gameState.history)
        }
    }

    val pendingPromotion = uiState.pendingPromotion
    if (pendingPromotion != null) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("PROMOTE PAWN") },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pendingPromotion.choices.forEach { choice ->
                        OutlinedButton(onClick = { onPromotion(choice) }, shape = RoundedCornerShape(8.dp)) {
                            Text(choice.notationLetter)
                        }
                    }
                }
            },
            confirmButton = {},
        )
    }
}

@Composable
fun PlayerProfileScreen(
    uiState: ChessUiState,
    contentPadding: PaddingValues,
    activeScreen: AppScreen,
    onSignIn: (Context) -> Unit,
    onSignOut: (Context) -> Unit,
    onNavigate: (AppScreen) -> Unit,
) {
    val context = LocalContext.current
    val profile = uiState.userProfile
    HudRoot(
        contentPadding = contentPadding,
        scanlines = uiState.settings.scanlineEffectEnabled,
        bottomBar = { MainHudNav(activeScreen = activeScreen, onNavigate = onNavigate) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HudStatusBar(statusText = "PROFILE")
            HudScreenFrame {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, HudBlue, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    HudText(profile?.initials ?: "G", color = HudBlue, style = MaterialTheme.typography.titleLarge)
                }
                HudText(
                    profile?.displayName ?: "GUEST PLAYER",
                    color = HudText,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                HudText(
                    profile?.email ?: "OFFLINE GUEST PROFILE",
                    color = HudMuted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                AccountStateBadge(signedIn = profile != null)
                if (profile == null) {
                    HudButton(
                        label = if (uiState.isSigningIn) "AUTH LINK ACTIVE..." else "SIGN IN WITH GOOGLE",
                        style = ButtonStyle.PRIMARY,
                        hapticEnabled = uiState.settings.hapticFeedbackEnabled,
                        onClick = { onSignIn(context) },
                    )
                    HudText(
                        "OFFLINE GUEST MODE REMAINS AVAILABLE",
                        color = HudMuted,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                } else {
                    HudButton(
                        label = "SIGN OUT GOOGLE ACCOUNT",
                        style = ButtonStyle.GHOST,
                        hapticEnabled = uiState.settings.hapticFeedbackEnabled,
                        onClick = { onSignOut(context) },
                    )
                }
                HudDivider()
                StatsRow(uiState)
                HudText("CURRENT GAME ACTIVITY", color = HudMuted, style = MaterialTheme.typography.labelMedium)
                CurrentGameActivity(uiState.gameState.history)
            }
        }
    }
}

@Composable
fun SettingsScreen(
    uiState: ChessUiState,
    contentPadding: PaddingValues,
    activeScreen: AppScreen,
    onHolographicTheme: (Boolean) -> Unit,
    onScanlines: (Boolean) -> Unit,
    onPieceGlow: (Boolean) -> Unit,
    onAiOverlay: (Boolean) -> Unit,
    onSound: (Boolean) -> Unit,
    onHaptic: (Boolean) -> Unit,
    onTwoFactor: (Boolean) -> Unit,
    onDifficulty: (AiDifficulty) -> Unit,
    onResetGame: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
) {
    HudRoot(
        contentPadding = contentPadding,
        scanlines = uiState.settings.scanlineEffectEnabled,
        bottomBar = { MainHudNav(activeScreen = activeScreen, onNavigate = onNavigate) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HudStatusBar(statusText = "CONFIG")
            HudScreenFrame {
                HudText("SYSTEM CONFIG", color = HudBlue, style = MaterialTheme.typography.headlineSmall)
                SettingSection("DISPLAY")
                HudSettingRow("HOLOGRAPHIC THEME", "NEON GRID OVERLAY", uiState.settings.holographicThemeEnabled, onHolographicTheme)
                HudSettingRow("SCANLINE EFFECT", "RETRO CRT FILTER", uiState.settings.scanlineEffectEnabled, onScanlines)
                HudSettingRow("PIECE GLOW", "AMBIENT NEON HALO", uiState.settings.pieceGlowEnabled, onPieceGlow)
                SettingSection("GAMEPLAY")
                HudSettingRow("AI ANALYSIS OVERLAY", "REAL-TIME EVAL BAR", uiState.settings.aiAnalysisOverlayEnabled, onAiOverlay)
                HudSettingRow("MOVE SOUND FX", "HOLODECK AUDIO PACK", uiState.settings.soundEnabled, onSound)
                HudSettingRow("HAPTIC FEEDBACK", "VIBRATION ON MOVE", uiState.settings.hapticFeedbackEnabled, onHaptic)
                DifficultyConfig(uiState.settings.aiDifficulty, onDifficulty)
                SettingSection("ACCOUNT")
                HudSettingRow("TWO-FACTOR AUTH", "BIOMETRIC LOCK", uiState.settings.twoFactorAuthEnabled, onTwoFactor)
                HudDivider()
                HudButton("RESET CURRENT MATCH", ButtonStyle.GHOST, uiState.settings.hapticFeedbackEnabled, onResetGame)
                HudText(
                    "BUILD ${BuildConfig.VERSION_NAME} - LOCAL NODE",
                    color = HudMuted,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

@Composable
private fun HudRoot(
    contentPadding: PaddingValues,
    scanlines: Boolean,
    bottomBar: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(HudBackground, HudSurface, HudBackground),
                ),
            )
            .padding(contentPadding),
    ) {
        content()
        CornerBrackets(modifier = Modifier.fillMaxSize().padding(16.dp))
        if (scanlines) {
            ScanlineOverlay()
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            bottomBar()
        }
    }
}

@Composable
private fun HudScreenFrame(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .padding(bottom = 60.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

@Composable
private fun HudStatusBar(statusText: String, isCheck: Boolean = false) {
    var time by remember { mutableStateOf(currentTime()) }
    LaunchedEffect(Unit) {
        while (true) {
            time = currentTime()
            delay(30_000)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HudText(time, color = HudBlue.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.weight(1f))
        if (isCheck) {
            CheckStatusSignal()
        } else {
            LivePulseDot(size = 6.dp)
            Spacer(modifier = Modifier.width(7.dp))
            HudText(statusText, color = HudMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun MainHudNav(activeScreen: AppScreen, onNavigate: (AppScreen) -> Unit) {
    HudBottomBar(
        items = listOf(
            NavItem("⌂", "HOME", AppScreen.HOME),
            NavItem("▷", "PLAY", AppScreen.BOARD),
            NavItem("◎", "PROFILE", AppScreen.PROFILE),
            NavItem("⚙", "SYSTEM", AppScreen.SETTINGS),
        ),
        activeScreen = activeScreen,
        onNavigate = onNavigate,
    )
}

@Composable
private fun HudBottomBar(items: List<NavItem>, activeScreen: AppScreen, onNavigate: (AppScreen) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(HudBackground.copy(alpha = 0.96f))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        items.forEach { item ->
            val active = item.screen == activeScreen
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(item.screen) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HudText(item.icon, color = if (active) HudBlue else HudMuted, style = MaterialTheme.typography.labelMedium)
                HudText(item.label, color = if (active) HudBlue else HudMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun BoardActionNav(
    hapticEnabled: Boolean,
    onUndo: () -> Unit,
    onResign: () -> Unit,
    onMoves: () -> Unit,
    onAnalysis: () -> Unit,
) {
    val actions = listOf(
        "UNDO" to onUndo,
        "RESIGN" to onResign,
        "MOVES" to onMoves,
        "ANALYSIS" to onAnalysis,
    )
    val view = LocalView.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(HudBackground.copy(alpha = 0.96f))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        actions.forEach { (label, action) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        if (hapticEnabled) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                        action()
                    },
                contentAlignment = Alignment.Center,
            ) {
                HudText(label, color = HudMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private data class NavItem(val icon: String, val label: String, val screen: AppScreen)

private enum class ButtonStyle {
    PRIMARY,
    SECONDARY,
    GHOST,
}

private val CheckRed = Color(0xFFFF1744)
private val whiteSetupIcons = listOf("\u2654", "\u2658", "\u2657", "\u2656", "\u2655")
private val blackSetupIcons = listOf("\u265A", "\u265E", "\u265D", "\u265C", "\u265B")
private const val checkPlayerSubtitle = "\u265A king in check"

@Composable
private fun HudButton(
    label: String,
    style: ButtonStyle,
    hapticEnabled: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val view = LocalView.current
    val baseAccent = when (style) {
        ButtonStyle.PRIMARY -> HudBlue
        ButtonStyle.SECONDARY -> HudViolet
        ButtonStyle.GHOST -> HudMuted
    }
    val accent = if (enabled) baseAccent else HudMuted.copy(alpha = 0.42f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when (style) {
                    ButtonStyle.PRIMARY -> baseAccent.copy(alpha = if (enabled) 0.12f else 0.04f)
                    ButtonStyle.SECONDARY -> baseAccent.copy(alpha = if (enabled) 0.10f else 0.04f)
                    ButtonStyle.GHOST -> Color.Transparent
                },
            )
            .border(1.dp, accent.copy(alpha = if (style == ButtonStyle.GHOST) 0.45f else 1f), RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) {
                if (hapticEnabled) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        HudText("▶  $label", color = accent, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun HudCard(
    modifier: Modifier = Modifier,
    borderColor: Color = HudBorder,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(HudSurface)
                .border(0.5.dp, borderColor, RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
        CornerBrackets(modifier = Modifier.matchParentSize(), inset = 4.dp, length = 13.dp)
    }
}

@Composable
private fun PlayerCard(
    initials: String,
    name: String,
    subtitle: String,
    status: String,
    color: Color,
    isChecked: Boolean = false,
    isActive: Boolean = false,
) {
    val borderColor = when {
        isChecked -> CheckRed.copy(alpha = 0.35f)
        isActive -> color.copy(alpha = 0.70f)
        else -> HudBorder
    }
    HudCard(borderColor = borderColor) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Avatar(
                initials = initials,
                color = color,
                size = 42.dp,
                borderColor = if (isChecked) CheckRed else if (isActive) color else color.copy(alpha = 0.55f),
            )
            Column(modifier = Modifier.weight(1f)) {
                HudText(name, color = HudText, style = MaterialTheme.typography.titleMedium)
                HudText(
                    subtitle,
                    color = if (isChecked) CheckRed else HudMuted,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            HudText(
                status,
                color = when {
                    isChecked -> CheckRed
                    isActive -> color
                    else -> HudMuted
                },
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun PlayerSetupCard(
    title: String,
    forceLabel: String,
    name: String,
    selectedIcon: String,
    icons: List<String>,
    accent: Color,
    onNameChange: (String) -> Unit,
    onIconSelected: (String) -> Unit,
) {
    HudCard(borderColor = accent.copy(alpha = 0.45f)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Avatar(initials = selectedIcon, color = accent, size = 46.dp, borderColor = accent)
            Column(modifier = Modifier.weight(1f)) {
                HudText(title, color = accent, style = MaterialTheme.typography.titleMedium)
                HudText(forceLabel, color = HudMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
        CallsignInput(
            value = name,
            accent = accent,
            onValueChange = { onNameChange(it.take(16)) },
        )
        PieceIconPicker(
            icons = icons,
            selectedIcon = selectedIcon,
            accent = accent,
            onSelected = onIconSelected,
        )
    }
}

@Composable
private fun CallsignInput(value: String, accent: Color, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = HudText,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Normal,
        ),
        cursorBrush = SolidColor(accent),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF040A16))
                    .border(1.dp, Color(0xFF0D2240), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isBlank()) {
                    HudText("CALLSIGN - MAX 16", color = HudMuted, style = MaterialTheme.typography.bodySmall)
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun PieceIconPicker(
    icons: List<String>,
    selectedIcon: String,
    accent: Color,
    onSelected: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        icons.forEach { icon ->
            val active = icon == selectedIcon
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) accent.copy(alpha = 0.16f) else Color.Transparent)
                    .border(0.5.dp, if (active) accent else HudBorder, RoundedCornerShape(8.dp))
                    .clickable { onSelected(icon) },
                contentAlignment = Alignment.Center,
            ) {
                HudText(icon, color = if (active) accent else HudMuted, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun TimeControlSelector(selected: LocalTimeControl, onSelected: (LocalTimeControl) -> Unit) {
    HudCard {
        HudText("TIME CONTROL", color = HudMuted, style = MaterialTheme.typography.labelMedium)
        LocalTimeControl.entries.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { control ->
                    val active = selected == control
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) HudBlue.copy(alpha = 0.13f) else Color.Transparent)
                            .border(0.5.dp, if (active) HudBlue else HudBorder, RoundedCornerShape(8.dp))
                            .clickable { onSelected(control) },
                        contentAlignment = Alignment.Center,
                    ) {
                        HudText(
                            control.label,
                            color = if (active) HudBlue else HudMuted,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CheckAlertBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CheckRed.copy(alpha = 0.10f))
            .border(1.dp, CheckRed.copy(alpha = 0.40f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            PulsingDot(size = 8.dp, color = CheckRed, label = "checkAlertPulse")
            HudText(
                "king in check \u00B7 protect your king",
                color = CheckRed,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun GameStatusStrip(uiState: ChessUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HudDivider()
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HudText("LAST MOVES", color = HudMuted, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.weight(1f))
            uiState.gameState.history.takeLast(3).forEach { record ->
                MoveChip(record)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HudMetric("MOVE", uiState.gameState.fullMoveNumber.toString(), HudGreen, Modifier.weight(1f))
            HudMetric("TURN", sideLabel(uiState.gameState.sideToMove), HudBlue, Modifier.weight(1f))
            HudMetric("STATUS", uiState.gameState.status.displayLabel, HudViolet, Modifier.weight(1f))
        }
        HudText(boardStatusText(uiState.gameState, uiState.isAiThinking), color = if (uiState.isAiThinking) HudViolet else HudGreen, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun MoveChip(record: MoveRecord) {
    val color = if (record.color == PieceColor.WHITE) HudBlue else HudAmber
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.13f))
            .border(0.5.dp, color.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
    ) {
        HudText(record.notation, color = color, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun HudMetric(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    HudCard(modifier = modifier) {
        HudText(label, color = HudMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally))
        HudText(value, color = color, style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun MoveHistory(records: List<MoveRecord>) {
    HudCard {
        HudText("MOVE LOG", color = HudMuted, style = MaterialTheme.typography.labelSmall)
        if (records.isEmpty()) {
            HudText("NO MOVES RECORDED", color = HudMuted, style = MaterialTheme.typography.bodySmall)
        } else {
            records.takeLast(6).chunked(2).forEachIndexed { index, pair ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HudText("${records.size - pair.size + index + 1}.", color = HudMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(28.dp))
                    HudText(pair.getOrNull(0)?.notation.orEmpty(), color = HudBlue, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    HudText(pair.getOrNull(1)?.notation.orEmpty(), color = HudAmber, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FileLabels() {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        ('a'..'h').forEach { file ->
            HudText(file.toString(), color = HudMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun CurrentSessionPanel(uiState: ChessUiState) {
    val lastMove = uiState.gameState.history.lastOrNull()?.notation ?: "NO MOVES YET"
    HudText("CURRENT SESSION", color = HudMuted, style = MaterialTheme.typography.labelMedium)
    HudCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Avatar(initials = "M", color = HudGreen, size = 36.dp)
            Column(modifier = Modifier.weight(1f)) {
                HudText(
                    text = "${matchModeLabel(uiState)} - MOVE ${uiState.gameState.fullMoveNumber}",
                    color = HudText,
                    style = MaterialTheme.typography.bodyMedium,
                )
                HudText(
                    text = "LAST MOVE: $lastMove",
                    color = HudMuted,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            HudText(
                text = uiState.gameState.status.displayLabel,
                color = if (uiState.gameState.isTerminal) HudAmber else HudGreen,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
    if (uiState.hasSavedGame) {
        HudText("SAVED GAME AVAILABLE", color = HudGreen, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun AccountHudCard(
    uiState: ChessUiState,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {
    val profile = uiState.userProfile
    HudCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Avatar(
                initials = profile?.initials ?: "G",
                color = if (profile == null) HudViolet else HudBlue,
                size = 36.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                HudText(
                    text = profile?.displayName ?: "GUEST ACCESS",
                    color = HudText,
                    style = MaterialTheme.typography.bodyMedium,
                )
                HudText(
                    text = profile?.email ?: "OPTIONAL GOOGLE SESSION AVAILABLE",
                    color = HudMuted,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .border(0.5.dp, if (profile == null) HudViolet else HudBlue, RoundedCornerShape(4.dp))
                    .clickable { if (profile == null) onSignIn() else onSignOut() }
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
                HudText(
                    text = when {
                        uiState.isSigningIn -> "LINKING"
                        profile == null -> "SIGN IN"
                        else -> "SIGN OUT"
                    },
                    color = if (profile == null) HudViolet else HudBlue,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun Avatar(initials: String, color: Color, size: Dp, borderColor: Color = color) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        HudText(initials, color = color, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun StatsRow(uiState: ChessUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HudMetric("MOVE", uiState.gameState.fullMoveNumber.toString(), HudBlue, Modifier.weight(1f))
        HudMetric("MOVES", uiState.gameState.history.size.toString(), HudGreen, Modifier.weight(1f))
        HudMetric("MODE", matchModeShort(uiState), HudViolet, Modifier.weight(1f))
    }
}

@Composable
private fun AccountStateBadge(signedIn: Boolean) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val color = if (signedIn) HudGreen else HudAmber
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.14f))
                .border(0.5.dp, color.copy(alpha = 0.65f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            HudText(if (signedIn) "GOOGLE ACCOUNT LINKED" else "GUEST MODE", color = color, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun CurrentGameActivity(records: List<MoveRecord>) {
    if (records.isEmpty()) {
        HudCard {
            HudText("NO MOVES PLAYED IN CURRENT GAME", color = HudMuted, style = MaterialTheme.typography.bodySmall)
        }
    } else {
        records.takeLast(3).asReversed().forEach { record ->
            HudCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val color = if (record.color == PieceColor.WHITE) HudBlue else HudAmber
                    Avatar(initials = if (record.color == PieceColor.WHITE) "B" else "A", color = color, size = 34.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        HudText("${sideLabel(record.color)} MOVE", color = HudText, style = MaterialTheme.typography.bodyMedium)
                        HudText(record.notation, color = HudMuted, style = MaterialTheme.typography.labelSmall)
                    }
                    HudText(record.move.to.algebraic, color = color, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun SettingSection(title: String) {
    HudText(title, color = HudMuted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun HudSettingRow(label: String, subLabel: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            HudText(label, color = HudText, style = MaterialTheme.typography.bodyMedium)
            HudText(subLabel, color = HudMuted, style = MaterialTheme.typography.labelSmall)
        }
        HudSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
    HudDivider(compact = true)
}

@Composable
private fun HudSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val color = if (checked) HudBlue else HudMuted
    Box(
        modifier = Modifier
            .size(width = 42.dp, height = 24.dp)
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = if (checked) 0.18f else 0.08f))
            .border(1.dp, color.copy(alpha = 0.9f), RoundedCornerShape(50))
            .clickable { onCheckedChange(!checked) },
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(3.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(color),
        )
    }
}

@Composable
private fun DifficultyConfig(selected: AiDifficulty, onDifficulty: (AiDifficulty) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HudText("AI DIFFICULTY", color = HudText, style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AiDifficulty.entries.forEach { difficulty ->
                val active = selected == difficulty
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) HudViolet.copy(alpha = 0.15f) else Color.Transparent)
                        .border(0.5.dp, if (active) HudViolet else HudBorder, RoundedCornerShape(8.dp))
                        .clickable { onDifficulty(difficulty) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    HudText(difficulty.label, color = if (active) HudViolet else HudMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun MiniVoidGrid(modifier: Modifier = Modifier, size: Dp) {
    val colors = listOf(HudBlue, HudViolet, HudElevated, HudSurface)
    Column(
        modifier = modifier.size(size),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        repeat(4) { row ->
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(4) { col ->
                    val color = colors[(row * 2 + col) % colors.size]
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(color.copy(alpha = if (color == HudElevated || color == HudSurface) 0.8f else 0.95f))
                            .border(0.5.dp, HudBorder, RoundedCornerShape(2.dp)),
                    )
                }
            }
        }
    }
}

@Composable
private fun LivePulseDot(size: Dp) {
    PulsingDot(size = size, color = HudGreen, label = "livePulse")
}

@Composable
private fun CheckStatusSignal() {
    val transition = rememberInfiniteTransition(label = "checkStatusPulse")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1_000), repeatMode = RepeatMode.Reverse),
        label = "checkStatusAlpha",
    )
    PulsingDot(size = 6.dp, color = CheckRed, label = "checkStatusDot")
    Spacer(modifier = Modifier.width(7.dp))
    HudText("\u26A0 CHECK", color = CheckRed.copy(alpha = alpha), style = MaterialTheme.typography.labelSmall)
}

@Composable
private fun PulsingDot(size: Dp, color: Color, label: String) {
    val transition = rememberInfiniteTransition(label = label)
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(850), repeatMode = RepeatMode.Reverse),
        label = "${label}Alpha",
    )
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha)),
    )
}

@Composable
private fun HudDivider(compact: Boolean = false) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 5.dp else 12.dp),
    ) {
        val y = size.height / 2f
        drawLine(HudBorder, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        drawLine(HudBlue, Offset(0f, y), Offset(size.width * 0.30f, y), strokeWidth = 1.dp.toPx())
    }
}

@Composable
private fun CornerBrackets(modifier: Modifier = Modifier, inset: Dp = 0.dp, length: Dp = 16.dp) {
    Canvas(modifier = modifier) {
        val stroke = 1.5.dp.toPx()
        val i = inset.toPx()
        val l = length.toPx()
        val w = size.width
        val h = size.height
        drawLine(HudBlue, Offset(i, i), Offset(i + l, i), stroke)
        drawLine(HudBlue, Offset(i, i), Offset(i, i + l), stroke)
        drawLine(HudBlue, Offset(w - i, i), Offset(w - i - l, i), stroke)
        drawLine(HudBlue, Offset(w - i, i), Offset(w - i, i + l), stroke)
        drawLine(HudBlue, Offset(i, h - i), Offset(i + l, h - i), stroke)
        drawLine(HudBlue, Offset(i, h - i), Offset(i, h - i - l), stroke)
        drawLine(HudBlue, Offset(w - i, h - i), Offset(w - i - l, h - i), stroke)
        drawLine(HudBlue, Offset(w - i, h - i), Offset(w - i, h - i - l), stroke)
    }
}

@Composable
private fun ScanlineOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gap = 8.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = HudBlue.copy(alpha = 0.025f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
            )
            y += gap
        }
    }
}

@Composable
private fun HudText(
    text: String,
    color: Color,
    style: TextStyle,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text.uppercase(Locale.US),
        modifier = modifier,
        color = color,
        style = style.copy(letterSpacing = 2.sp, fontWeight = if (style.fontSize.value >= 17f) FontWeight.Medium else FontWeight.Normal),
        textAlign = textAlign,
    )
}

private fun currentTime(): String {
    return SimpleDateFormat("HH:mm", Locale.US).format(Date())
}

private val AiDifficulty.label: String
    get() = when (this) {
        AiDifficulty.EASY -> "EASY"
        AiDifficulty.MEDIUM -> "MEDIUM"
        AiDifficulty.HARD -> "HARD"
    }

private fun accountStatus(uiState: ChessUiState): String {
    return if (uiState.userProfile == null) "GUEST" else "SIGNED IN"
}

private fun opponentInitials(uiState: ChessUiState): String {
    return if (uiState.gameMode == GameMode.VS_AI) "AI" else uiState.localBlackIcon.ifBlank { "P2" }
}

private fun opponentName(uiState: ChessUiState): String {
    return if (uiState.gameMode == GameMode.VS_AI) {
        "AI OPPONENT"
    } else {
        uiState.localBlackName.ifBlank { "BLACK PLAYER" }
    }
}

private fun opponentSubtitle(uiState: ChessUiState): String {
    return if (uiState.gameMode == GameMode.VS_AI) {
        "${uiState.settings.aiDifficulty.label} AI - VIOLET FORCE"
    } else {
        "${uiState.localTimeControl.label} - VIOLET FORCE"
    }
}

private fun whitePlayerInitials(uiState: ChessUiState): String {
    return if (uiState.gameMode == GameMode.VS_AI) {
        uiState.userProfile?.initials ?: "P1"
    } else {
        uiState.localWhiteIcon.ifBlank { "P1" }
    }
}

private fun whitePlayerName(uiState: ChessUiState): String {
    return if (uiState.gameMode == GameMode.VS_AI) {
        uiState.userProfile?.displayName ?: "WHITE PLAYER"
    } else {
        uiState.localWhiteName.ifBlank { "WHITE PLAYER" }
    }
}

private fun whitePlayerSubtitle(uiState: ChessUiState): String {
    return if (uiState.gameMode == GameMode.VS_AI) {
        uiState.userProfile?.email ?: "LOCAL GUEST - CYAN FORCE"
    } else {
        "${uiState.localTimeControl.label} - CYAN FORCE"
    }
}

private fun checkedPlayerColor(uiState: ChessUiState): PieceColor? {
    return if (uiState.gameState.status == GameStatus.CHECK || uiState.gameState.status == GameStatus.CHECKMATE) {
        uiState.gameState.sideToMove
    } else {
        null
    }
}

private fun playerCardStatus(uiState: ChessUiState, color: PieceColor): String {
    return if (uiState.gameMode == GameMode.LOCAL_PLAYER && uiState.localTimeControl.durationMillis != null) {
        formatClock(if (color == PieceColor.WHITE) uiState.whiteClockMillis else uiState.blackClockMillis)
    } else {
        playerTurnStatus(uiState, color)
    }
}

private fun formatClock(millis: Long): String {
    val totalSeconds = (millis / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(Locale.US, minutes, seconds)
}

private fun playerTurnStatus(uiState: ChessUiState, color: PieceColor): String {
    return if (uiState.gameState.sideToMove == color) "TO MOVE" else "WAIT"
}

private fun matchModeLabel(uiState: ChessUiState): String {
    return if (uiState.gameMode == GameMode.VS_AI) {
        "PLAYER VS ${uiState.settings.aiDifficulty.label} AI"
    } else {
        "LOCAL PLAYER VS PLAYER"
    }
}

private fun matchModeShort(uiState: ChessUiState): String {
    return if (uiState.gameMode == GameMode.VS_AI) "AI" else "LOCAL"
}

private fun sideLabel(color: PieceColor): String {
    return if (color == PieceColor.WHITE) "GOLD" else "BLACK"
}

private val GameStatus.displayLabel: String
    get() = when (this) {
        GameStatus.ONGOING -> "ACTIVE"
        GameStatus.CHECK -> "CHECK"
        GameStatus.CHECKMATE -> "MATE"
        GameStatus.STALEMATE -> "STALEMATE"
        GameStatus.DRAW_INSUFFICIENT_MATERIAL -> "DRAW"
    }
