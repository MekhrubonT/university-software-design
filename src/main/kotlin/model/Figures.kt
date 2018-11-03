package model

sealed class FigureImpl(
        var mPosition: Position,
        val mColor: Table.Color,
        var mMoveDirections: List<Pair<Int, Int>>,
        val mShortMoves: Boolean = false,
        var mBeatDirections: List<Pair<Int, Int>>? = null
) : Figure {
    var moved = false
    val initialPosition = mPosition

    override fun beats(position: Position?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun eat() {
        val position = mPosition

    }

    override fun getPosition(): Position = mPosition

    override fun setPosition(position: Position) {
        mPosition = position
    }

    override fun makeMove(to: Position?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isAllowedMove(to: Position?): Boolean {
        return getPossibleMoves().flatten().map { position plus it.toPair() }.contains(to)
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

    override fun afterMove() {
        super.afterMove()
        isShortCastlingPossible = false
        isLongCastlingPossible = false
    }
}

class Rook(mPosition: Position, mColor: Table.Color) :
        FigureImpl(mPosition, mColor, ROOK_DIRS)

class Pawn(mPosition: Position, mColor: Table.Color) :
        FigureImpl(mPosition, mColor, listOf(Pair(1, 0)), true, UPPER_DIAGONAL) {
    init {
        if (mColor == Table.Color.BLACK) {
            mMoveDirections = mMoveDirections.map { it times (-1) }
            mBeatDirections = mBeatDirections?.map { it times (-1) }
        }
    }


}

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
                ROOK_DIRS.plus(BISHOP_DIRS))

//enum class FigureKind(
//        val initWhitePositions: List<Position>,
//        val moveDirections: List<Pair<Int, Int>>,
//        val shortMoves: Boolean = false,
//        val beatDirections: List<Pair<Int, Int>>? = null) {
//    PAWN((0..7).map { PositionImpl(0, it) },
//            listOf(Pair(1, 0)), true, listOf(Pair(1, 1), Pair(1, -1))),
//    ROOK(listOf(PositionImpl(0, 0), PositionImpl(0, 7)),
//            listOf(Pair(1, 0), Pair(0, 1), Pair(-1, 0), Pair(0, -1))),
//    KNIGHT(listOf(PositionImpl(0, 1), PositionImpl(0, 6)),
//            listOf(Pair(1, 2), Pair(2, 1), Pair(-1, 2), Pair(-2, 1), Pair(1, -2), Pair(2, -1), Pair(-1, -2), Pair(-2, -1))),
//    BISHOP(listOf(PositionImpl(0, 2), PositionImpl(0, 5)),
//            listOf(Pair(1, 0), Pair(1, 1), Pair(0, 1), Pair(-1, 1), Pair(-1, 0), Pair(-1, -1), Pair(0, -1), Pair(1, -1))),
//    QUEEN(listOf(PositionImpl(0, 3)), ROOK.moveDirections.plus(BISHOP.moveDirections)),
//    KING(listOf(PositionImpl(0, 4)), QUEEN.moveDirections, shortMoves = true)
//}