package transports;

import model.IllegalMoveException;
import model.IllegalPositionException;
import model.Position;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public interface Transport {
    void receiveMove(Position f, Position to) throws IllegalMoveException, IOException, ParseException, IllegalPositionException;

    void sendMove(Position f, Position to) throws IOException, ParseException, IllegalMoveException, IllegalPositionException;

//    void sendTable(model.Table t);

//    model.Table getTable();
}
