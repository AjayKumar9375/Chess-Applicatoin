package com.pramod.chessmasteroffline.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pramod.chessmasteroffline.BuildConfig
import com.pramod.chessmasteroffline.data.AiDifficulty
import com.pramod.chessmasteroffline.data.BoardTheme
import com.pramod.chessmasteroffline.data.GameMode
import com.pramod.chessmasteroffline.data.PieceStyle
import com.pramod.chessmasteroffline.engine.MoveRecord
import com.pramod.chessmasteroffline.engine.PieceType
import com.pramod.chessmasteroffline.ui.ChessUiState
import com.pramod.chessmasteroffline.ui.components.boardStatusText
import com.pramod.chessmasteroffline.ui.components.ChessBoard
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(contentPadding: PaddingValues, onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(850)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF102326), Color(0xFF163C3A), Color(0xFF101817)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "\u265E", color = Color(0xFFF6C96D), fontSize = 78.sp)
            Text(
                text = "Chess Master Offline",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun HomeScreen(
    uiState: ChessUiState,
    contentPadding: PaddingValues,
    onNewGame: () -> Unit,
    onResume: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    onPremium: () -> Unit,
) {
    ScreenColumn(contentPadding = contentPadding) {
        Spacer(modifier = Modifier.height(18.dp))
        Text(text = "\u265E", fontSize = 58.sp, color = MaterialTheme.colorScheme.secondary)
        Text(
            text = "Chess Master Offline",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        HomePreviewBoard(modifier = Modifier.padding(vertical = 12.dp))
        MenuButton(text = "New Game", icon = { Icon(Icons.Default.PlayArrow, null) }, onClick = onNewGame)
        if (uiState.hasSavedGame) {
            MenuButton(text = "Resume Last Game", icon = { Icon(Icons.Default.Refresh, null) }, onClick = onResume)
        }
        MenuButton(text = "Settings", icon = { Icon(Icons.Default.Settings, null) }, onClick = onSettings)
        MenuButton(text = "Premium", icon = { Icon(Icons.Default.Star, null) }, onClick = onPremium)
        MenuButton(text = "About", icon = { Icon(Icons.Default.Info, null) }, onClick = onAbout)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Offline | No login | No permissions",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun NewGameScreen(
    uiState: ChessUiState,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onModeSelected: (GameMode) -> Unit,
    onDifficultySelected: (AiDifficulty) -> Unit,
    onStart: () -> Unit,
) {
    val view = LocalView.current
    ScreenColumn(contentPadding = contentPadding) {
        Header(title = "New Game", onBack = onBack)
        SettingGroup(title = "Mode") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChoiceChip("Player vs Player", uiState.newGameMode == GameMode.LOCAL_PLAYER) {
                    onModeSelected(GameMode.LOCAL_PLAYER)
                }
                ChoiceChip("Player vs AI", uiState.newGameMode == GameMode.VS_AI) {
                    onModeSelected(GameMode.VS_AI)
                }
            }
        }

        if (uiState.newGameMode == GameMode.VS_AI) {
            SettingGroup(title = "AI Difficulty") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AiDifficulty.entries.forEach { difficulty ->
                        ChoiceChip(difficulty.label, uiState.newGameDifficulty == difficulty) {
                            onDifficultySelected(difficulty)
                        }
                    }
                }
                Text(
                    text = uiState.newGameDifficulty.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onStart()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(6.dp),
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Game")
        }
    }
}

@Composable
fun BoardScreen(
    uiState: ChessUiState,
    contentPadding: PaddingValues,
    onBackHome: () -> Unit,
    onSquareTapped: (com.pramod.chessmasteroffline.engine.Square) -> Unit,
    onPromotion: (PieceType) -> Unit,
    onUndo: () -> Unit,
    onRestart: () -> Unit,
    onSave: () -> Unit,
    onSettings: () -> Unit,
) {
    val tone = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 48) }
    DisposableEffect(Unit) {
        onDispose { tone.release() }
    }
    LaunchedEffect(uiState.moveSoundTick) {
        if (uiState.moveSoundTick > 0 && uiState.settings.soundEnabled) {
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        }
    }

    ScreenColumn(contentPadding = contentPadding, scrollable = true) {
        Header(
            title = if (uiState.gameMode == GameMode.VS_AI) "Vs AI" else "Local Game",
            onBack = onBackHome,
            trailing = {
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            },
        )
        GameStatusPanel(
            uiState = uiState,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))
        ChessBoard(
            state = uiState.gameState,
            selectedSquare = uiState.selectedSquare,
            legalTargets = uiState.legalTargets,
            boardTheme = uiState.settings.boardTheme,
            pieceStyle = uiState.settings.pieceStyle,
            onSquareTapped = onSquareTapped,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 430.dp)
                .aspectRatio(1f)
                .align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BoardActionButton("Undo", Icons.AutoMirrored.Filled.Undo, onUndo, Modifier.weight(1f))
            BoardActionButton("Save", Icons.Default.Save, onSave, Modifier.weight(1f))
            BoardActionButton("Restart", Icons.Default.Refresh, onRestart, Modifier.weight(1f))
        }
        MoveHistory(records = uiState.gameState.history)
    }

    val pendingPromotion = uiState.pendingPromotion
    if (pendingPromotion != null) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Promote pawn") },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pendingPromotion.choices.forEach { choice ->
                        OutlinedButton(onClick = { onPromotion(choice) }, shape = RoundedCornerShape(6.dp)) {
                            Text(choice.notationLetter, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
        )
    }
}

