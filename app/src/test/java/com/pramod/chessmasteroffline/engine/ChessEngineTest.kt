package com.pramod.chessmasteroffline.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChessEngineTest {
    @Test
    fun initialPositionHasTwentyLegalMoves() {
        val state = ChessEngine.initialState()

        assertEquals(20, ChessEngine.legalMoves(state).size)
        assertEquals(GameStatus.ONGOING, state.status)
    }

    @Test
    fun rejectsMoveThatExposesOwnKing() {
        val state = ChessEngine.stateFromFen("k3r3/8/8/8/8/8/4R3/4K3 w - - 0 1")
        val illegalPinnedMove = Move(Square.fromAlgebraic("e2")!!, Square.fromAlgebraic("d2")!!)

        assertEquals(GameStatus.ONGOING, state.status)
        assertNull(ChessEngine.makeMove(state, illegalPinnedMove))
    }

    @Test
    fun castlingMovesKingAndRookAndClearsRights() {
        val state = ChessEngine.stateFromFen("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")
        val castle = Move(Square.fromAlgebraic("e1")!!, Square.fromAlgebraic("g1")!!)

        val next = ChessEngine.makeMove(state, castle)

        assertNotNull(next)
        next!!
        assertEquals(Piece(PieceType.KING, PieceColor.WHITE), next.board[Square.fromAlgebraic("g1")])
        assertEquals(Piece(PieceType.ROOK, PieceColor.WHITE), next.board[Square.fromAlgebraic("f1")])
        assertFalse(next.castlingRights.whiteKingSide)
        assertFalse(next.castlingRights.whiteQueenSide)
    }

    @Test
    fun enPassantCapturesPawnBehindTargetSquare() {
        val state = ChessEngine.stateFromFen("7k/8/8/3pP3/8/8/8/4K3 w - d6 0 1")
        val enPassant = Move(Square.fromAlgebraic("e5")!!, Square.fromAlgebraic("d6")!!)

        val next = ChessEngine.makeMove(state, enPassant)

        assertNotNull(next)
        next!!
        assertEquals(Piece(PieceType.PAWN, PieceColor.WHITE), next.board[Square.fromAlgebraic("d6")])
        assertNull(next.board[Square.fromAlgebraic("d5")])
        assertTrue(next.history.last().notation.contains("x"))
    }

    @Test
    fun pawnPromotionOffersFourChoicesAndPromotes() {
        val state = ChessEngine.stateFromFen("4k3/P7/8/8/8/8/8/4K3 w - - 0 1")
        val from = Square.fromAlgebraic("a7")!!
        val to = Square.fromAlgebraic("a8")!!

        val promotions = ChessEngine.legalMovesForSquare(state, from).filter { it.to == to }
        val next = ChessEngine.makeMove(state, Move(from, to, PieceType.QUEEN))

        assertEquals(PieceType.promotionChoices.toSet(), promotions.mapNotNull { it.promotion }.toSet())
        assertEquals(Piece(PieceType.QUEEN, PieceColor.WHITE), next!!.board[to])
    }

    @Test
    fun detectsScholarMateCheckmate() {
        var state = ChessEngine.initialState()
        val moves = listOf(
            "e2" to "e4",
            "e7" to "e5",
            "d1" to "h5",
            "b8" to "c6",
            "f1" to "c4",
            "g8" to "f6",
            "h5" to "f7",
        )

        moves.forEach { (from, to) ->
            state = ChessEngine.makeMove(state, Move(Square.fromAlgebraic(from)!!, Square.fromAlgebraic(to)!!))!!
        }

        assertEquals(GameStatus.CHECKMATE, state.status)
    }

    @Test
    fun detectsStalemate() {
        val state = ChessEngine.stateFromFen("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1")

        assertEquals(GameStatus.STALEMATE, state.status)
    }

    @Test
    fun detectsInsufficientMaterial() {
        val state = ChessEngine.stateFromFen("8/8/8/8/8/8/3k4/4K2B w - - 0 1")

        assertEquals(GameStatus.DRAW_INSUFFICIENT_MATERIAL, state.status)
    }
}
