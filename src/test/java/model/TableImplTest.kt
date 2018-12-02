package model


import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class TableImplTest {
    private val table = TableImpl()

    @BeforeEach
    internal fun setUp() {
        table.clear()
    }

    // junit 5 requires static params method, thus the ugly workaround
    companion object {
        @JvmStatic
        fun singleFigureParams(): Array<Array<out Any?>> = arrayOf(
                arrayOf(
                        Rook(PositionImpl(0, 0), Table.Color.WHITE),
                        PositionImpl(0, 7),
                        true,
                        null
                ),
                arrayOf(
                        Rook(PositionImpl(0, 0), Table.Color.WHITE),
                        PositionImpl(1, 1),
                        false,
                        null
                ),
                arrayOf(
                        Rook(PositionImpl(0, 0), Table.Color.WHITE),
                        PositionImpl(0, 7),
                        false,
                        Pawn(PositionImpl(0, 1), Table.Color.WHITE)
                )
        )
    }

    @ParameterizedTest
    @MethodSource("singleFigureParams")
    internal fun testSingleFigure(figure: Figure, to: Position, isValid: Boolean, obstacle: Figure? = null) {
        val from = figure.position
        table.setFigure(figure)
        if (obstacle != null)
            table.setFigure(obstacle)

        val move = { table.makeMove(Table.Color.WHITE, from, to) }
        if (!isValid) {
            assertThrows<IllegalMoveException>(move)
        } else if (obstacle == null) {
            move()
            assertNull(table.getFigure(from))
            assertEquals(figure, table.getFigure(to))
        } else {
            assertEquals(figure, table.getFigure(from))
        }
    }

    @Test
    fun failEatOwnFigure() {
        val from = PositionImpl(2, 3)
        table.setFigure(Pawn(from, Table.Color.BLACK))
        val to = PositionImpl(2, 2)
        table.setFigure(Pawn(to, Table.Color.BLACK))
        assertThrows<IllegalMoveException> {
            table.makeMove(Table.Color.BLACK, from, to)
        }
    }


    @Test
    fun eatOtherFigure() {
        val from = PositionImpl(2, 2)
        val whitePawn = Pawn(from, Table.Color.WHITE)
        table.setFigure(whitePawn)
        val to = PositionImpl(3, 3)
        table.setFigure(Pawn(to, Table.Color.BLACK))

        table.makeMove(Table.Color.WHITE, from, to)
        assertNull(table.getFigure(from))
        assertEquals(whitePawn, table.getFigure(to))
    }

    @Test
    internal fun successShortCastling() {
        val king = King(Table.Color.WHITE)
        table.setFigure(king)
        val rook = Rook(PositionImpl(0, 0), Table.Color.WHITE)
        table.setFigure(rook)
        table.makeMove(Table.Color.WHITE, king.position, PositionImpl(0, 1))

        assertNull(table.getFigure(0, 0))
        assertEquals(king, PositionImpl(0, 1))
        assertEquals(rook, PositionImpl(0, 2))
        assertNull(table.getFigure(0, 3))
    }

    @Test
    internal fun successEatOnHop() {
        table.setFigure(Pawn(PositionImpl(1, 1), Table.Color.WHITE))
        val blackPawn = Pawn(PositionImpl(3, 2), Table.Color.BLACK)
        table.setFigure(blackPawn)
        table.makeMove(Table.Color.WHITE, PositionImpl(1, 1), PositionImpl(3, 1))
        table.makeMove(Table.Color.BLACK, PositionImpl(3, 2), PositionImpl(2, 1))

        val figures = table.figures
        assertEquals(1, figures.size)
        assertEquals(blackPawn, figures.iterator().next())
    }

    @Test
    internal fun reproduceNotRevertedMoves() {
        val whitePawn = Pawn(PositionImpl(1, 0), Table.Color.WHITE)
        table.setFigure(whitePawn)
        val whiteKing = King(PositionImpl(0, 3), Table.Color.WHITE)
        table.setFigure(whiteKing)
        val blackPawn = Pawn(PositionImpl(6, 0), Table.Color.BLACK)
        table.setFigure(blackPawn)
        val blackKing = King(PositionImpl(7, 3), Table.Color.BLACK)
        table.setFigure(blackKing)

        table.makeMove(Table.Color.WHITE, whitePawn.position, PositionImpl(2, 0))

        assertEquals(PositionImpl(2, 0), whitePawn.position)
        assertEquals(PositionImpl(0, 3), whiteKing.position)
        assertEquals(PositionImpl(6, 0), blackPawn.position)
        assertEquals(PositionImpl(7, 3), blackKing.position)

    }

    private fun printBoard() {
        table.figuresByColor.forEach {
            println(it.key)
            it.value.forEach(::println)
        }
    }
}