@Composable
fun SettingsScreen(
    uiState: ChessUiState,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onBoardTheme: (BoardTheme) -> Unit,
    onPieceStyle: (PieceStyle) -> Unit,
    onSound: (Boolean) -> Unit,
    onDifficulty: (AiDifficulty) -> Unit,
    onResetGame: () -> Unit,
    onClearSaved: () -> Unit,
    onResetSettings: () -> Unit,
) {
    ScreenColumn(contentPadding = contentPadding, scrollable = true) {
        Header(title = "Settings", onBack = onBack)
        SettingGroup(title = "Board Theme") {
            WrappingRow {
                BoardTheme.entries.forEach { theme ->
                    ChoiceChip(theme.label, uiState.settings.boardTheme == theme) { onBoardTheme(theme) }
                }
            }
        }
        SettingGroup(title = "Piece Style") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PieceStyle.entries.forEach { style ->
                    ChoiceChip(style.label, uiState.settings.pieceStyle == style) { onPieceStyle(style) }
                }
            }
        }
        ToggleRow("Sound", uiState.settings.soundEnabled, onSound)
        SettingGroup(title = "AI Difficulty") {
            WrappingRow {
                AiDifficulty.entries.forEach { difficulty ->
                    ChoiceChip(difficulty.label, uiState.settings.aiDifficulty == difficulty) {
                        onDifficulty(difficulty)
                    }
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        OutlinedButton(onClick = onResetGame, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp)) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset Game")
        }
        OutlinedButton(onClick = onClearSaved, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(6.dp)) {
            Text("Clear Saved Game")
        }
        TextButton(onClick = onResetSettings, modifier = Modifier.fillMaxWidth()) {
            Text("Reset Settings")
        }
    }
}

@Composable
fun AboutScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    ScreenColumn(contentPadding = contentPadding, scrollable = true) {
        Header(title = "About", onBack = onBack)
        Text(
            text = "Chess Master Offline",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text("Version ${BuildConfig.VERSION_NAME}")
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        Text(
            text = "A privacy-friendly offline chess game with local play, AI opponents, move history, undo, and saved games.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "The app stores settings and game state on this device. It does not require login, internet, location, contacts, camera, microphone, or storage permissions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun PremiumScreen(contentPadding: PaddingValues, onBack: () -> Unit) {
    ScreenColumn(contentPadding = contentPadding) {
        Header(title = "Premium", onBack = onBack)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Premium features", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Reserved for future releases.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Planned: advanced themes, deeper analysis, and training packs.")
            }
        }
    }
}

