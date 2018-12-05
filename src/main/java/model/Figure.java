package model;

import kotlin.sequences.Sequence;

public interface Figure {
    void afterMove();

    boolean hasMoved();

    boolean isMine(Color playerColor);

    Color getColor();

    Position getPosition();

    void setPosition(Position to);

    boolean beats(Table table, Position position);

    Sequence<Move> getPossibleMoves(Table table);

    String representation();

    String colorToString();
}
