package com.pramod.chessmasteroffline.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pramod.chessmasteroffline.engine.GameState
import com.pramod.chessmasteroffline.engine.GameStatus
import com.pramod.chessmasteroffline.engine.Piece
import com.pramod.chessmasteroffline.engine.PieceColor
import com.pramod.chessmasteroffline.engine.PieceType
import com.pramod.chessmasteroffline.engine.Square
import com.pramod.chessmasteroffline.ui.theme.HudBlue
import com.pramod.chessmasteroffline.ui.theme.HudBorder
import com.pramod.chessmasteroffline.ui.theme.JetBrainsMono

@Composable
fun ChessBoard(
    state: GameState,
    selectedSquare: Square?,
    legalTargets: Set<Square>,
    pieceGlowEnabled: Boolean,
    hapticFeedbackEnabled: Boolean,
    onSquareTapped: (Square) -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val checkPulse by rememberInfiniteTransition(label = "checkSquarePulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "checkSquarePulseValue",
    )
    val checkPulseColor = lerp(Color(0xFFFF1744), Color(0xFF8B0000), checkPulse)
    val lastMoveSquares = state.lastMove?.let { setOf(it.from, it.to) }.orEmpty()
    val checkedKingSquare = if (state.status == GameStatus.CHECK || state.status == GameStatus.CHECKMATE) {
        state.board.entries.firstOrNull {
            it.value.type == PieceType.KING && it.value.color == state.sideToMove
        }?.key
    } else {
        null
    }

    BoxWithConstraints(modifier = modifier) {
        val cell = maxWidth / 8
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellPx = size.width / 8f
            for (row in 0..7) {
                for (col in 0..7) {
                    val square = Square(row, col)
                    val baseColor = if ((row + col) % 2 == 0) {
                        Color(0xFFF0D9B5)
                    } else {
                        Color(0xFFB58863)
                    }
                    val topLeft = Offset(col * cellPx, row * cellPx)
                    drawRect(
                        color = if (checkedKingSquare == square) checkPulseColor else baseColor,
                        topLeft = topLeft,
                        size = Size(cellPx, cellPx),
                    )
                    if (square in lastMoveSquares && checkedKingSquare != square) {
                        drawRect(
                            color = Color(0xFF3B2413).copy(alpha = 0.14f),
                            topLeft = topLeft,
                            size = Size(cellPx, cellPx),
                        )
                    }
                    if (selectedSquare == square && checkedKingSquare != square) {
                        drawRect(
                            color = HudBlue.copy(alpha = 0.16f),
                            topLeft = topLeft,
                            size = Size(cellPx, cellPx),
                        )
                        drawRect(
                            color = HudBlue.copy(alpha = 0.92f),
                            topLeft = topLeft + Offset(2.dp.toPx(), 2.dp.toPx()),
                            size = Size(cellPx - 4.dp.toPx(), cellPx - 4.dp.toPx()),
                            style = Stroke(width = 2.dp.toPx()),
                        )
                    }
                    if (checkedKingSquare == square) {
                        drawRect(
                            color = Color(0xFFFF1744),
                            topLeft = topLeft + Offset(1.dp.toPx(), 1.dp.toPx()),
                            size = Size(cellPx - 2.dp.toPx(), cellPx - 2.dp.toPx()),
                            style = Stroke(width = 2.dp.toPx()),
                        )
                    }
                    drawRect(
                        color = HudBorder,
                        topLeft = topLeft,
                        size = Size(cellPx, cellPx),
                        style = Stroke(width = 0.6.dp.toPx()),
                    )
                    if (square in legalTargets) {
                        val center = topLeft + Offset(cellPx / 2f, cellPx / 2f)
                        val targetPiece = state.board[square]
                        if (targetPiece == null) {
                            drawCircle(
                                color = Color(0xFF22140A).copy(alpha = 0.58f),
                                radius = cellPx * 0.11f,
                                center = center,
                            )
                        } else {
                            drawCircle(
                                color = Color(0xFF22140A).copy(alpha = 0.86f),
                                radius = cellPx * 0.32f,
                                center = center,
                                style = Stroke(width = 2.dp.toPx()),
                            )
                        }
                    }
                }
            }
        }

        for (row in 0..7) {
            for (col in 0..7) {
                val square = Square(row, col)
                val piece = state.board[square]
                Box(
                    modifier = Modifier
                        .offset(x = cell * col, y = cell * row)
                        .size(cell)
                        .clickable {
                            if (hapticFeedbackEnabled) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            }
                            onSquareTapped(square)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (piece != null) {
                        HudPiece(
                            piece = piece,
                            glowEnabled = pieceGlowEnabled,
                            selected = selectedSquare == square,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HudPiece(piece: Piece, glowEnabled: Boolean, selected: Boolean) {
    val pieceColor = if (piece.color == PieceColor.WHITE) PieceGold else PieceBlack
    val outlineColor = if (piece.color == PieceColor.WHITE) PieceBlack else PieceGold
    val offsets = listOf(
        (-1.35f).dp to 0.dp,
        1.35f.dp to 0.dp,
        0.dp to (-1.35f).dp,
        0.dp to 1.35f.dp,
        (-0.95f).dp to (-0.95f).dp,
        0.95f.dp to (-0.95f).dp,
        (-0.95f).dp to 0.95f.dp,
        0.95f.dp to 0.95f.dp,
    )
    Box(contentAlignment = Alignment.Center) {
        offsets.forEach { (xOffset, yOffset) ->
            Text(
                text = piece.type.symbolFor(piece.color),
                modifier = Modifier.offset(x = xOffset, y = yOffset),
                color = outlineColor.copy(alpha = 0.95f),
                fontFamily = JetBrainsMono,
                fontSize = 34.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Text(
            text = piece.type.symbolFor(piece.color),
            color = pieceColor,
            fontFamily = JetBrainsMono,
            fontSize = 34.sp,
            fontWeight = FontWeight.Medium,
            style = TextStyle(
                shadow = Shadow(
                    color = outlineColor.copy(alpha = 0.65f),
                    offset = Offset(0f, 0f),
                    blurRadius = 7f,
                ),
            ),
        )
    }
}

private val PieceGold = Color(0xFFFFE082)
private val PieceBlack = Color(0xFF050505)

fun boardStatusText(state: GameState, isAiThinking: Boolean): String {
    if (isAiThinking) return "AI SEARCHING..."
    val side = if (state.sideToMove == PieceColor.WHITE) "GOLD" else "BLACK"
    return when (state.status) {
        GameStatus.ONGOING -> "$side TO MOVE"
        GameStatus.CHECK -> "$side KING IN CHECK"
        GameStatus.CHECKMATE -> "${if (state.sideToMove == PieceColor.WHITE) "BLACK" else "GOLD"} WINS BY CHECKMATE"
        GameStatus.STALEMATE -> "DRAW BY STALEMATE"
        GameStatus.DRAW_INSUFFICIENT_MATERIAL -> "DRAW: INSUFFICIENT MATERIAL"
    }
}
