package model;

import java.util.List;

public interface Table {
    GameState getCurrentState();

    Color getCurrentTurn();

    Figure getFigure(Position p);

    Figure getFigure(String row, String column);

    void setFigure(Figure figure);

    List<Figure> getAllFigures();

    List<Figure> getBlackFigures();

    List<Figure> getWhiteFigures();

    void makeMove(Color playerColor, Position from, Position to) throws IllegalMoveException;

}
