public interface Client {
    void makeMove(Figure f, Position to);

    void receivedMove(Figure f, Position to);

//    void updateTable(Table t);
    void createNewGame();
}
