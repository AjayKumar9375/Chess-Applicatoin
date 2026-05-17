package com.pramod.chessmasteroffline.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pramod.chessmasteroffline.data.BoardTheme
import com.pramod.chessmasteroffline.data.PieceStyle
import com.pramod.chessmasteroffline.engine.GameState
import com.pramod.chessmasteroffline.engine.GameStatus
import com.pramod.chessmasteroffline.engine.Piece
import com.pramod.chessmasteroffline.engine.PieceColor
import com.pramod.chessmasteroffline.engine.PieceType
import com.pramod.chessmasteroffline.engine.Square

data class BoardPalette(
    val light: Color,
    val dark: Color,
    val selected: Color,
    val legal: Color,
    val lastMove: Color,
    val check: Color,
)

@Composable
fun ChessBoard(
    state: GameState,
    selectedSquare: Square?,
    legalTargets: Set<Square>,
    boardTheme: BoardTheme,
    pieceStyle: PieceStyle,
    onSquareTapped: (Square) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = boardPalette(boardTheme)
    val lastMoveSquares = state.lastMove?.let { setOf(it.from, it.to) }.orEmpty()
    val selectedPiece = selectedSquare?.let { state.board[it] }
    val checkedKingSquare = if (state.status == GameStatus.CHECK || state.status == GameStatus.CHECKMATE) {
        state.board.entries.firstOrNull {
            it.value.type == PieceType.KING && it.value.color == state.sideToMove
        }?.key
    } else {
        null
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f), RoundedCornerShape(6.dp)),
    ) {
        for (row in 0..7) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0..7) {
                    val square = Square(row, col)
                    BoardSquare(
                        square = square,
                        piece = state.board[square],
                        palette = palette,
                        pieceStyle = pieceStyle,
                        isSelected = selectedSquare == square,
                        isLegalTarget = square in legalTargets,
                        isPawnTarget = selectedPiece?.type == PieceType.PAWN && square in legalTargets,
                        isLastMove = square in lastMoveSquares,
                        isCheck = checkedKingSquare == square,
                        onTap = { onSquareTapped(square) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun BoardSquare(
    square: Square,
    piece: Piece?,
    palette: BoardPalette,
    pieceStyle: PieceStyle,
    isSelected: Boolean,
    isLegalTarget: Boolean,
    isPawnTarget: Boolean,
    isLastMove: Boolean,
    isCheck: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val base = if ((square.row + square.col) % 2 == 0) palette.light else palette.dark
    val targetColor = when {
        isCheck -> palette.check
        isSelected -> palette.selected
        isLastMove -> palette.lastMove
        else -> base
    }
    val color by animateColorAsState(targetValue = targetColor, label = "squareColor")
    val scale by animateFloatAsState(targetValue = if (isSelected) 1.08f else 1f, label = "pieceScale")
    val view = LocalView.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onTap()
            },
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(3.dp, palette.selected.copy(alpha = 0.95f)),
            )
        }

        if (isLegalTarget) {
            val dotColor = if (isPawnTarget) Color(0xFFF6C96D) else palette.legal
            val indicatorModifier = if (piece == null) {
                Modifier
                    .size(if (isPawnTarget) 20.dp else 15.dp)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = 0.92f))
            } else {
                Modifier
                    .size(if (isPawnTarget) 52.dp else 46.dp)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = 0.16f))
                    .border(3.dp, dotColor.copy(alpha = 0.92f), CircleShape)
            }
            Box(
                modifier = indicatorModifier,
            )
        }

        if (piece != null) {
            ChessPiece(piece = piece, style = pieceStyle, modifier = Modifier.scale(scale))
        }
    }
}

@Composable
private fun ChessPiece(piece: Piece, style: PieceStyle, modifier: Modifier = Modifier) {
    when (style) {
        PieceStyle.CLASSIC -> {
            Text(
                text = piece.type.symbolFor(piece.color),
                modifier = modifier,
                color = if (piece.color == PieceColor.WHITE) Color(0xFFFFF8E7) else Color(0xFF151918),
                fontSize = 34.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        PieceStyle.MINIMAL -> {
            val background = if (piece.color == PieceColor.WHITE) Color(0xFFFFF8E7) else Color(0xFF18201F)
            val foreground = if (piece.color == PieceColor.WHITE) Color(0xFF18201F) else Color(0xFFFFF8E7)
            Surface(
                modifier = modifier.size(38.dp),
                shape = CircleShape,
                color = background,
                shadowElevation = 1.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = piece.type.notationLetter.ifBlank { "P" },
                        color = foreground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                }
            }
        }
    }
}

private fun boardPalette(theme: BoardTheme): BoardPalette {
    return when (theme) {
        BoardTheme.CLASSIC -> BoardPalette(
            light = Color(0xFFE9D6AD),
            dark = Color(0xFF8B5E3C),
            selected = Color(0xFFF6C96D),
            legal = Color(0xFF126B5B),
            lastMove = Color(0xFFC9DB75),
            check = Color(0xFFE85D55),
        )
        BoardTheme.OCEAN -> BoardPalette(
            light = Color(0xFFDCE8E6),
            dark = Color(0xFF2E6D73),
            selected = Color(0xFFF2C14E),
            legal = Color(0xFF1B998B),
            lastMove = Color(0xFFA7D8DE),
            check = Color(0xFFE85D75),
        )
        BoardTheme.SLATE -> BoardPalette(
            light = Color(0xFFCFD7D2),
            dark = Color(0xFF4A5658),
            selected = Color(0xFFE7B75F),
            legal = Color(0xFF6CC6B8),
            lastMove = Color(0xFF9DB57D),
            check = Color(0xFFE0604C),
        )
    }
}

fun boardStatusText(state: GameState, isAiThinking: Boolean): String {
    if (isAiThinking) return "AI is thinking..."
    val side = if (state.sideToMove == PieceColor.WHITE) "White" else "Black"
    return when (state.status) {
        GameStatus.ONGOING -> "$side to move"
        GameStatus.CHECK -> "$side is in check"
        GameStatus.CHECKMATE -> "${if (state.sideToMove == PieceColor.WHITE) "Black" else "White"} wins by checkmate"
        GameStatus.STALEMATE -> "Draw by stalemate"
        GameStatus.DRAW_INSUFFICIENT_MATERIAL -> "Draw by insufficient material"
    }
}
