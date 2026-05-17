package com.pramod.chessmasteroffline.engine

object Fen {
    const val STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

    fun fromFen(fen: String = STARTING_FEN, history: List<MoveRecord> = emptyList()): GameState {
        val parts = fen.trim().split(Regex("\\s+"))
        require(parts.size >= 4) { "FEN must include board, side, castling, and en passant fields." }

        val board = mutableMapOf<Square, Piece>()
        val ranks = parts[0].split("/")
        require(ranks.size == 8) { "FEN board must contain 8 ranks." }

        ranks.forEachIndexed { row, rank ->
            var col = 0
            rank.forEach { char ->
                when {
                    char.isDigit() -> col += char.digitToInt()
                    else -> {
                        val type = PieceType.fromFen(char) ?: error("Unknown FEN piece: $char")
                        val color = if (char.isUpperCase()) PieceColor.WHITE else PieceColor.BLACK
                        board[Square(row, col)] = Piece(type, color)
                        col++
                    }
                }
            }
            require(col == 8) { "Every FEN rank must contain 8 files." }
        }

        val sideToMove = when (parts[1]) {
            "w" -> PieceColor.WHITE
            "b" -> PieceColor.BLACK
            else -> error("Invalid side to move in FEN: ${parts[1]}")
        }

        val castling = parts[2]
        val rights = CastlingRights(
            whiteKingSide = castling.contains('K'),
            whiteQueenSide = castling.contains('Q'),
            blackKingSide = castling.contains('k'),
            blackQueenSide = castling.contains('q'),
        )

        val enPassant = parts[3].takeUnless { it == "-" }?.let {
            Square.fromAlgebraic(it) ?: error("Invalid en passant square in FEN: $it")
        }

        return GameState(
            board = board,
            sideToMove = sideToMove,
            castlingRights = rights,
            enPassantTarget = enPassant,
            halfMoveClock = parts.getOrNull(4)?.toIntOrNull() ?: 0,
            fullMoveNumber = parts.getOrNull(5)?.toIntOrNull() ?: 1,
            history = history,
        )
    }

    fun toFen(state: GameState): String {
        val boardPart = (0..7).joinToString("/") { row ->
            buildString {
                var empty = 0
                for (col in 0..7) {
                    val piece = state.board[Square(row, col)]
                    if (piece == null) {
                        empty++
                    } else {
                        if (empty > 0) {
                            append(empty)
                            empty = 0
                        }
                        append(piece.fenChar())
                    }
                }
                if (empty > 0) append(empty)
            }
        }
        val side = if (state.sideToMove == PieceColor.WHITE) "w" else "b"
        val enPassant = state.enPassantTarget?.algebraic ?: "-"
        return "$boardPart $side ${state.castlingRights.toFen()} $enPassant ${state.halfMoveClock} ${state.fullMoveNumber}"
    }
}
