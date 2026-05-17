package com.pramod.chessmasteroffline.ai

import com.pramod.chessmasteroffline.data.AiDifficulty
import com.pramod.chessmasteroffline.engine.ChessEngine
import com.pramod.chessmasteroffline.engine.GameState
import com.pramod.chessmasteroffline.engine.GameStatus
import com.pramod.chessmasteroffline.engine.Move
import com.pramod.chessmasteroffline.engine.PieceColor
import com.pramod.chessmasteroffline.engine.PieceType
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class ChessAi(
    private val random: Random = Random.Default,
) {
    fun chooseMove(state: GameState, difficulty: AiDifficulty): Move? {
        val legalMoves = ChessEngine.legalMoves(state)
        if (legalMoves.isEmpty()) return null

        return when (difficulty) {
            AiDifficulty.EASY -> legalMoves.random(random)
            AiDifficulty.MEDIUM -> chooseMaterialMove(state, legalMoves)
            AiDifficulty.HARD -> chooseMinimaxMove(state, legalMoves, depth = 3)
        }
    }

    private fun chooseMaterialMove(state: GameState, legalMoves: List<Move>): Move {
        val aiColor = state.sideToMove
        return legalMoves.maxBy { move ->
            val next = ChessEngine.applyMoveForSearch(state, move)
            evaluate(next, aiColor) + captureBonus(state, move)
        }
    }

    private fun chooseMinimaxMove(state: GameState, legalMoves: List<Move>, depth: Int): Move {
        val aiColor = state.sideToMove
        return orderMoves(state, legalMoves).maxBy { move ->
            val next = ChessEngine.applyMoveForSearch(state, move)
            minimax(next, depth - 1, Int.MIN_VALUE + 1, Int.MAX_VALUE - 1, aiColor)
        }
    }

    private fun minimax(
        state: GameState,
        depth: Int,
        alphaStart: Int,
        betaStart: Int,
        aiColor: PieceColor,
    ): Int {
        if (depth == 0 || state.isTerminal) return evaluate(state, aiColor)

        var alpha = alphaStart
        var beta = betaStart
        val legalMoves = orderMoves(state, ChessEngine.legalMoves(state))
        if (legalMoves.isEmpty()) return evaluate(state, aiColor)

        return if (state.sideToMove == aiColor) {
            var best = Int.MIN_VALUE + 1
            for (move in legalMoves) {
                best = max(best, minimax(ChessEngine.applyMoveForSearch(state, move), depth - 1, alpha, beta, aiColor))
                alpha = max(alpha, best)
                if (beta <= alpha) break
            }
            best
        } else {
            var best = Int.MAX_VALUE - 1
            for (move in legalMoves) {
                best = min(best, minimax(ChessEngine.applyMoveForSearch(state, move), depth - 1, alpha, beta, aiColor))
                beta = min(beta, best)
                if (beta <= alpha) break
            }
            best
        }
    }

    private fun evaluate(state: GameState, aiColor: PieceColor): Int {
        when (state.status) {
            GameStatus.CHECKMATE -> return if (state.sideToMove == aiColor) -100_000 else 100_000
            GameStatus.STALEMATE,
            GameStatus.DRAW_INSUFFICIENT_MATERIAL,
            -> return 0
            else -> Unit
        }

        val material = state.board.values.sumOf { piece ->
            val sign = if (piece.color == aiColor) 1 else -1
            sign * piece.type.materialValue
        }
        val mobility = ChessEngine.legalMoves(state).size * if (state.sideToMove == aiColor) 2 else -2
        val checkPressure = if (state.status == GameStatus.CHECK && state.sideToMove != aiColor) 35 else 0
        val development = state.board.entries.sumOf { (square, piece) ->
            if (piece.color != aiColor) return@sumOf 0
            when (piece.type) {
                PieceType.PAWN -> if (piece.color == PieceColor.WHITE) (6 - square.row) * 3 else (square.row - 1) * 3
                PieceType.KNIGHT, PieceType.BISHOP -> if (square.col in 2..5 && square.row in 2..5) 12 else 0
                else -> 0
            }
        }
        return material + mobility + checkPressure + development
    }

    private fun orderMoves(state: GameState, moves: List<Move>): List<Move> {
        return moves.sortedByDescending { move ->
            captureBonus(state, move) + if (move.promotion != null) move.promotion.materialValue else 0
        }
    }

    private fun captureBonus(state: GameState, move: Move): Int {
        val moving = state.board[move.from] ?: return 0
        val captured = state.board[move.to]
            ?: if (moving.type == PieceType.PAWN && move.to == state.enPassantTarget && move.from.col != move.to.col) {
                state.board[com.pramod.chessmasteroffline.engine.Square(move.from.row, move.to.col)]
            } else {
                null
            }
        return captured?.type?.materialValue ?: 0
    }
}
