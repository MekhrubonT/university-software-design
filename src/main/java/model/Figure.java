package model;

import kotlin.sequences.Sequence;

public interface Figure {
    void makeMove(Position to);

    boolean isAllowedMove(Position to);

    boolean isMine(Table.Color playerColor);

    Position getPosition();

    void setPosition(Position to);

    boolean beats(Position position);

    void eat();

    Sequence<Sequence<Move>> getPossibleMoves();
}
