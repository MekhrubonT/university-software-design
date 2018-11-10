package model


import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TableImplTest {
    val table = TableImpl()

    @BeforeEach
    internal fun setUp() {
        table.clear()
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
        assertEquals(null, table.getFigure(from))
        assertEquals(whitePawn, table.getFigure(to))
    }
}