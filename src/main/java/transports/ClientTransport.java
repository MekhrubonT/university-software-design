package transports;

import model.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import static transports.TransportConstants.*;

public class ClientTransport extends AbstractTransport {
    private Table table;

    public ClientTransport(int port) throws IOException {
        super(SocketChannel.open(new InetSocketAddress("localhost", port)));
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public JSONObject register(String username, String password) throws IOException, ParseException {
        return getPlayer(username, password, TRANSPORT_ACTION_REGISTER);
    }

    public JSONObject login(String username, String password) throws IOException, ParseException {
        return getPlayer(username, password, TRANSPORT_ACTION_LOGIN);
    }

    private JSONObject getPlayer(String username, String password, String transportAction) throws IOException, ParseException {
        JSONObject login = new JSONObject();
        login.put(TRANSPORT_ACTION, transportAction);
        login.put(TRANSPORT_LOGIN, username);
        login.put(TRANSPORT_PASSWORD, password);

        return sendMessageAndWaitForResponseJSON(login);
    }


    @Override
    public void sendMove(Position from, Position to) throws IOException, ParseException, IllegalMoveException, IllegalPositionException {
        System.out.println("ClientTransport.sendMove");
        super.sendMove(from, to);
        JSONObject response = waitForMessageJSON();
        if (JSON_RESPONSE_CHECKMATE.equals(response)) { // ignored
        } else if (JSON_RESPONSE_STALEMATE.equals(response)) { // ignored
        } else if (JSON_RESPONSE_OK.equals(response)){
            waitForMove();
        } else {
            throw new RuntimeException("[false]");
        }
    }

    public void waitForMove() throws IOException, ParseException, IllegalMoveException, IllegalPositionException {
        System.out.println("ClientTransport.waitForMove");
        JSONObject move = waitForMessageJSON();
        if (TRANSPORT_ACTION_MOVE.equals(move.get(TRANSPORT_ACTION))) {
            receiveMove(
                    AbstractPosition.fromString(((String) move.get(TRANSPORT_ACTION_MOVE_FROM))),
                    AbstractPosition.fromString(((String) move.get(TRANSPORT_ACTION_MOVE_TO)))
                    );
        } else {
            throw new RuntimeException("[false]");
        }
    }

    public Color joinGame() throws IOException, ParseException {
        JSONObject response = sendMessageAndWaitForResponseJSON(JSON_JOIN_GAME_REQUEST);
        if (JSON_COLOR_WHITE.equals(response)) {
            return Color.WHITE;
        } else if (JSON_COLOR_BLACK.equals(response)) {
            return Color.BLACK;
        } else {
            throw new RuntimeException("[false]");
        }
    }

    @Override
    public void receiveMove(Position f, Position to) throws IllegalMoveException {
        table.makeMove(table.getCurrentTurn(), f, to);
    }

    public void logout() throws IOException {
        table = null;
        sendMessageJSON(JSON_LOGOUT);
    }
}
