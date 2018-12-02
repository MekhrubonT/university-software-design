package model;

import kotlin.sequences.Sequence;

public interface Figure {

//    boolean isAllowedMove(Position to);

    boolean isMine(Table.Color playerColor);

    Table.Color getColor();

    Position getPosition();

    void setPosition(Position to);

    boolean beats(Position position);

    Sequence<Sequence<Move>> getPossibleMoves(Table table);
}