@Composable
private fun HomePreviewBoard(modifier: Modifier = Modifier) {
    val light = Color(0xFFE9D6AD)
    val dark = Color(0xFF8B5E3C)
    val pieces = mapOf(
        0 to "\u265C",
        3 to "\u265A",
        12 to "\u2654",
        15 to "\u2656",
    )

    Column(
        modifier = modifier
            .size(118.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f), RoundedCornerShape(6.dp)),
    ) {
        repeat(4) { row ->
            Row(modifier = Modifier.weight(1f)) {
                repeat(4) { col ->
                    val index = row * 4 + col
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(if ((row + col) % 2 == 0) light else dark),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = pieces[index].orEmpty(),
                            fontSize = 22.sp,
                            color = if (index == 0 || index == 3) Color(0xFF151918) else Color(0xFFFFF8E7),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameStatusPanel(uiState: ChessUiState, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.isAiThinking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.secondary,
                )
            } else {
                Text(text = "\u265F", fontSize = 24.sp, color = MaterialTheme.colorScheme.secondary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = boardStatusText(uiState.gameState, uiState.isAiThinking),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (uiState.gameMode == GameMode.VS_AI) {
                        "You: White | AI: Black (${uiState.settings.aiDifficulty.label})"
                    } else {
                        "Two-player game on this device"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ScreenColumn(
    contentPadding: PaddingValues,
    scrollable: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val modifier = Modifier
        .fillMaxSize()
        .padding(contentPadding)
        .background(
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                    MaterialTheme.colorScheme.background,
                ),
            ),
        )
        .padding(horizontal = 18.dp, vertical = 14.dp)
        .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun Header(
    title: String,
    onBack: (() -> Unit)?,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        if (trailing != null) {
            trailing()
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
private fun MenuButton(text: String, icon: @Composable () -> Unit, onClick: () -> Unit) {
    val view = LocalView.current
    ElevatedButton(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f),
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        icon()
        Spacer(modifier = Modifier.width(10.dp))
        Text(text)
    }
}

@Composable
private fun BoardActionButton(
    text: String,
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    OutlinedButton(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onClick()
        },
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        Icon(imageVector, contentDescription = text, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 13.sp)
    }
}

@Composable
private fun MoveHistory(records: List<MoveRecord>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Move History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(138.dp),
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        ) {
            if (records.isEmpty()) {
                Box(contentAlignment = Alignment.Center) {
                    Text("No moves yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val pairs = records.chunked(2)
                LazyColumn(contentPadding = PaddingValues(10.dp)) {
                    items(pairs.size) { index ->
                        val white = pairs[index].getOrNull(0)
                        val black = pairs[index].getOrNull(1)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text("${index + 1}.", modifier = Modifier.width(30.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(white?.notation.orEmpty(), modifier = Modifier.weight(1f))
                            Text(black?.notation.orEmpty(), modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingGroup(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val view = LocalView.current
    FilterChip(
        selected = selected,
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onClick()
        },
        label = { Text(label) },
        shape = RoundedCornerShape(6.dp),
    )
}

@Composable
private fun WrappingRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

private val AiDifficulty.label: String
    get() = when (this) {
        AiDifficulty.EASY -> "Easy"
        AiDifficulty.MEDIUM -> "Medium"
        AiDifficulty.HARD -> "Hard"
    }

private val AiDifficulty.description: String
    get() = when (this) {
        AiDifficulty.EASY -> "Fast casual replies with random legal moves."
        AiDifficulty.MEDIUM -> "Looks for material gains and safer captures."
        AiDifficulty.HARD -> "Searches deeper and takes a moment before moving."
    }

private val BoardTheme.label: String
    get() = when (this) {
        BoardTheme.CLASSIC -> "Classic"
        BoardTheme.OCEAN -> "Ocean"
        BoardTheme.SLATE -> "Slate"
    }

private val PieceStyle.label: String
    get() = when (this) {
        PieceStyle.CLASSIC -> "Classic"
        PieceStyle.MINIMAL -> "Minimal"
    }

