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
        repeat(8) {
            setFigure(Pawn(PositionImpl(1, it), Table.Color.WHITE))
            setFigure(Pawn(PositionImpl(6, it), Table.Color.BLACK))
        }

        mirrorFigure(PositionImpl(0, 0)) { pos, color -> Rook(pos, color) }
        mirrorFigure(PositionImpl(0, 1)) { pos, color -> Knight(pos, color) }
        mirrorFigure(PositionImpl(0, 2)) { pos, color -> Bishop(pos, color) }

        setFigure(King(Table.Color.WHITE))
        setFigure(King(Table.Color.BLACK))

        setFigure(Queen(Table.Color.WHITE))
        setFigure(Queen(Table.Color.BLACK))
    }


    private fun mirrorFigure(position: Position, figureSupplier: (Position, Table.Color) -> Figure) {
        setFigure(figureSupplier(position, Table.Color.WHITE))
        setFigure(figureSupplier(PositionImpl(position.row, 7 - position.col), Table.Color.WHITE))
        setFigure(figureSupplier(PositionImpl(7 - position.row, position.col), Table.Color.BLACK))
        setFigure(figureSupplier(PositionImpl(7 - position.row, 7 - position.col), Table.Color.BLACK))
    }

    fun clear() {
        board.forEach { row ->
            repeat(row.size) {
                row[it]?.let { figure ->
                    figures.remove(figure)
                    figuresByColor[figure.color]?.remove(figure)
                }
                row[it] = null
            }
        }
    }

    override fun getCurrentState() = state

    override fun getCurrentTurn() = turn

    override fun getFigure(p: Position): Figure? = board[p.row][p.col]

    override fun getFigure(row: String, column: String): Figure? {
        return this.getFigure(PositionImpl(Integer.parseInt(row), Integer.parseInt(column)))
    }

    override fun setFigure(figure: Figure) {
        val position = figure.position
        board[position.row][position.col] = figure
        figures.add(figure)
        figuresByColor[figure.color]?.add(figure)
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

    private fun setFigure(figure: Figure, position: Position) {
        val oldPosition = figure.position
        board[oldPosition.row][oldPosition.col] = null
        figure.position = position
        setFigure(figure)
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
            if (!tryMove(figure, from, to)) {
                throw e()
            }
            if (figure::class == King::class) {
                kingsPositions[turn] = to
            }
        }
        turn = turn.other()
        updateState()
    }

    private fun revertMove(from: Position, to: Position, otherFigure: Figure?) {
        getFigure(to)?.let { figure ->
            setFigure(figure, from)
            otherFigure?.let {
                setFigure(it, to)
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
            state = if (currentHasMoves()) {
                GameState.CHECK
            } else {
                GameState.CHECKMATE
            }
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