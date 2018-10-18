// Table is kept and handled on server or client?
public interface Server extends Transport {
    void gameCreated(Transport white, Transport black);

    void receiveMove(Figure f, Position to);

    void sendMove(Figure f, Position to);

//    void sendTable(Table t);
}
