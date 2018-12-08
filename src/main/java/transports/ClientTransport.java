package transports;

import model.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import static transports.TransportConstants.*;

public class ClientTransport extends AbstractTransport {
    public static final int MOVE_NONE = 0;
    public static final int MOVE_DONE = 1;
    public static final int MOVE_CHEKMATE_WIN = 2;
    public static final int MOVE_STALEMATE = 4;
    public static final int MOVE_CHECKMATE_LOSE = 3;
    private Table table;

    public ClientTransport(int port) throws IOException {
        super(SocketChannel.open(new InetSocketAddress("localhost", port)));
        connection.configureBlocking(false);
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Table getTable() {
        return table;
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

        sendMessageJSON(login);
        while (true) {
            JSONObject object = waitForMessageJSON();
            if (object != null) {
                return object;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void sendMove(Position from, Position to) throws IOException, ParseException, IllegalMoveException, IllegalPositionException {
        System.out.println("ClientTransport.sendMove");
        super.sendMove(from, to);
    }

    public void joinGame() throws IOException {
        System.out.println("ClientTransport.joinGame");
        sendMessageJSON(JSON_JOIN_GAME_REQUEST);
    }

    @Override
    public void receiveMove(Position f, Position to) throws IllegalMoveException {
        System.out.println("ClientTransport.receiveMove " + f + " " + to);
        table.makeMove(table.getCurrentTurn(), f, to);
    }

    public void logout() throws IOException {
        table = null;
        sendMessageJSON(JSON_LOGOUT);
    }

    public int checkMove() throws IOException, ParseException, IllegalPositionException, IllegalMoveException {
        System.out.println("ClientTransport.checkMove");
        JSONObject response = waitForMessageJSON();
        if (response == null) {
            return MOVE_NONE;
        } if (JSON_RESPONSE_CHECKMATE.equals(response)) { // ignored
            return MOVE_CHEKMATE_WIN;
        } else if (JSON_RESPONSE_STALEMATE.equals(response)) { // ignored
            return MOVE_STALEMATE;
        } else if (TRANSPORT_ACTION_MOVE.equals(response.get(TRANSPORT_ACTION))) {
            receiveMove(
                    AbstractPosition.fromString(((String) response.get(TRANSPORT_ACTION_MOVE_FROM))),
                    AbstractPosition.fromString(((String) response.get(TRANSPORT_ACTION_MOVE_TO)))
            );
            if (table.getCurrentState() == GameState.CHECKMATE) {
                return MOVE_CHECKMATE_LOSE;
            } else if (table.getCurrentState() == GameState.STALEMATE) {
                return MOVE_STALEMATE;
            }
            return MOVE_DONE;
        } else {
            throw new RuntimeException("[false]");
        }
    }

    public Color receiveColor() throws IOException, ParseException {
        System.out.println("ClientTransport.receiveColor");
        JSONObject response = waitForMessageJSON();
        if (response == null) {
            return null;
        }
        if (JSON_COLOR_WHITE.equals(response)) {
            System.out.println("ClientTransport.receiveColor WHITE");
            return Color.WHITE;
        } else if (JSON_COLOR_BLACK.equals(response)) {
            System.out.println("ClientTransport.receiveColor BLACK");
            return Color.BLACK;
        } else {
            throw new RuntimeException("[false]");
        }
    }
}
