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

    public ServerTransport getOpponent() {
        return opponent;
    }

    public Color getColor() {
        return color;
    }

    public void receiveAction() throws IOException, ParseException, IllegalMoveException, IllegalPositionException {
        ByteBuffer buffer = ByteBuffer.allocate(256);
        int amount = connection.read(buffer);
        if (amount == -1) {
            System.out.println("ServerTransport.receiveAction client connection closed");
            disconnect();
            return;
        }
        parseAction(new String(buffer.array(), 0, amount));
    }

    private void parseAction(String actionJSON) throws ParseException, IOException, IllegalMoveException, IllegalPositionException {
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
                break;
            case TRANSPORT_ACTION_LOGOUT:
                logout();
                break;
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

    private void disconnect() throws IOException {
        logout();
        server.disconnect(this);
    }

    private void logout() throws IOException {
        server.logout(this);
        isStillJoiningGame = false;
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
        sendMessageJSON(color == Color.WHITE ? JSON_COLOR_WHITE : JSON_COLOR_BLACK);
    }
    public void finishGame() {
        this.color = null;
        this.opponent = null;
    }

    public boolean getIsStillJoiningGame() {
        return isStillJoiningGame;
    }

    @Override
    public void receiveMove(Position from, Position to) throws IllegalPositionException, IllegalMoveException, IOException, ParseException {
        System.out.println("ServerTransport.receiveMove");
        Table table = server.getGameTable(this);
        if (table == null) {
            return;
        }
        table.makeMove(color, from, to);
        switch (table.getCurrentState()) {
            case CHECKMATE:
                win(true);
                opponent.lose(true);
                finishGame();
                break;
            case STALEMATE:
                draw(true);
                opponent.draw(true);
                finishGame();
                break;
            default:
                opponent.sendMove(from, to);
        }
    }
    public void win(boolean sendMessage) throws IOException {
        finishGame();
        player.addWin();
        Database.updatePlayer(player);
        if (sendMessage) {
            sendMessageJSON(JSON_RESPONSE_CHECKMATE);
        }
    }

    public void lose(boolean sendMessage) throws IOException {
        finishGame();
        player.addLose();
        Database.updatePlayer(player);
        if (sendMessage) {
            sendMessageJSON(JSON_RESPONSE_CHECKMATE);
        }
    }

    private void draw(boolean sendMessage) throws IOException {
        finishGame();
        player.addDraw();
        Database.updatePlayer(player);
        if (sendMessage) {
            sendMessageJSON(JSON_RESPONSE_STALEMATE);
        }
    }

    public SocketChannel getSocket() {
        return connection;
    }
}
