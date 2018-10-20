import java.util.List;

public interface Table {
    GameState currentState();

    Figure getFigure(Position p);

    // TODO: separately need to get black and white figures
    List<Figure> getAllFigures();
}
