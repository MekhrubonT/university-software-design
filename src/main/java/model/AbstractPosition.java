package model;

public abstract class AbstractPosition implements Position {
    final public String toString() {
        return String.format("%d,%d", getRow(), getCol());
    }

    static public Position fromString(String position) {
        String[] split = position.split(",");
        if (split.length != 2) {
            throw new IllegalArgumentException(position + " should have %d,%d format");
        }
        int row = Integer.parseInt(split[0]);
        int col = Integer.parseInt(split[1]);
        return new PositionImpl(row, col);
    }
}
