package model

class PositionImpl(row: Int, column: Int) : AbstractPosition() {
    override fun isValid(): Boolean = row in (0..7) && col in (0..7)

    private val coordinates = Pair(row, column)

    override fun getRow() = coordinates.first

    override fun getCol() = coordinates.second

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PositionImpl

        if (coordinates != other.coordinates) return false

        return true
    }

    override fun hashCode(): Int {
        return coordinates.hashCode()
    }
}