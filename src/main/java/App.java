import name.Server;
import transports.ClientTransport;

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
        try (Server server = new Server(port)) {
            server.start();
        }
    }

    private static void runClient(int port) throws Exception {
        System.out.println("App.runClient");
        try (ClientTransport client = new ClientTransport(port)) {
        }
    }
}
