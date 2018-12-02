package transports;

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

    public void loginOrRegister(String hashedToken) throws IOException, ParseException {
        JSONObject object = new JSONObject();
        object.put(TRANSPORT_ACTION, TRANSPORT_ACTION_LOGIN_OR_REGISTER);
        object.put(TRANSPORT_TOKEN, hashedToken);

        sendMessageAndWaitForResponseOk(object.toJSONString());
    }

    public void joinGame() throws IOException, ParseException {
        JSONObject object = new JSONObject();
        object.put(TRANSPORT_ACTION, TRANSPORT_ACTION_JOIN_GAME);

        JSONObject response = sendMessageAndWaitForResponse(object.toJSONString());
        if (COLOR_WHITE.equals(response)) {
            // TODO: Mekhrubon
        } else if (COLOR_BLACK.equals(response)) {
            // TODO: Mekhrubon
        } else {
            // TODO: Mekhrubon
        }
    }

    @Override
    public void receiveMove(Position f, Position to) {
        // TODO: Mekhrubon
    }
}
