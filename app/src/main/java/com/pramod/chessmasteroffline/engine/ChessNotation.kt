package com.pramod.chessmasteroffline.engine

object ChessNotation {
    fun format(
        before: GameState,
        move: Move,
        after: GameState,
        movedPiece: Piece,
        captured: Piece?,
    ): String {
        val suffix = when (after.status) {
            GameStatus.CHECKMATE -> "#"
            GameStatus.CHECK -> "+"
            else -> ""
        }

        if (movedPiece.type == PieceType.KING && kotlin.math.abs(move.to.col - move.from.col) == 2) {
            return if (move.to.col == 6) "O-O$suffix" else "O-O-O$suffix"
        }

        val isEnPassantCapture = movedPiece.type == PieceType.PAWN &&
            before.enPassantTarget == move.to &&
            before.board[move.to] == null &&
            move.from.col != move.to.col

        val separator = if (captured != null || isEnPassantCapture) "x" else "-"
        val promotion = move.promotion?.let { "=${it.notationLetter}" }.orEmpty()
        val piecePrefix = movedPiece.type.notationLetter
        return "$piecePrefix${move.from.algebraic}$separator${move.to.algebraic}$promotion$suffix"
    }
}
