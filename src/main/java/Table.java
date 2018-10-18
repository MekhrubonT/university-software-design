import java.util.List;

public interface Table {
    GameState currentState();

    Figure getFigure(Position p);

    List<Figure> getAllFigures();
}
