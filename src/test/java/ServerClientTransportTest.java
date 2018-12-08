import controller.ChessmateServer;
import db.Database;
import model.Color;
import model.Player;
import model.TableImpl;
import org.jetbrains.annotations.NotNull;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import transports.ClientTransport;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static model.AbstractPosition.fromString;
import static model.Player.fromJSON;
import static org.junit.jupiter.api.Assertions.*;
import static transports.ClientTransport.*;

public class ServerClientTransportTest {
    final static int SERVER_PORT = 8080;
    private static final int TIME_SECOND = 1000;

    private void baseTest(TransportsTest test) throws Exception {
        try (
                ChessmateServer chessmateServer = doRunServer(SERVER_PORT);
                ClientTransport client0 = doRunClient(SERVER_PORT);
                ClientTransport client1 = doRunClient(SERVER_PORT);
        ) {
            Thread serverThread = runOnThread(chessmateServer::run);
            serverThread.start();
            test.runTest(serverThread, Arrays.asList(client0, client1));
            serverThread.join(10 * TIME_SECOND);
        }

    }

    @NotNull
    private Thread runOnThread(Task t) {
        return new Thread(t::run);
    }

    private ChessmateServer doRunServer(int port) throws IOException {
        return new ChessmateServer(port);
    }

    private ClientTransport doRunClient(int serverPort) throws IOException {
        return new ClientTransport("localhost", serverPort);
    }

    @Test
    public void runServerAndClients() throws Exception {
        baseTest(clients -> {
        });
    }

    @Test
    public void clientRegister() throws Exception {
        String login = "mekhrubon_test_client_register";
        String password = "password";

        baseTest(clients -> {
            Player player = fromJSON(clients.get(0).register(login, password));
            assertEquals(player, new Player(login, password));
            Database.removePlayer(player);
        });
    }

    @Test
    public void clientLogin() throws Exception {
        String login = "mekhrubon_test_client_login";
        String password = "password";

        Player player = Database.registerPlayer(login, password);
        baseTest(clients -> {
            Player logined = fromJSON(clients.get(0).login(login, password));
            assertEquals(logined, player);
        });
        Database.removePlayer(player);
    }

    @Test
    public void joinGame() throws Exception {
        String login = "mekhrubon_test_join_game";
        String password = "password";

        baseTest(clients -> {
            Color[] colors = doJoinGame(clients.get(0), clients.get(1));
            assertNotNull(colors[0]);
            assertNotNull(colors[1]);
            assertNotEquals(colors[0], colors[1]);
        });
        Database.removePlayer(new Player(login + "1", password));
        Database.removePlayer(new Player(login + "2", password));
    }

    private Color[] doJoinGame(ClientTransport playerTransport0, ClientTransport playerTransport1) throws IOException, ParseException, InterruptedException {
        String login = "mekhrubon_test_join_game";
        String password = "password";

        Player player0 = fromJSON(playerTransport0.register(login + "1", password));
        Player player1 = fromJSON(playerTransport1.register(login + "2", password));
        Color colors[] = new Color[2];
        playerTransport0.joinGame();
        playerTransport1.joinGame();
        System.out.println("ServerClientTransportTest.doJoinGame FIRST");
        while ((colors[0] = playerTransport0.receiveColor()) == null) {
            Thread.sleep(TIME_SECOND / 2);
        }
        System.out.println("ServerClientTransportTest.doJoinGame SECOND");
        while ((colors[1] = playerTransport1.receiveColor()) == null) {
            Thread.sleep(TIME_SECOND / 2);
        }


        return colors;
    }

    private interface TransportsTest {
        default void runTest(Thread server, List<ClientTransport> clients) throws Exception {
            doTest(clients);
            server.interrupt();
        }

        void doTest(List<ClientTransport> clients) throws Exception;

    }

    interface Task {
        default void run() {
            try {
                doRun();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void doRun() throws Exception;
    }

    @Test
    public void makeTwoMoves() throws Exception {
        String login = "mekhrubon_test_join_game";
        String password = "password";

        baseTest(clients -> {
            Color[] colors = doJoinGame(clients.get(0), clients.get(1));
            ClientTransport white = colors[0] == Color.WHITE ? clients.get(0) : clients.get(1);
            ClientTransport black = colors[0] == Color.BLACK ? clients.get(0) : clients.get(1);
            TableImpl table = new TableImpl();
            white.setTable(table);
            black.setTable(table);

            int res;
            white.sendMove(fromString("a2"), fromString("a4"));
            while ((res = black.checkMove()) == MOVE_NONE) {
                Thread.sleep(TIME_SECOND / 2);
            }
            assertEquals(MOVE_DONE, res);
            black.sendMove(fromString("h7"), fromString("h6"));
            while ((res = white.checkMove()) == MOVE_NONE) {
                Thread.sleep(TIME_SECOND / 2);
            }
            assertEquals(MOVE_DONE, res);
            white.logout();
            while ((res = black.checkMove()) == MOVE_NONE) {
                Thread.sleep(TIME_SECOND / 2);
            }
            assertEquals(MOVE_CHEKMATE_WIN, res);
        });
        Database.removePlayer(new Player(login + "1", password));
        Database.removePlayer(new Player(login + "2", password));
    }
}
