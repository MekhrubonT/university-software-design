package controller;

import model.*;
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

public class ChessmateServer implements AutoCloseable {
    private final Selector selector;
    private final ServerSocketChannel serverSocket;
    private final Queue<ServerTransport> joinGameQueue = new LinkedList<>();
    private final Map<SocketChannel, ServerTransport> clientsTransport = new HashMap<>();
    private final Map<Transport, Table> currentGames = new HashMap<>();

    public ChessmateServer(int port) throws IOException {
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
            throws IOException, ParseException, IllegalMoveException, IllegalPositionException {
        SocketChannel client = (SocketChannel) key.channel();
        ServerTransport serverTransport = clientsTransport.get(client);
        serverTransport.receiveAction();
    }


    public void run() throws IOException, ParseException, IllegalMoveException, IllegalPositionException {
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

    public void joinGameRequest(ServerTransport black) throws IOException {
        while (!joinGameQueue.isEmpty() && !joinGameQueue.peek().getIsStillJoiningGame()) {
            joinGameQueue.poll();
        }
        if (joinGameQueue.isEmpty()) {
            joinGameQueue.add(black);
        } else {
            ServerTransport white = joinGameQueue.poll();
            TableImpl table = new TableImpl();
            currentGames.put(white, table);
            currentGames.put(black, table);

            white.startGame(Color.WHITE, black);
            black.startGame(Color.BLACK, white);
        }
    }

    public void disconnect(ServerTransport client) {
        currentGames.remove(client);
        clientsTransport.remove(client.getSocket());
        client.getSocket().keyFor(selector).cancel();
        // TODO: opponent wins a game
    }

    public void logout(ServerTransport client) {
        // TODO
    }

    @Override
    public void close() throws Exception {
        serverSocket.close();
    }

    public Table getGameTable(Transport transport) {
        return currentGames.get(transport);
    }
}
