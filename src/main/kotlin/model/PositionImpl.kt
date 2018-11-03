package model

class PositionImpl(row: Int, column: Int) : Position {
    override fun isValid(): Boolean = row in (0..7) && col in (0..7)

    private val coordinates = Pair(row, column)

    override fun getRow() = coordinates.first

    override fun getCol() = coordinates.second
}