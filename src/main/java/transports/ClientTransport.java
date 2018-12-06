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

//    @Override
//    public void close() throws Exception {
//        if (client != null) {
//            try {
//                closeConnection();
//            } catch (Exception ignored) {
//            }
//        }
//        super.close();
//    }
//
//    private void closeConnection() throws IOException {
//        JSONObject object = new JSONObject();
//        object.put(TRANSPORT_ACTION, TRANSPORT_ACTION_CLOSE);
//        sendMessage(object.toJSONString());
//    }

    public ClientTransport(int port) throws IOException {
        super(SocketChannel.open(new InetSocketAddress("localhost", port)));
    }

    public JSONObject register(String login, String password) throws IOException, ParseException {
        JSONObject object = new JSONObject();
        object.put(TRANSPORT_ACTION, TRANSPORT_ACTION_REGISTER);
        object.put(TRANSPORT_LOGIN, login);
        object.put(TRANSPORT_PASSWORD, password);

        return sendMessageAndWaitForResponse(object.toJSONString());
    }

    @Override
    public void sendMove(Position from, Position to) throws IOException, ParseException, IllegalMoveException {
        super.sendMove(from, to);
        JSONObject response = waitForMessage();
        if (RESPONSE_CHECKMATE.equals(response)) {
            // TODO: Finish with checkmate
        } else if (RESPONSE_STALEMATE.equals(response)) {
            // TODO: Finish with stalemate
        } else if (RESPONSE_OK.equals(response)){
            waitForMove();
        } else {
            throw new RuntimeException("[false]");
        }
    }

    public void waitForMove() throws IOException, ParseException, IllegalMoveException {
        JSONObject move = waitForMessage();
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
        JSONObject object = new JSONObject();
        object.put(TRANSPORT_ACTION, TRANSPORT_ACTION_JOIN_GAME);

        JSONObject response = sendMessageAndWaitForResponse(object.toJSONString());
        if (COLOR_WHITE.equals(response)) {
            return Color.WHITE;
        } else if (COLOR_BLACK.equals(response)) {
            return Color.BLACK;
        } else {
            throw new RuntimeException("[false]");
        }
    }

    @Override
    public void receiveMove(Position f, Position to) throws IllegalMoveException {
        table.makeMove(table.getCurrentTurn(), f, to);
    }
    public void setTable(Table table) {
        this.table = table;
    }
}
