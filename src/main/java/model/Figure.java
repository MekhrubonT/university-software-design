package model;

public interface Figure {
    void makeMove(Position to);

    void isAllowedMove(Position to);

    boolean isMine();
}
