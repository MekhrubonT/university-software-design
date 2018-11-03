import model.Figure;
import model.Position;

public interface Transport {
    void receiveMove(Figure f, Position to);

    void sendMove(Figure f, Position to);

//    void sendTable(model.Table t);

//    model.Table getTable();
}
