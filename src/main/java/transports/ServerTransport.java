package transports;

import model.*;
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
    private Table.Color color;
    private AbstractTransport opponent;

    public ServerTransport(SocketChannel client, Server server) throws IOException {
        super(client);
        this.server = server;
    }

    public void receiveAction() throws IOException, ParseException, IllegalMoveException {
        ByteBuffer buffer = ByteBuffer.allocate(256);
        client.read(buffer);
        parseAction(new String(buffer.array()));
    }


    private void parseAction(String actionJSON) throws ParseException, IOException, IllegalMoveException {
        JSONObject msg = (JSONObject) new JSONParser().parse(actionJSON);
        String action = (String) msg.get(TransportConstants.TRANSPORT_ACTION);
        if (action == null) {
            return;
        }
        switch (action) {
            case TRANSPORT_ACTION_LOGIN_OR_REGISTER:
                loginOrRegister((String) msg.get(TransportConstants.TRANSPORT_TOKEN));
                break;
            case TRANSPORT_ACTION_JOIN_GAME:
                joinGame();
                break;
            case TRANSPORT_ACTION_MOVE:
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
        sendMessage(RESPONSE_OK.toJSONString());
    }
    private void joinGame() throws IOException {
        if (server.joinGameQueue.isEmpty()) {
            server.joinGameQueue.add(this);
        } else {
            ServerTransport black = server.joinGameQueue.poll();
            server.createGame(this, black);

            sendMessage(COLOR_WHITE.toJSONString());
            this.color = Table.Color.WHITE;
            opponent = black;

            black.sendMessage(COLOR_BLACK.toJSONString());
            black.color = Table.Color.BLACK;
            black.opponent = this;
        }
    }

    @Override
    public void receiveMove(Position from, Position to) throws IllegalMoveException, IOException, ParseException {
        Table table = server.getGameTable(this);
        table.makeMove(color, from, to);
        switch (table.getCurrentState()) {
            case CHECKMATE:
                sendMessage(RESPONSE_CHECKMATE.toJSONString());
                opponent.sendMessage(RESPONSE_CHECKMATE.toJSONString());
                break;
            case STALEMATE:
                sendMessage(RESPONSE_STALEMATE.toJSONString());
                opponent.sendMessage(RESPONSE_STALEMATE.toJSONString());
                break;
            default:
                sendMessage(RESPONSE_OK.toJSONString());
                opponent.sendMove(from, to);
        }
    }
}
