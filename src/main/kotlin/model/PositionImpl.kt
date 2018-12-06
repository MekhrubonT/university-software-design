package model

class PositionImpl(row: Int, column: Int) : AbstractPosition() {
    private val coordinates = Pair(row, column)

    init {
        if (!isValid)
            throw IllegalArgumentException("invalid coordinates: $coordinates")
    }

    override fun isValid(): Boolean = row in (0..7) && col in (0..7)

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