package model

import kotlin.reflect.KClass

class Move(val figureShifts: MutableMap<KClass<out Figure>, Pair<Int, Int>>) {
    fun toPair(): Pair<Int, Int> {
        return figureShifts.iterator().next().value
    }
}

fun singleFigureMove(figure: Figure, shift: Pair<Int, Int>): Move {
    return Move(mutableMapOf(figure::class to shift))
}

val SHORT_CASTLING = Move(mutableMapOf(
        King::class to Pair(0, 2),
        Rook::class to Pair(0, -2)))

val LONG_CASTLING = Move(mutableMapOf(
        King::class to Pair(0, -2),
        Rook::class to Pair(0, 3)))