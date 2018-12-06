package transports;

import model.Figure;
import model.IllegalMoveException;
import model.Position;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public interface Transport {
    void receiveMove(Position f, Position to) throws IllegalMoveException, IOException, ParseException;

    void sendMove(Position f, Position to) throws IOException, ParseException, IllegalMoveException;

//    void sendTable(model.Table t);

//    model.Table getTable();
}
