package transports;

import model.AbstractPosition;
import model.Position;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import static transports.TransportConstants.*;

public class ClientTransport extends AbstractTransport {

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
    public void sendMove(Position from, Position to) throws IOException, ParseException {
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

    protected void waitForMove() throws IOException, ParseException {
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

    public void joinGame() throws IOException, ParseException {
        JSONObject object = new JSONObject();
        object.put(TRANSPORT_ACTION, TRANSPORT_ACTION_JOIN_GAME);

        JSONObject response = sendMessageAndWaitForResponse(object.toJSONString());
        if (COLOR_WHITE.equals(response)) {
            // TODO: Mekhrubon need to make a move

        } else if (COLOR_BLACK.equals(response)) {
            // TODO: Mekhrubon
            waitForMove();
        } else {
            // TODO: Mekhrubon some error or what?
            throw new RuntimeException("[false]");
        }
    }

    @Override
    public void receiveMove(Position f, Position to) {
        // TODO: Mekhrubon
    }
}
