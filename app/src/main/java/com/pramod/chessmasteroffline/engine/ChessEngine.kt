package com.pramod.chessmasteroffline.engine

import kotlin.math.abs

object ChessEngine {
    fun initialState(): GameState = stateFromFen(Fen.STARTING_FEN)

    fun stateFromFen(fen: String, history: List<MoveRecord> = emptyList()): GameState {
        return evaluateState(Fen.fromFen(fen, history))
    }

    fun legalMoves(state: GameState): List<Move> {
        return state.board
            .filterValues { it.color == state.sideToMove }
            .flatMap { (square, piece) -> pseudoMovesForPiece(state, square, piece) }
            .filterNot { move -> leavesKingInCheck(state, move, state.sideToMove) }
    }

    fun legalMovesForSquare(state: GameState, square: Square): List<Move> {
        val piece = state.board[square] ?: return emptyList()
        if (piece.color != state.sideToMove) return emptyList()
        return legalMoves(state).filter { it.from == square }
    }

    fun makeMove(state: GameState, requestedMove: Move): GameState? {
        val move = findMatchingLegalMove(state, requestedMove) ?: return null
        val movedPiece = state.board[move.from] ?: return null
        val captured = capturedPieceFor(state, move)
        val beforeFen = Fen.toFen(state)
        val evaluated = evaluateState(applyMoveUnchecked(state, move))
        val notation = ChessNotation.format(state, move, evaluated, movedPiece, captured)
        val record = MoveRecord(
            color = state.sideToMove,
            move = move,
            piece = movedPiece,
            captured = captured,
            notation = notation,
            fenBefore = beforeFen,
            fenAfter = Fen.toFen(evaluated),
        )
        return evaluated.copy(history = state.history + record)
    }

    fun applyMoveForSearch(state: GameState, move: Move): GameState {
        return evaluateState(applyMoveUnchecked(state, move).copy(history = state.history))
    }

    fun undo(state: GameState): GameState? {
        val previous = state.history.lastOrNull() ?: return null
        return stateFromFen(previous.fenBefore, state.history.dropLast(1))
    }

    fun evaluateState(state: GameState): GameState {
        val insufficient = hasInsufficientMaterial(state)
        val inCheck = isInCheck(state, state.sideToMove)
        val moves = legalMoves(state)
        val status = when {
            insufficient -> GameStatus.DRAW_INSUFFICIENT_MATERIAL
            moves.isEmpty() && inCheck -> GameStatus.CHECKMATE
            moves.isEmpty() -> GameStatus.STALEMATE
            inCheck -> GameStatus.CHECK
            else -> GameStatus.ONGOING
        }
        return state.copy(status = status)
    }

    fun isInCheck(state: GameState, color: PieceColor): Boolean {
        val kingSquare = state.board.entries.firstOrNull {
            it.value.color == color && it.value.type == PieceType.KING
        }?.key ?: return false
        return isSquareAttacked(state, kingSquare, color.opposite)
    }

    fun isSquareAttacked(state: GameState, square: Square, byColor: PieceColor): Boolean {
        val pawnRow = square.row - byColor.pawnDirection
        for (colOffset in listOf(-1, 1)) {
            val pawnSquare = Square.orNull(pawnRow, square.col + colOffset)
            if (pawnSquare != null && state.board[pawnSquare] == Piece(PieceType.PAWN, byColor)) {
                return true
            }
        }

        val knightOffsets = listOf(
            -2 to -1, -2 to 1, -1 to -2, -1 to 2,
            1 to -2, 1 to 2, 2 to -1, 2 to 1,
        )
        for ((dr, dc) in knightOffsets) {
            val target = Square.orNull(square.row + dr, square.col + dc)
            if (target != null && state.board[target] == Piece(PieceType.KNIGHT, byColor)) {
                return true
            }
        }

        val kingOffsets = (-1..1).flatMap { dr -> (-1..1).map { dc -> dr to dc } }
            .filterNot { it.first == 0 && it.second == 0 }
        for ((dr, dc) in kingOffsets) {
            val target = Square.orNull(square.row + dr, square.col + dc)
            if (target != null && state.board[target] == Piece(PieceType.KING, byColor)) {
                return true
            }
        }

        val directions = listOf(
            -1 to 0, 1 to 0, 0 to -1, 0 to 1,
            -1 to -1, -1 to 1, 1 to -1, 1 to 1,
        )
        for ((dr, dc) in directions) {
            var row = square.row + dr
            var col = square.col + dc
            while (Square.isInside(row, col)) {
                val piece = state.board[Square(row, col)]
                if (piece != null) {
                    if (piece.color == byColor) {
                        val orthogonal = dr == 0 || dc == 0
                        val diagonal = abs(dr) == abs(dc)
                        if (piece.type == PieceType.QUEEN ||
                            (orthogonal && piece.type == PieceType.ROOK) ||
                            (diagonal && piece.type == PieceType.BISHOP)
                        ) {
                            return true
                        }
                    }
                    break
                }
                row += dr
                col += dc
            }
        }

        return false
    }

