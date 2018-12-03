package model

class Move(val figureShifts: MutableMap<out Figure, Pair<Int, Int>>) {
    fun toPair(): Pair<Int, Int> {
        return figureShifts.iterator().next().value
    }

    fun isComposite(): Boolean = figureShifts.size > 1
}

fun singleFigureMove(figure: Figure, shift: Pair<Int, Int>): Move {
    return Move(mutableMapOf(figure to shift))
}

fun creteCastling(king: King, rook: Rook, isShort: Boolean): Move =
        Move(
                if (isShort) mutableMapOf(
                        king to Pair(0, 2),
                        rook to Pair(0, -2))
                else
                    mutableMapOf(
                            king to Pair(0, -2),
                            rook to Pair(0, 3))
        )
