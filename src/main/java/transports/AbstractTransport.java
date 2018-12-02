package transports;

import model.AbstractPosition;
import model.Figure;
import model.Position;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static transports.TransportConstants.*;

public abstract class AbstractTransport implements Transport, AutoCloseable {
    final protected SocketChannel client;

    public AbstractTransport(SocketChannel client) throws IOException {
        this.client = client;
    }

    public void sendMessage(String msg) throws IOException {
        ByteBuffer wrap = ByteBuffer.wrap(msg.getBytes());
        client.write(wrap);
    }

    public void sendMessageAndWaitForResponseOk(String msg) throws IOException, ParseException {
        JSONObject response = sendMessageAndWaitForResponse(msg);

        if (!RESPONSE_OK.equals(response)) {
            throw new RuntimeException((String) response.get(TRANSPORT_RESULT));
        }
    }

    public JSONObject sendMessageAndWaitForResponse(String msg) throws IOException, ParseException {
        sendMessage(msg);
        return waitForMessage();
    }

    protected JSONObject waitForMessage() throws IOException, ParseException {
        ByteBuffer wrap = ByteBuffer.allocate(256);
        client.read(wrap);
        return (JSONObject) new JSONParser().parse(wrap.toString());
    }

    @Override
    public void close() throws Exception {
        client.close();
    }

    public void sendMove(Position from, Position to) throws IOException, ParseException {
        JSONObject object = new JSONObject();
        object.put(TRANSPORT_ACTION, TRANSPORT_ACTION_MOVE);
        object.put(TRANSPORT_ACTION_MOVE_FROM, from.toString());
        object.put(TRANSPORT_ACTION_MOVE_TO, to.toString());

        sendMessage(object.toString());
    }
}