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
                        Rook(PositionImpl(0, 0), Color.WHITE),
                        PositionImpl(0, 7),
                        true,
                        null
                ),
                arrayOf(
                        Rook(PositionImpl(0, 0), Color.WHITE),
                        PositionImpl(1, 1),
                        false,
                        null
                ),
                arrayOf(
                        Rook(PositionImpl(0, 0), Color.WHITE),
                        PositionImpl(0, 7),
                        false,
                        Pawn(PositionImpl(0, 1), Color.WHITE)
                ),
                arrayOf(
                        Pawn(PositionImpl(1, 0), Color.WHITE),
                        PositionImpl(2, 1),
                        false,
                        null
                ),
                arrayOf(
                        Pawn(PositionImpl(1, 0), Color.WHITE),
                        PositionImpl(2, 0),
                        true,
                        null
                ),
                arrayOf(
                        Pawn(PositionImpl(1, 0), Color.WHITE),
                        PositionImpl(3, 0),
                        true,
                        null
                ),
                arrayOf(
                        Pawn(PositionImpl(1, 0), Color.WHITE),
                        PositionImpl(3, 0),
                        false,
                        Pawn(PositionImpl(2, 0), Color.WHITE)
                )
        )

        @JvmStatic
        fun beatParams() = arrayOf(
                arrayOf(Queen(Color.WHITE), PositionImpl(1, 3), true),
                arrayOf(Queen(Color.WHITE), PositionImpl(1, 2), true)
        )
    }

    @ParameterizedTest
    @MethodSource("singleFigureParams")
    internal fun singleFigureMoves(figure: Figure, to: Position, isValid: Boolean, obstacle: Figure? = null) {
        val from = figure.position
        table.setFigure(figure)
        if (obstacle != null)
            table.setFigure(obstacle)

        val move = { table.makeMove(Color.WHITE, from, to) }
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


    @ParameterizedTest
    @MethodSource("beatParams")
    internal fun beats(figure: Figure, position: Position, flag: Boolean) {
        assert(flag == figure.beats(table, position))
    }

    @Test
    fun failEatOwnFigure() {
        val from = PositionImpl(2, 3)
        table.setFigure(Pawn(from, Color.BLACK))
        val to = PositionImpl(2, 2)
        table.setFigure(Pawn(to, Color.BLACK))
        assertThrows<IllegalMoveException> {
            table.makeMove(Color.BLACK, from, to)
        }
    }


    @Test
    fun eatRivalFigure() {
        val from = PositionImpl(2, 2)
        val whitePawn = Pawn(from, Color.WHITE)
        table.setFigure(whitePawn)
        val to = PositionImpl(3, 3)
        table.setFigure(Pawn(to, Color.BLACK))

        table.makeMove(Color.WHITE, from, to)
        assertNull(table.getFigure(from))
        assertEquals(whitePawn, table.getFigure(to))
    }

    @Test
    internal fun successShortCastling() {
        val king = King(Color.WHITE)
        table.setFigure(king)
        val rook = Rook(PositionImpl(0, 0), Color.WHITE)
        table.setFigure(rook)
        table.makeMove(Color.WHITE, king.position, PositionImpl(0, 1))

        assertNull(table.getFigure(0, 0))
        assertEquals(king, PositionImpl(0, 1))
        assertEquals(rook, PositionImpl(0, 2))
        assertNull(table.getFigure(0, 3))
    }

    @Test
    internal fun successEatOnHop() {
        table.setFigure(Pawn(PositionImpl(1, 1), Color.WHITE))
        val blackPawn = Pawn(PositionImpl(3, 2), Color.BLACK)
        table.setFigure(blackPawn)
        table.makeMove(Color.WHITE, PositionImpl(1, 1), PositionImpl(3, 1))
        table.makeMove(Color.BLACK, PositionImpl(3, 2), PositionImpl(2, 1))

        val figures = table.figures
        assertEquals(1, figures.size)
        assertEquals(blackPawn, figures.iterator().next())
    }

    @Test
    internal fun reproduceNotRevertedMoves() {
        val whitePawn = Pawn(PositionImpl(1, 0), Color.WHITE)
        table.setFigure(whitePawn)
        val whiteKing = King(PositionImpl(0, 3), Color.WHITE)
        table.setFigure(whiteKing)
        val blackPawn = Pawn(PositionImpl(6, 0), Color.BLACK)
        table.setFigure(blackPawn)
        val blackKing = King(PositionImpl(7, 3), Color.BLACK)
        table.setFigure(blackKing)

        table.makeMove(Color.WHITE, whitePawn.position, PositionImpl(2, 0))

        assertEquals(PositionImpl(2, 0), whitePawn.position)
        assertEquals(PositionImpl(0, 3), whiteKing.position)
        assertEquals(PositionImpl(6, 0), blackPawn.position)
        assertEquals(PositionImpl(7, 3), blackKing.position)

    }

    @Test
    internal fun reproduceNoCheck() {
        table.fill()
        table.makeMove(Color.WHITE, PositionImpl(1, 4), PositionImpl(2, 4))
        table.makeMove(Color.BLACK, PositionImpl(7, 1), PositionImpl(5, 2))
        table.makeMove(Color.WHITE, PositionImpl(0, 3), PositionImpl(4, 7))
        assertThrows<IllegalMoveException> {
            table.makeMove(Color.BLACK, PositionImpl(6, 5), PositionImpl(5, 5))
        }
    }

    @Test
    internal fun testFoolsMate() {
        table.fill()
        table.makeMove(Color.WHITE, PositionImpl(1, 5), PositionImpl(2, 5))
        table.makeMove(Color.BLACK, PositionImpl(6, 4), PositionImpl(4, 4))
        table.makeMove(Color.WHITE, PositionImpl(1, 6), PositionImpl(3, 6))
        table.makeMove(Color.BLACK, PositionImpl(7, 3), PositionImpl(3, 7))
        assertEquals(GameState.CHECKMATE, table.state)
    }


    private fun printBoard() {
        table.figuresByColor.forEach {
            println(it.key)
            it.value.forEach(::println)
        }
    }
}