    fun hasInsufficientMaterial(state: GameState): Boolean {
        val nonKings = state.board.entries.filter { it.value.type != PieceType.KING }
        if (nonKings.isEmpty()) return true
        if (nonKings.any { it.value.type == PieceType.PAWN || it.value.type == PieceType.ROOK || it.value.type == PieceType.QUEEN }) {
            return false
        }
        if (nonKings.size == 1 && nonKings.first().value.type in listOf(PieceType.BISHOP, PieceType.KNIGHT)) {
            return true
        }
        if (nonKings.all { it.value.type == PieceType.BISHOP }) {
            return nonKings.map { (it.key.row + it.key.col) % 2 }.distinct().size == 1
        }
        return false
    }

    private fun pseudoMovesForPiece(state: GameState, from: Square, piece: Piece): List<Move> {
        return when (piece.type) {
            PieceType.PAWN -> pawnMoves(state, from, piece)
            PieceType.KNIGHT -> knightMoves(state, from, piece)
            PieceType.BISHOP -> slidingMoves(state, from, piece, listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1))
            PieceType.ROOK -> slidingMoves(state, from, piece, listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1))
            PieceType.QUEEN -> slidingMoves(
                state,
                from,
                piece,
                listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1, -1 to -1, -1 to 1, 1 to -1, 1 to 1),
            )
            PieceType.KING -> kingMoves(state, from, piece)
        }
    }

    private fun pawnMoves(state: GameState, from: Square, piece: Piece): List<Move> {
        val moves = mutableListOf<Move>()
        val direction = piece.color.pawnDirection
        val startRow = if (piece.color == PieceColor.WHITE) 6 else 1
        val oneForward = Square.orNull(from.row + direction, from.col)

        if (oneForward != null && state.board[oneForward] == null) {
            moves += promotionAwareMoves(from, oneForward, piece)
            val twoForward = Square.orNull(from.row + direction * 2, from.col)
            if (from.row == startRow && twoForward != null && state.board[twoForward] == null) {
                moves += Move(from, twoForward)
            }
        }

        for (colOffset in listOf(-1, 1)) {
            val target = Square.orNull(from.row + direction, from.col + colOffset) ?: continue
            val targetPiece = state.board[target]
            if (targetPiece != null && targetPiece.color != piece.color) {
                moves += promotionAwareMoves(from, target, piece)
            } else if (target == state.enPassantTarget) {
                moves += Move(from, target)
            }
        }

        return moves
    }

    private fun knightMoves(state: GameState, from: Square, piece: Piece): List<Move> {
        val offsets = listOf(
            -2 to -1, -2 to 1, -1 to -2, -1 to 2,
            1 to -2, 1 to 2, 2 to -1, 2 to 1,
        )
        return offsets.mapNotNull { (dr, dc) ->
            val target = Square.orNull(from.row + dr, from.col + dc) ?: return@mapNotNull null
            val targetPiece = state.board[target]
            if (targetPiece?.color == piece.color) null else Move(from, target)
        }
    }

    private fun slidingMoves(
        state: GameState,
        from: Square,
        piece: Piece,
        directions: List<Pair<Int, Int>>,
    ): List<Move> {
        val moves = mutableListOf<Move>()
        for ((dr, dc) in directions) {
            var row = from.row + dr
            var col = from.col + dc
            while (Square.isInside(row, col)) {
                val target = Square(row, col)
                val targetPiece = state.board[target]
                if (targetPiece == null) {
                    moves += Move(from, target)
                } else {
                    if (targetPiece.color != piece.color) moves += Move(from, target)
                    break
                }
                row += dr
                col += dc
            }
        }
        return moves
    }

    private fun kingMoves(state: GameState, from: Square, piece: Piece): List<Move> {
        val moves = mutableListOf<Move>()
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val target = Square.orNull(from.row + dr, from.col + dc) ?: continue
                val targetPiece = state.board[target]
                if (targetPiece?.color != piece.color) moves += Move(from, target)
            }
        }
        moves += castlingMoves(state, from, piece)
        return moves
    }

    private fun castlingMoves(state: GameState, from: Square, piece: Piece): List<Move> {
        if (piece.type != PieceType.KING || isInCheck(state, piece.color)) return emptyList()
        val row = if (piece.color == PieceColor.WHITE) 7 else 0
        if (from != Square(row, 4)) return emptyList()

        val opponent = piece.color.opposite
        val moves = mutableListOf<Move>()
        val rights = state.castlingRights

        fun empty(vararg cols: Int): Boolean = cols.all { state.board[Square(row, it)] == null }
        fun safe(vararg cols: Int): Boolean = cols.all { !isSquareAttacked(state, Square(row, it), opponent) }
        fun rookAt(col: Int): Boolean = state.board[Square(row, col)] == Piece(PieceType.ROOK, piece.color)

        if ((piece.color == PieceColor.WHITE && rights.whiteKingSide || piece.color == PieceColor.BLACK && rights.blackKingSide) &&
            rookAt(7) && empty(5, 6) && safe(5, 6)
        ) {
            moves += Move(from, Square(row, 6))
        }
        if ((piece.color == PieceColor.WHITE && rights.whiteQueenSide || piece.color == PieceColor.BLACK && rights.blackQueenSide) &&
            rookAt(0) && empty(1, 2, 3) && safe(2, 3)
        ) {
            moves += Move(from, Square(row, 2))
        }
        return moves
    }

    private fun promotionAwareMoves(from: Square, to: Square, piece: Piece): List<Move> {
        val promotionRow = if (piece.color == PieceColor.WHITE) 0 else 7
        return if (piece.type == PieceType.PAWN && to.row == promotionRow) {
            PieceType.promotionChoices.map { Move(from, to, it) }
        } else {
            listOf(Move(from, to))
        }
    }

    private fun leavesKingInCheck(state: GameState, move: Move, color: PieceColor): Boolean {
        return isInCheck(applyMoveUnchecked(state, move), color)
    }

    private fun findMatchingLegalMove(state: GameState, requestedMove: Move): Move? {
        val requestedPromotion = requestedPromotion(state, requestedMove)
        return legalMoves(state).firstOrNull {
            it.from == requestedMove.from && it.to == requestedMove.to && it.promotion == requestedPromotion
        }
    }

    private fun requestedPromotion(state: GameState, move: Move): PieceType? {
        val piece = state.board[move.from] ?: return move.promotion
        val promotionRow = if (piece.color == PieceColor.WHITE) 0 else 7
        return if (piece.type == PieceType.PAWN && move.to.row == promotionRow) {
            move.promotion ?: PieceType.QUEEN
        } else {
            null
        }
    }

    private fun applyMoveUnchecked(state: GameState, move: Move): GameState {
        val movingPiece = state.board[move.from] ?: return state
        val board = state.board.toMutableMap()
        val captureSquare = captureSquareFor(state, move, movingPiece)
        val captured = captureSquare?.let { board[it] }

        board.remove(move.from)
        if (captureSquare != null) board.remove(captureSquare)

        val promotionRow = if (movingPiece.color == PieceColor.WHITE) 0 else 7
        val placedPiece = if (movingPiece.type == PieceType.PAWN && move.to.row == promotionRow) {
            Piece(move.promotion ?: PieceType.QUEEN, movingPiece.color)
        } else {
            movingPiece
        }
        board[move.to] = placedPiece

        if (movingPiece.type == PieceType.KING && abs(move.to.col - move.from.col) == 2) {
            val row = move.from.row
            if (move.to.col == 6) {
                board.remove(Square(row, 7))?.let { board[Square(row, 5)] = it }
            } else {
                board.remove(Square(row, 0))?.let { board[Square(row, 3)] = it }
            }
        }

        val rights = updateCastlingRights(state.castlingRights, movingPiece, move.from, captureSquare, captured)
        val enPassantTarget = if (movingPiece.type == PieceType.PAWN && abs(move.to.row - move.from.row) == 2) {
            Square((move.from.row + move.to.row) / 2, move.from.col)
        } else {
            null
        }
        val halfMoveClock = if (movingPiece.type == PieceType.PAWN || captured != null) 0 else state.halfMoveClock + 1
        val fullMoveNumber = if (movingPiece.color == PieceColor.BLACK) state.fullMoveNumber + 1 else state.fullMoveNumber

        return state.copy(
            board = board,
            sideToMove = state.sideToMove.opposite,
            castlingRights = rights,
            enPassantTarget = enPassantTarget,
            halfMoveClock = halfMoveClock,
            fullMoveNumber = fullMoveNumber,
            status = GameStatus.ONGOING,
            lastMove = move,
        )
    }

    private fun updateCastlingRights(
        current: CastlingRights,
        movingPiece: Piece,
        from: Square,
        captureSquare: Square?,
        captured: Piece?,
    ): CastlingRights {
        var rights = current
        fun revokeWhiteKing() {
            rights = rights.copy(whiteKingSide = false, whiteQueenSide = false)
        }
        fun revokeBlackKing() {
            rights = rights.copy(blackKingSide = false, blackQueenSide = false)
        }

        when {
            movingPiece.type == PieceType.KING && movingPiece.color == PieceColor.WHITE -> revokeWhiteKing()
            movingPiece.type == PieceType.KING && movingPiece.color == PieceColor.BLACK -> revokeBlackKing()
            movingPiece.type == PieceType.ROOK && from == Square(7, 0) -> rights = rights.copy(whiteQueenSide = false)
            movingPiece.type == PieceType.ROOK && from == Square(7, 7) -> rights = rights.copy(whiteKingSide = false)
            movingPiece.type == PieceType.ROOK && from == Square(0, 0) -> rights = rights.copy(blackQueenSide = false)
            movingPiece.type == PieceType.ROOK && from == Square(0, 7) -> rights = rights.copy(blackKingSide = false)
        }

        if (captured?.type == PieceType.ROOK && captureSquare != null) {
            rights = when (captureSquare) {
                Square(7, 0) -> rights.copy(whiteQueenSide = false)
                Square(7, 7) -> rights.copy(whiteKingSide = false)
                Square(0, 0) -> rights.copy(blackQueenSide = false)
                Square(0, 7) -> rights.copy(blackKingSide = false)
                else -> rights
            }
        }
        return rights
    }

    private fun capturedPieceFor(state: GameState, move: Move): Piece? {
        val movingPiece = state.board[move.from] ?: return null
        val captureSquare = captureSquareFor(state, move, movingPiece)
        return captureSquare?.let { state.board[it] }
    }

    private fun captureSquareFor(state: GameState, move: Move, movingPiece: Piece): Square? {
        val directCapture = state.board[move.to]
        if (directCapture != null) return move.to
        val isEnPassant = movingPiece.type == PieceType.PAWN &&
            move.to == state.enPassantTarget &&
            move.from.col != move.to.col
        return if (isEnPassant) Square(move.from.row, move.to.col) else null
    }
}
