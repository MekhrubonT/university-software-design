package model

sealed class FigureImpl(
        var mPosition: Position,
        val mColor: Table.Color,
        var mMoveDirections: List<Pair<Int, Int>>,
        val mShortMoves: Boolean = false,
        var mBeatDirections: List<Pair<Int, Int>> = listOf()
) : Figure {
    var moved = false

    protected fun beatsImpl(directions: List<Pair<Int, Int>>, position: Position?): Boolean {
        return position != null && directions.map { position plus it }.contains(position)
    }

    override fun beats(table: Table, position: Position): Boolean {
        return getPossibleMoves(table).map { position plus it.toPair() }.contains(position)
    }

    override fun getColor(): Table.Color = mColor

    override fun getPosition(): Position = mPosition

    override fun setPosition(position: Position) {
        mPosition = position
    }

    override fun isMine(playerColor: Table.Color): Boolean {
        return mColor == playerColor
    }

    override fun getPossibleMoves(table: Table): Sequence<Sequence<Move>> {
        val shortMoves = sequenceOf(mMoveDirections.asSequence()
                .filter { position plus it != null })
        val movePairs = if (mShortMoves) {
            shortMoves
        } else {
            mMoveDirections.asSequence().map { direction ->
                generateSequence(direction) { it plus direction }
                        .takeWhile { shift ->
                            val curPosition = position plus shift
                            curPosition?.let {
                                val otherFigure = table.getFigure(position plus shift)
                                otherFigure == null || otherFigure.color != color &&
                                        (position plus (shift minus direction))
                                                ?.let { it == position || table.getFigure(it) == null } == true
                            } == true
                        }
            }

        }
        return movePairs.map { it.map { shift -> singleFigureMove(this, shift) } }
    }

    open fun afterMove() {
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

class King(mPosition: Position, mColor: Table.Color) :
        FigureImpl(
                mPosition,
                mColor,
                ROOK_DIRS.plus(BISHOP_DIRS),
                true) {
    var isShortCastlingPossible = true

    var isLongCastlingPossible = true

    constructor(mColor: Table.Color) : this(
            PositionImpl(if (mColor == Table.Color.WHITE) 0 else 7, 4),
            mColor)

    override fun afterMove() {
        super.afterMove()
        isShortCastlingPossible = false
        isLongCastlingPossible = false
    }
}

class Pawn(mPosition: Position, mColor: Table.Color) :
        FigureImpl(mPosition, mColor, listOf(Pair(1, 0)), true, UPPER_DIAGONAL) {
    init {
        if (mColor == Table.Color.BLACK) {
            mMoveDirections = mMoveDirections.map { it times (-1) }
            mBeatDirections = mBeatDirections.map { it times (-1) }
        }
    }

    override fun beats(position: Position?): Boolean {
        return beatsImpl(mBeatDirections, position)
    }

    override fun getPossibleMoves(table: Table): Sequence<Sequence<Move>> {
        return super.getPossibleMoves(table).plus(
                mBeatDirections.filter { position plus it != null }
                        .map { sequenceOf(singleFigureMove(this, it)) })
                .plus(sequenceOf(sequenceOf(singleFigureMove(this, Pair(2, 0)))))
    }
}

class Rook(mPosition: Position, mColor: Table.Color) :
        FigureImpl(mPosition, mColor, ROOK_DIRS)

class Knight(mPosition: Position, mColor: Table.Color) :
        FigureImpl(
                mPosition,
                mColor,
                listOf(Pair(1, 2), Pair(2, 1), Pair(-1, 2), Pair(-2, 1), Pair(1, -2), Pair(2, -1), Pair(-1, -2), Pair(-2, -1)))

class Bishop(mPosition: Position, mColor: Table.Color) :
        FigureImpl(
                mPosition,
                mColor,
                BISHOP_DIRS)

class Queen(mPosition: Position, mColor: Table.Color) :
        FigureImpl(
                mPosition,
                mColor,
                ROOK_DIRS.plus(BISHOP_DIRS)) {
    constructor(mColor: Table.Color) : this(
            PositionImpl(if (mColor == Table.Color.WHITE) 0 else 7, 3),
            mColor
    )
}