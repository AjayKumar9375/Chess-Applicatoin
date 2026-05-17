package com.pramod.chessmasteroffline.engine

enum class PieceColor {
    WHITE,
    BLACK;

    val opposite: PieceColor
        get() = if (this == WHITE) BLACK else WHITE

    val pawnDirection: Int
        get() = if (this == WHITE) -1 else 1
}

enum class PieceType(
    val fenChar: Char,
    val materialValue: Int,
    val notationLetter: String,
    val whiteSymbol: String,
    val blackSymbol: String,
) {
    KING('k', 20_000, "K", "\u2654", "\u265A"),
    QUEEN('q', 900, "Q", "\u2655", "\u265B"),
    ROOK('r', 500, "R", "\u2656", "\u265C"),
    BISHOP('b', 330, "B", "\u2657", "\u265D"),
    KNIGHT('n', 320, "N", "\u2658", "\u265E"),
    PAWN('p', 100, "", "\u2659", "\u265F");

    fun symbolFor(color: PieceColor): String = if (color == PieceColor.WHITE) whiteSymbol else blackSymbol

    companion object {
        val promotionChoices = listOf(QUEEN, ROOK, BISHOP, KNIGHT)

        fun fromFen(char: Char): PieceType? {
            val lower = char.lowercaseChar()
            return entries.firstOrNull { it.fenChar == lower }
        }
    }
}

data class Piece(
    val type: PieceType,
    val color: PieceColor,
) {
    fun fenChar(): Char {
        val char = type.fenChar
        return if (color == PieceColor.WHITE) char.uppercaseChar() else char
    }
}

data class Square(
    val row: Int,
    val col: Int,
) {
    init {
        require(row in 0..7 && col in 0..7) { "Square must be inside the chess board." }
    }

    val algebraic: String
        get() = "${('a'.code + col).toChar()}${8 - row}"

    companion object {
        fun isInside(row: Int, col: Int): Boolean = row in 0..7 && col in 0..7

        fun orNull(row: Int, col: Int): Square? = if (isInside(row, col)) Square(row, col) else null

        fun fromAlgebraic(value: String): Square? {
            if (value.length != 2) return null
            val file = value[0].lowercaseChar()
            val rank = value[1]
            if (file !in 'a'..'h' || rank !in '1'..'8') return null
            return Square(8 - rank.digitToInt(), file.code - 'a'.code)
        }
    }
}

data class Move(
    val from: Square,
    val to: Square,
    val promotion: PieceType? = null,
)

data class CastlingRights(
    val whiteKingSide: Boolean = true,
    val whiteQueenSide: Boolean = true,
    val blackKingSide: Boolean = true,
    val blackQueenSide: Boolean = true,
) {
    fun toFen(): String {
        val value = buildString {
            if (whiteKingSide) append('K')
            if (whiteQueenSide) append('Q')
            if (blackKingSide) append('k')
            if (blackQueenSide) append('q')
        }
        return value.ifEmpty { "-" }
    }
}

enum class GameStatus {
    ONGOING,
    CHECK,
    CHECKMATE,
    STALEMATE,
    DRAW_INSUFFICIENT_MATERIAL,
}

data class MoveRecord(
    val color: PieceColor,
    val move: Move,
    val piece: Piece,
    val captured: Piece?,
    val notation: String,
    val fenBefore: String,
    val fenAfter: String,
)

data class GameState(
    val board: Map<Square, Piece>,
    val sideToMove: PieceColor,
    val castlingRights: CastlingRights,
    val enPassantTarget: Square?,
    val halfMoveClock: Int,
    val fullMoveNumber: Int,
    val status: GameStatus = GameStatus.ONGOING,
    val lastMove: Move? = null,
    val history: List<MoveRecord> = emptyList(),
) {
    fun pieceAt(square: Square): Piece? = board[square]

    val isTerminal: Boolean
        get() = status == GameStatus.CHECKMATE ||
            status == GameStatus.STALEMATE ||
            status == GameStatus.DRAW_INSUFFICIENT_MATERIAL
}
