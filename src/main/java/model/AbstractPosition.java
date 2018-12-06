package model;

public abstract class AbstractPosition implements Position {
    final public String toString() {
        return String.format("%c%c", getCol() + 'a', getRow() + '1');
    }

    static public Position fromString(String position) throws IllegalPositionException {
        if (position.length() != 2) {
            throw new IllegalPositionException("Illegal string for position: " + position);
        }
        int column = position.charAt(0) - 'a';
        int row = position.charAt(1) - '1';
        if (column < 0 || column > 7 || row < 0 || row > 7) {
            throw new IllegalPositionException("Illegal string for position: " + position);
        }
        return new PositionImpl(row, column);
    }
}
