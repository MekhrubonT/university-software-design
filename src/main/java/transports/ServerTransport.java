package transports;

import model.AbstractPosition;
import model.Position;
import name.Server;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static transports.TransportConstants.*;

public class ServerTransport extends AbstractTransport {
    final Server server;

    public ServerTransport(SocketChannel client, Server server) throws IOException {
        super(client);
        this.server = server;
    }

    public void receiveAction() throws IOException, ParseException {
        ByteBuffer buffer = ByteBuffer.allocate(256);
        client.read(buffer);
        String clientMessage = new String(buffer.array());
        int newLinePos;
        while ((newLinePos = clientMessage.indexOf(System.lineSeparator())) != -1) {
            String action = clientMessage.substring(newLinePos);
            clientMessage = clientMessage.substring(newLinePos + 1);
            parseAction(action);
        }
    }


    private void parseAction(String actionJSON) throws ParseException, IOException {
        JSONObject msg = (JSONObject) new JSONParser().parse(actionJSON);
        String action = (String) msg.get(TransportConstants.TRANSPORT_ACTION);
        if (action == null) {
            return;
        }
        switch (action) {
            case TransportConstants.TRANSPORT_ACTION_LOGIN_OR_REGISTER:
                loginOrRegister((String) msg.get(TransportConstants.TRANSPORT_TOKEN));
                break;
            case TransportConstants.TRANSPORT_ACTION_JOIN_GAME:
                joinGame();
                break;
            case TransportConstants.TRANSPORT_ACTION_MOVE:
                receiveMove(
                        AbstractPosition.fromString(((String) msg.get(TRANSPORT_ACTION_MOVE_FROM))),
                        AbstractPosition.fromString((String) msg.get(TRANSPORT_ACTION_MOVE_TO))
                );
                break;
            default:
                throw new RuntimeException("Bad action exception");
        }
    }

    void loginOrRegister(String userToken) throws IOException {
        sendJSONMessage(RESPONSE_OK);
    }
    private void sendJSONMessage(JSONObject msg) throws IOException {
        String s = msg.toJSONString();
        client.write(ByteBuffer.wrap(s.getBytes()));
    }

    private void joinGame() throws IOException {
        if (server.joinGameQueue.isEmpty()) {
            server.joinGameQueue.add(this);
        } else {
            ServerTransport black = server.joinGameQueue.poll();
            server.createGame(this, black);
            sendJSONMessage(COLOR_WHITE);
            black.sendJSONMessage(COLOR_BLACK);
        }
    }

    @Override
    public void receiveMove(Position from, Position to) {
        // TODO: Mekhrubon
    }
}
