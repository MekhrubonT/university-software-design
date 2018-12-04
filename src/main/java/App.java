import controller.Client;
import controller.ChessmateServer;
import db.Database;
import transports.ClientTransport;

public class App {
    private static ClientTransport clientTransport;

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
                new App().runServer(options.port);
                break;
            case CLIENT:
                new App().runClient(options.port);
                break;
        }
    }

    private void runServer(int port) throws Exception {
        System.out.println("App.runServer");
        Database.createDatabase();
        try (ChessmateServer server = new ChessmateServer(port)) {
            server.run();
        }
    }

    private void runClient(int port) throws Exception {
        try (ClientTransport clientTransport = new ClientTransport(port)) {
            Client.staticClientTransport = clientTransport;
            Client.init();
        }
    }

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
}
