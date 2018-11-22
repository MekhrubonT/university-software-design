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

    override fun beats(position: Position?): Boolean {
        return beatsImpl(mMoveDirections, position)
    }

    override fun getColor(): Table.Color = mColor

    override fun getPosition(): Position = mPosition

    override fun setPosition(position: Position) {
        mPosition = position
    }

    override fun isAllowedMove(to: Position?): Boolean {
        return possibleMoves.flatten().map { position plus it.toPair() }.contains(to)
    }

    override fun isMine(playerColor: Table.Color): Boolean {
        return mColor == playerColor
    }

    override fun getPossibleMoves(): Sequence<Sequence<Move>> {
        val shortMoves = sequenceOf(mMoveDirections.asSequence()
                .takeWhile { position plus it != null })
        val movePairs = if (mShortMoves) {
            shortMoves
        } else {
            mMoveDirections.asSequence().map { direction ->
                generateSequence(direction) { it plus direction }
                        .takeWhile { position plus it != null }
            }

        }
        return movePairs.map { it.map { x: Pair<Int, Int> -> singleFigureMove(this, x) } }
    }

    override fun colorToString(): String {
        return if (mColor == Table.Color.BLACK) "black" else "white"
    }

    open fun afterMove() {
        moved = true
    }

    fun upgrade(): Figure = this
}

private infix fun Pair<Int, Int>.plus(dir: Pair<Int, Int>): Pair<Int, Int>? {
    return first + dir.first to second + dir.second
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
                ROOK_DIRS.plus(BISHOP_DIRS)) {
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

    override fun toString(): String {
        return "king"
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

    override fun getPossibleMoves(): Sequence<Sequence<Move>> {
        return super.getPossibleMoves().plus(
                mBeatDirections.filter { position plus it != null }
                        .map { sequenceOf(singleFigureMove(this, it)) })
    }

    override fun toString(): String {
        return "pawn"
    }
}

class Rook(mPosition: Position, mColor: Table.Color) :
        FigureImpl(mPosition, mColor, ROOK_DIRS) {
    override fun toString(): String {
        return "rook"
    }
}

class Knight(mPosition: Position, mColor: Table.Color) :
        FigureImpl(
                mPosition,
                mColor,
                listOf(Pair(1, 2), Pair(2, 1), Pair(-1, 2), Pair(-2, 1), Pair(1, -2), Pair(2, -1), Pair(-1, -2), Pair(-2, -1)))
{
    override fun toString(): String {
        return "knight"
    }
}
class Bishop(mPosition: Position, mColor: Table.Color) :
        FigureImpl(
                mPosition,
                mColor,
                BISHOP_DIRS)
{
    override fun toString(): String {
        return "bishop"
    }
}

class Queen(mPosition: Position, mColor: Table.Color) :
        FigureImpl(
                mPosition,
                mColor,
                ROOK_DIRS.plus(BISHOP_DIRS)) {
    constructor(mColor: Table.Color) : this(
            PositionImpl(if (mColor == Table.Color.WHITE) 0 else 7, 4),
            mColor
    )

    override fun toString(): String {
        return "queen"
    }
}