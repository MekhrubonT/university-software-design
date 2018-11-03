import model.Figure;
import model.Position;

public interface Transport {
    void receiveMove(Figure f, Position to);

    void sendMove(Figure f, Position to);

//    void sendTable(Table t);

//    Table getTable();
}
