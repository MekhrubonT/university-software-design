import db.Database;
import controller.Server;
import controller.Client;

public class App {
    enum ServerOrClient {
        SERVER, CLIENT
    }
    static class Options {
        ServerOrClient serverOrClient;
        int port;

        Options(ServerOrClient serverOrClient, int port) {
            this.serverOrClient = serverOrClient;
            this.port = port;
        }
    }

    private static Options parseOptions(String[] args) {
        ServerOrClient serverOrClient = "server".equals(args[0]) ? ServerOrClient.SERVER : ServerOrClient.CLIENT;
        int port = Integer.parseInt(args[1]);
        return new Options(serverOrClient, port);
    }

    public static void main(String[] args) throws Exception {
        Options options = parseOptions(args);
        System.out.println(options.serverOrClient + " " + options.port);
        switch (options.serverOrClient) {
            case SERVER:
                runServer(options.port);
                break;
            case CLIENT:
                runClient(options.port);
                break;
        }
    }

    private static void runServer(int port) throws Exception {
        System.out.println("App.runServer");
        Database.createDatabase();
        try (Server server = new Server(port)) {
            server.run();
        }
    }

    private static void runClient(int port) throws Exception {
        System.out.println("App.runClient");
        int uIPort = 8100;
        try (Client client = new Client(uIPort, port)) {
            client.join();
        }
    }
}
