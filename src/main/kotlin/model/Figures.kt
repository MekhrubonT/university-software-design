package model

sealed class FigureImpl(
        var mPosition: Position,
        val mColor: Color,
        var mMoveDirections: List<Pair<Int, Int>>,
        val mShortMoves: Boolean = false,
        var mBeatDirections: List<Pair<Int, Int>> = listOf()
) : Figure {
    var moved = false

    override fun hasMoved() = moved

    protected fun beatsImpl(directions: List<Pair<Int, Int>>, position: Position?): Boolean {
        return position != null && directions.map { position plus it }.contains(position)
    }

    override fun beats(table: Table, position: Position?): Boolean {
        return getPossibleMoves(table).filter { !it.isComposite() }.map { position?.plus(it.toPair()) }.contains(position)
    }

    override fun getColor(): Color = mColor

    override fun getPosition(): Position = mPosition

    override fun setPosition(position: Position) {
        mPosition = position
    }

    override fun isMine(playerColor: Color): Boolean {
        return mColor == playerColor
    }

    open fun getShortMoves(table: Table): Sequence<Move> =
            mMoveDirections.asSequence().map { singleFigureMove(this, it) }

    override fun getPossibleMoves(table: Table): Sequence<Move> {
        return if (mShortMoves) {
            getShortMoves(table).filter { this applyMove it != null }
        } else {
            mMoveDirections.asSequence().flatMap { direction ->
                generateSequence(direction) { it plus direction }
                        .takeWhile { shift ->
                            val curPosition = position plus shift
                            curPosition?.let {
                                val otherFigure = table.getFigure(curPosition)
                                otherFigure == null || otherFigure.color != color &&
                                        (position plus (shift minus direction))
                                                ?.let { it == position || table.getFigure(it) == null } == true
                            } == true
                        }
            }.map { shift -> singleFigureMove(this, shift) }
        }
    }

    override fun afterMove() {
        moved = true
    }

    fun upgrade(): Figure = this

    override fun toString(): String {
        return "${this::class.java.name}(mPosition=$mPosition, mColor=$mColor)"
    }
}

private infix fun Pair<Int, Int>.plus(dir: Pair<Int, Int>): Pair<Int, Int> {
    return first + dir.first to second + dir.second
}

private infix fun Pair<Int, Int>.minus(dir: Pair<Int, Int>): Pair<Int, Int> {
    return first - dir.first to second - dir.second
}

val HORIZONTAL = listOf(Pair(0, 1), Pair(0, -1))

val VERTICAL = listOf(Pair(1, 0), Pair(-1, 0))

val ROOK_DIRS = HORIZONTAL.plus(VERTICAL)

val UPPER_DIAGONAL = listOf(Pair(1, -1), Pair(1, 1))

val LOWER_DIAGONAL = listOf(Pair(-1, -1), Pair(-1, 1))

val BISHOP_DIRS = UPPER_DIAGONAL.plus(LOWER_DIAGONAL)

class King(mPosition: Position, mColor: Color) :
        FigureImpl(
                mPosition,
                mColor,
                ROOK_DIRS.plus(BISHOP_DIRS),
                true) {

    constructor(mColor: Color) : this(
            PositionImpl(if (mColor == Color.WHITE) 0 else 7, 4),
            mColor)

    private fun getRook(table: Table, isShort: Boolean): Rook? {
        val rookPosition = PositionImpl(position.row, if (isShort) 7 else 0)
        return table.getFigure(rookPosition)?.let {
            if (it::class == Rook::class && it.color == color && !it.hasMoved())
                it as Rook
            else
                null
        }
    }

    private fun createCastlingIfPossible(table: Table, isShort: Boolean): Sequence<Move> {
        val rook = getRook(table, isShort)
        return sequenceOf(rook).filterNotNull().filter { moved }
                .map { creteCastling(this, it, isShort) }
    }

    override fun getShortMoves(table: Table): Sequence<Move> {
        return mMoveDirections.asSequence()
                .map { singleFigureMove(this, it) }
                .plus(createCastlingIfPossible(table, true))
                .plus(createCastlingIfPossible(table, false))
    }
}

class Pawn(mPosition: Position, mColor: Color) :
        FigureImpl(mPosition, mColor, listOf(Pair(1, 0)), true, UPPER_DIAGONAL) {
    init {
        if (mColor == Color.BLACK) {
            mMoveDirections = mMoveDirections.map { it times (-1) }
            mBeatDirections = mBeatDirections.map { it times (-1) }
        }
    }

    override fun beats(table: Table, position: Position?): Boolean {
        return beatsImpl(mBeatDirections, position)
    }

    override fun getShortMoves(table: Table): Sequence<Move> {
        return mMoveDirections.asSequence()
                .map { singleFigureMove(this, it) }
                .plus(mBeatDirections.map { singleFigureMove(this, it) })
                .plus(sequenceOf(singleFigureMove(this, Pair(2, 0))).filter { !moved })
    }
}

class Rook(mPosition: Position, mColor: Color) :
        FigureImpl(mPosition, mColor, ROOK_DIRS)

class Knight(mPosition: Position, mColor: Color) :
        FigureImpl(
                mPosition,
                mColor,
                listOf(Pair(1, 2), Pair(2, 1), Pair(-1, 2), Pair(-2, 1), Pair(1, -2), Pair(2, -1), Pair(-1, -2), Pair(-2, -1)))

class Bishop(mPosition: Position, mColor: Color) :
        FigureImpl(
                mPosition,
                mColor,
                BISHOP_DIRS)

class Queen(mPosition: Position, mColor: Color) :
        FigureImpl(
                mPosition,
                mColor,
                ROOK_DIRS.plus(BISHOP_DIRS)) {
    constructor(mColor: Color) : this(
            PositionImpl(if (mColor == Color.WHITE) 0 else 7, 3),
            mColor
    )
}