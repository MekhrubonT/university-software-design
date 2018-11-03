package model

class TableImpl : Table {
    var state = GameState.NONE

    var turn = Table.Color.WHITE

    val kingsPositions = mutableMapOf<Table.Color, Position>()

    val figuresByColor = mapOf(
            Table.Color.BLACK to mutableSetOf<Figure>(),
            Table.Color.WHITE to mutableSetOf())

    val figures = mutableSetOf<Figure>()

    val board: Array<Array<Figure?>> = Array(8) { Array(8) { null as Figure? } }

    init {
        // fixme
//        for (figureKind in FigureKind.values()) {
//            val initWhitePositions = figureKind.initWhitePositions
//            for (pos in initWhitePositions) {
//                val whiteFigure = Knight(pos, Table.Color.WHITE)
//                board[pos.row][pos.col] = whiteFigure
//                figuresByColor[Table.Color.WHITE]?.add(whiteFigure)
//                val mirroredPos = PositionImpl(7 - pos.row, pos.col)
//                val blackFigure = FigureImpl(mirroredPos, figureKind, Table.Color.BLACK)
//                board[mirroredPos.row][mirroredPos.col] = blackFigure
//                figuresByColor[Table.Color.BLACK]?.add(whiteFigure)
//            }
//        }
//        kingsPositions[Table.Color.BLACK] = PositionImpl(0, 4)
//        kingsPositions[Table.Color.WHITE] = PositionImpl(7, 4)
//        figures.addAll(figuresByColor.values.asSequence().flatMap { it.asSequence() })
    }

    override fun getCurrentState() = state

    override fun getCurrentTurn() = turn

    override fun getFigure(p: Position): Figure? = board[p.row][p.col]

    override fun setFigure(figure: Figure?, position: Position) {
        board[position.row][position.col] = figure
    }

    override fun getAllFigures(): MutableList<Figure> = figures.toMutableList()

    override fun getBlackFigures(): MutableList<Figure>? = figuresByColor[Table.Color.BLACK]?.toMutableList()

    override fun getWhiteFigures(): MutableList<Figure>? = figuresByColor[Table.Color.WHITE]?.toMutableList()

    private fun throwUnless(
            condition: Boolean,
            exceptionSupplier: () -> Exception) {
        if (!condition)
            throw exceptionSupplier.invoke()
    }

    private fun isMoveFeasible(playerColor: Table.Color, from: Position, to: Position): Boolean =
            playerColor == turn &&
                    getFigure(from)?.isMine(playerColor) ?: false &&
                    !(getFigure(to)?.isMine(playerColor) ?: false)

    private fun Table.Color.other() = when (this) {
        Table.Color.BLACK -> Table.Color.WHITE
        else -> Table.Color.BLACK
    }

    private infix fun Figure.moveTo(position: Position): Figure? {
        setFigure(this, position)
        val otherFigure = getFigure(position)
        otherFigure?.let {
            figuresByColor[turn.other()]?.remove(it)
            figures.remove(it)
        }
        return otherFigure
    }

    private fun tryMove(figure: Figure, from: Position, to: Position): Boolean {
        val otherFigure = figure moveTo to
        if (isCurrentKingBeaten()) {
            revertMove(from, to, otherFigure)
            return false
        }
        return true
    }

    private fun isCurrentKingBeaten() = figuresByColor[turn.other()]?.asSequence()
            ?.any { it.beats(kingsPositions[turn]) } != false

    override fun makeMove(playerColor: Table.Color, from: Position, to: Position) {
        val e = { IllegalMoveException("move from $from to $to by player $playerColor is impossible") }
        throwUnless(isMoveFeasible(playerColor, from, to), e)
        getFigure(from)?.let { figure ->
            throwUnless(figure.isAllowedMove(to), e)
            if (!tryMove(figure, from, to))
                throw e()
            if (figure::class == King::class)
                kingsPositions[turn] = to
        }
        turn = turn.other()
        updateState()
    }

    private fun revertMove(from: Position, to: Position, otherFigure: Figure?) {
        getFigure(to)?.let { figure ->
            setFigure(figure, from)
            setFigure(otherFigure, to)
            otherFigure?.let {
                figuresByColor[turn.other()]?.add(it)
                figures.add(it)
            }
        } ?: throw IllegalStateException()
    }

    private fun Figure.hasMoves(): Boolean {
        return possibleMoves.any { movesForDir ->
            movesForDir.any { move ->
                val to = position plus move.toPair()
                to?.let { newPosition ->
                    getFigure(newPosition) == null && tryMove(this, newPosition, to)
                } != false
            }
        }
    }

    private fun currentHasMoves() =
            figuresByColor[turn]?.asSequence()?.any { it.hasMoves() } != false


    private fun updateState() {
        if (isCurrentKingBeaten()) {
            state = if (currentHasMoves())
                GameState.CHECK
            else
                GameState.CHECKMATE
        } else {
            if (!currentHasMoves())
                state = GameState.STALEMATE
        }
    }
}

infix fun Pair<Int, Int>.times(i: Int): Pair<Int, Int> = i * this.first to i * this.second

infix fun Position.plus(move: Pair<Int, Int>): Position? {
    val newRow = this.row + move.first
    val newCol = this.col + move.second
    if (newRow in 0..(N_ROWS - 1) && newCol in 0..(N_COLS - 1))
        return PositionImpl(newRow, newCol)
    return null
}

val N_ROWS = 8

val N_COLS = 8

class IllegalMoveException(message: String) : Exception(message)