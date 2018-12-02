package transports;

import model.Figure;
import model.Position;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public interface Transport {
    void receiveMove(Position f, Position to);

    void sendMove(Figure f, Position to) throws IOException, ParseException;

//    void sendTable(model.Table t);

//    model.Table getTable();
}
