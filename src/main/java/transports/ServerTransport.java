package transports;

import controller.ChessmateServer;
import db.Database;
import model.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static transports.TransportConstants.*;

public class ServerTransport extends AbstractTransport {
    final private ChessmateServer server;
    private Color color;
    private ServerTransport opponent;
    private boolean isStillJoiningGame = false;
    private Player player;

    public ServerTransport(SocketChannel client, ChessmateServer server) throws IOException {
        super(client);
        this.server = server;
    }

    public void receiveAction() throws IOException, ParseException, IllegalMoveException {
        ByteBuffer buffer = ByteBuffer.allocate(256);
        int amount = connection.read(buffer);
        if (amount == -1) {
            server.disconnect(this);
            isStillJoiningGame = false;
            return;
        }
        parseAction(new String(buffer.array(), 0, amount));
    }


    private void parseAction(String actionJSON) throws ParseException, IOException, IllegalMoveException {
        System.out.println("ServerTransport.parseAction");
        System.out.println(actionJSON);
        JSONObject msgJSON = (JSONObject) new JSONParser().parse(actionJSON);
        String action = (String) msgJSON.get(TransportConstants.TRANSPORT_ACTION);
        if (action == null) {
            throw new RuntimeException("Bad action exception: " + action);
        }
        switch (action) {
            case TRANSPORT_ACTION_REGISTER:
                register(
                        (String) msgJSON.get(TransportConstants.TRANSPORT_LOGIN),
                        (String) msgJSON.get(TransportConstants.TRANSPORT_PASSWORD)
                );
                break;
            case TRANSPORT_ACTION_LOGIN:
                login(
                        (String) msgJSON.get(TransportConstants.TRANSPORT_LOGIN),
                        (String) msgJSON.get(TransportConstants.TRANSPORT_PASSWORD)
                );
            case TRANSPORT_ACTION_JOIN_GAME:
                joinGame();
                break;
            case TRANSPORT_ACTION_MOVE:
                receiveMove(
                        AbstractPosition.fromString(((String) msgJSON.get(TRANSPORT_ACTION_MOVE_FROM))),
                        AbstractPosition.fromString((String) msgJSON.get(TRANSPORT_ACTION_MOVE_TO))
                );
                break;
            default:
                throw new RuntimeException("Bad action exception: " + action);
        }
    }

    void register(String login, String password) throws IOException {
        System.out.println("ServerTransport.register");
        player = Database.registerPlayer(login, password);
        sendMessageJSON(player.toJSON());
    }

    void login(String login, String password) throws IOException {
        System.out.println("ServerTransport.login");
        player = Database.getPlayer(login, password);
        sendMessageJSON(player.toJSON());
    }

    private void joinGame() throws IOException {
        System.out.println("ServerTransport.joinGame");
        isStillJoiningGame = true;
        server.joinGameRequest(this);
    }

    public void startGame(Color color, ServerTransport opponent) throws IOException {
        System.out.println("ServerTransport.startGame");
        this.color = color;
        this.opponent = opponent;
        sendMessageJSON(color == Color.WHITE ? COLOR_WHITE : COLOR_BLACK);
    }
    public void finishGame() {
        this.color = null;
        this.opponent = null;
    }

    public boolean getIsStillJoiningGame() {
        return isStillJoiningGame;
    }

    @Override
    public void receiveMove(Position from, Position to) throws IllegalMoveException, IOException, ParseException {
        System.out.println("ServerTransport.receiveMove");
        Table table = server.getGameTable(this);
        table.makeMove(color, from, to);
        switch (table.getCurrentState()) {
            case CHECKMATE:
                player.addWin();
                opponent.player.addLose();

                sendMessageJSON(RESPONSE_CHECKMATE);
                opponent.sendMessageJSON(RESPONSE_CHECKMATE);
                finishGame();
                break;
            case STALEMATE:
                player.addDraw();
                opponent.player.addDraw();

                sendMessageJSON(RESPONSE_STALEMATE);
                opponent.sendMessageJSON(RESPONSE_STALEMATE);
                finishGame();
                break;
            default:
                sendMessageJSON(RESPONSE_OK);
                opponent.sendMove(from, to);
        }
    }

    public SocketChannel getSocket() {
        return connection;
    }
}
