package name;// Artem: model.Table is kept bot on server and clients. Send only moves.

import org.json.simple.parser.ParseException;
import transports.ServerTransport;
import transports.Transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Server implements AutoCloseable {
    private final Selector selector;
    private final ServerSocketChannel serverSocket;
    public final Queue<ServerTransport> joinGameQueue = new LinkedList<>();
    private final Map<SocketChannel, ServerTransport> clientsTransport = new HashMap<>();

    public Server(int port) throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void acceptClient(Selector selector, ServerSocketChannel serverSocket)
            throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        clientsTransport.put(client, new ServerTransport(client, this));
    }

    private void receiveClientAction(SelectionKey key)
            throws IOException, ParseException {
        SocketChannel client = (SocketChannel) key.channel();
        clientsTransport.get(client).receiveAction();
    }


    public void start() throws IOException, ParseException {
        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {

                SelectionKey key = iter.next();

                if (key.isAcceptable()) {
                    acceptClient(selector, serverSocket);
                }

                if (key.isReadable()) {
                    receiveClientAction(key);
                }

                iter.remove();
            }
        }
    }

    public void createGame(Transport white, Transport black) {
        // TODO: Mekhrubon
    }

    @Override
    public void close() throws Exception {
        serverSocket.close();
    }
}
