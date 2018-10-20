// TODO: need "Position from" everywhere to determine the move or Figure has info about its current position?

import java.io.IOException;

public class Client {
    Player player;

    void login() {
        // TODO: get from login page
        String login = "login";
        String password = "password";

        Player player = Database.getPlayer(login, password);
        if (player != Player.emptyPlayer) {
            this.player = player;
        } else {
            throw new RuntimeException("No player found with this login and password: " + login + ", " + password);
        }
    }

    void register() throws IOException {
        // TODO: get from login page
        String login = "login";
        String password = "password";

        Player player = Database.registerPlayer(login, password);
        if (player != Player.emptyPlayer) {
            this.player = player;
        } else {
            throw new RuntimeException("Player with this login is already exists: " + login);
        }
    }

    void updatePlayerData(Player newData) {
        player.updateData(newData);
    }

    void makeMove(Figure f, Position to) {
    }



    void receivedMove(Figure f, Position to) {
    }



//    void updateTable(Table t);
void createNewGame() {
}


}
