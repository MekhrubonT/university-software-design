// Mekh: Table is kept and handled on server or client?
// Artem: Table is kept bot on server and clients. Send only moves.

import java.io.IOException;

public class Server implements Transport {

    public Server() throws IOException {
        Database.createDatabase();
    }

    void gameCreated(Transport white, Transport black) {
    }



    public void receiveMove(Figure f, Position to) {
    }



    public void sendMove(Figure f, Position to) {
    }



//    void sendTable(Table t);
}
