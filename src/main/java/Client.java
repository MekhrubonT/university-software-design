// TODO: need "model.Position from" everywhere to determine the move or model.Figure has info about its current position?

import model.Figure;
import model.Player;
import model.Position;

import java.io.IOException;

public class Client {
    Player player;

    public void login() {
        // TODO: get from login page
        String login = "login";
        String password = "password";

        Player player = Database.getPlayer(login, password);
        if (player != Player.EMPTY_PLAYER) {
            this.player = player;
        } else {
            throw new RuntimeException("No player found with this login and password: " + login + ", " + password);
        }
    }

    public void register() throws IOException {
        // TODO: get from login page
        String login = "login";
        String password = "password";

        Player player = Database.registerPlayer(login, password);
        if (player != Player.EMPTY_PLAYER) {
            this.player = player;
        } else {
            throw new RuntimeException("Player with this login is already exists: " + login);
        }
    }

    public void updatePlayerData(Player newData) {
        player.updateData(newData);
    }

    public void makeMove(Figure f, Position to) {
    }


    public void receivedMove(Figure f, Position to) {
    }


    //    void updateTable(model.Table t);
    public void createNewGame() {
    }


}
