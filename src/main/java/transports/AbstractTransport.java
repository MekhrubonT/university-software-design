package transports;

import model.IllegalMoveException;
import model.IllegalPositionException;
import model.Position;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static transports.TransportConstants.*;

public abstract class AbstractTransport implements Transport, AutoCloseable {
    final protected SocketChannel connection;
    String buffer = "";

    public AbstractTransport(SocketChannel client) throws IOException {
        this.connection = client;
    }

    public void sendMessage(String msg) throws IOException {
        ByteBuffer wrap = ByteBuffer.wrap(msg.getBytes());
        connection.write(wrap);
    }
    public void sendMessageJSON(JSONObject msgJSON) throws IOException {
        sendMessage(msgJSON.toJSONString());
    }

    public JSONObject sendMessageAndWaitForResponseJSON(JSONObject msgJSON) throws IOException, ParseException {
        sendMessageJSON(msgJSON);
        return waitForMessageJSON();
    }

    public JSONObject waitForMessageJSON() throws IOException, ParseException {
        ByteBuffer wrap = ByteBuffer.allocate(256);
        int amount = connection.read(wrap);
        if (amount > 0) {
            buffer += new String(wrap.array(), 0, amount);
        }
        int pos = buffer.indexOf('}');
        if (pos == -1) {
            return null;
        }
        String msg = buffer.substring(0, pos + 1);
        buffer = buffer.substring(pos + 1);
        System.out.println("AbstractTransport.waitForMessageJSON " + msg);
        return (JSONObject) new JSONParser().parse(msg);
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    public void sendMove(Position from, Position to) throws IOException, ParseException, IllegalMoveException, IllegalPositionException {
        JSONObject move = new JSONObject();
        move.put(TRANSPORT_ACTION, TRANSPORT_ACTION_MOVE);
        move.put(TRANSPORT_ACTION_MOVE_FROM, from.toString());
        move.put(TRANSPORT_ACTION_MOVE_TO, to.toString());

        sendMessageJSON(move);
    }
}