package model

class TableImpl : Table {
    var state = GameState.NONE

    var turn = Color.WHITE

    val kingsPositions = mutableMapOf<Color, Position>()

    val figuresByColor = mapOf(
            Color.BLACK to mutableSetOf<Figure>(),
            Color.WHITE to mutableSetOf())

    val figures = mutableSetOf<Figure>()

    val board: Array<Array<Figure?>> = Array(8) { Array(8) { null as Figure? } }

    init {
        fill()
    }


    private fun mirrorFigure(position: Position, figureSupplier: (Position, Color) -> Figure) {
        setFigure(figureSupplier(position, Color.WHITE))
        setFigure(figureSupplier(PositionImpl(position.row, 7 - position.col), Color.WHITE))
        setFigure(figureSupplier(PositionImpl(7 - position.row, position.col), Color.BLACK))
        setFigure(figureSupplier(PositionImpl(7 - position.row, 7 - position.col), Color.BLACK))
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

    fun fill() {
        repeat(8) {
            setFigure(Pawn(PositionImpl(1, it), Color.WHITE))
            setFigure(Pawn(PositionImpl(6, it), Color.BLACK))
        }

        mirrorFigure(PositionImpl(0, 0)) { pos, color -> Rook(pos, color) }
        mirrorFigure(PositionImpl(0, 1)) { pos, color -> Knight(pos, color) }
        mirrorFigure(PositionImpl(0, 2)) { pos, color -> Bishop(pos, color) }

        Color.values().forEach {
            val king = King(it)
            setFigure(king)
            kingsPositions[it] = king.position
        }


        setFigure(Queen(Color.WHITE))
        setFigure(Queen(Color.BLACK))
    }

    override fun getCurrentState() = state

    override fun getCurrentTurn() = turn

    override fun getFigure(p: Position): Figure? = board[p.row][p.col]

    override fun setFigure(figure: Figure) {
        val position = figure.position
        board[position.row][position.col] = figure
        figures.add(figure)
        figuresByColor[figure.color]?.add(figure)
        if (figure::class == King::class)
            kingsPositions[figure.color] = figure.position
    }

    override fun getAllFigures(): MutableList<Figure> = figures.toMutableList()

    override fun getBlackFigures(): MutableList<Figure>? = figuresByColor[Color.BLACK]?.toMutableList()

    override fun getWhiteFigures(): MutableList<Figure>? = figuresByColor[Color.WHITE]?.toMutableList()

    private fun throwUnless(
            condition: Boolean,
            exceptionSupplier: () -> Exception) {
        if (!condition)
            throw exceptionSupplier.invoke()
    }

    private fun isMoveFeasible(playerColor: Color, from: Position, to: Position): Boolean =
            playerColor == turn &&
                    getFigure(from)?.isMine(playerColor) ?: false &&
                    !(getFigure(to)?.isMine(playerColor) ?: false)


    private fun setFigure(figure: Figure, position: Position) {
        val oldPosition = figure.position
        board[oldPosition.row][oldPosition.col] = null
        figure.position = position
        setFigure(figure)
    }

    private infix fun Figure.moveTo(position: Position): Figure? {
        val otherFigure = getFigure(position)
        setFigure(this, position)
        otherFigure?.let {
            figuresByColor[turn.other()]?.remove(it)
            figures.remove(it)
        }
        return otherFigure
    }

    private fun tryMove(figure: Figure, from: Position, to: Position, revert: Boolean = false): Boolean {
        val otherFigure = figure moveTo to
        val isKingBeaten = isCurrentKingBeaten()
        if (revert || isKingBeaten) {
            revertMove(from, to, otherFigure)
            return !isKingBeaten
        }
        return true
    }

    private fun isCurrentKingBeaten() = figuresByColor[turn.other()]?.asSequence()
            ?.any { it.beats(this, kingsPositions[turn]) } != false

    override fun makeMove(playerColor: Color, from: Position, to: Position) {
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
            figure.afterMove()
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
            figure
        } ?: throw IllegalArgumentException("position $to must not be empty")
    }

    fun Figure.isAllowedMove(to: Position?): Boolean {
        return getPossibleMoves(this@TableImpl).map { position plus it.toPair() }.contains(to)
    }


    private fun Figure.hasMoves(): Boolean {
        return getPossibleMoves(this@TableImpl).any { move ->
            val to = position plus move.toPair()
            to?.let { newPosition ->
                getFigure(newPosition)?.color != color &&
                        tryMove(this, position, newPosition, true)
            } == true
        }

    }

    fun currentHasMoves() =
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

    fun getFigure(row: Int, col: Int): Figure? {
        return board[row][col]
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

fun Color.other() = when (this) {
    Color.BLACK -> Color.WHITE
    else -> Color.BLACK
}

infix fun Figure.applyMove(move: Move): Position? =
        move.figureShifts[this]?.let { position plus it }

const val N_ROWS = 8

const val N_COLS = 8

class IllegalMoveException(message: String) : Exception(message)