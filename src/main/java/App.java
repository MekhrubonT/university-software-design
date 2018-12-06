import controller.Client;
import controller.ChessmateServer;
import db.Database;
import org.json.simple.parser.ParseException;
import transports.ClientTransport;
import utils.Options;
import utils.OptionsParseException;

import java.net.BindException;
import java.net.ConnectException;

import static utils.Options.ServerOrClient.CLIENT;
import static utils.Options.ServerOrClient.SERVER;

public class App {
    public static void main(String[] args) throws Exception {
        try {
            Options options = Options.parseOptions(args);
            switch (options.appType) {
                case SERVER:
                    new App().runServer(options.port);
                    break;
                case CLIENT:
                    new App().runClient(options.serverPort, options.port);
                    break;
            }
        } catch (OptionsParseException e) {
            System.err.println(e.getMessage());
            Options.printHelpMessage();
        } catch (ConnectException e) {
            System.err.println("No server is ran on provided port");
        } catch (BindException e) {
            System.err.println("Provided port is already used");
        }
    }

    private void runServer(int port) throws Exception {
        System.out.println("App.runServer");
        Database.createDatabase();
        try (ChessmateServer server = new ChessmateServer(port)) {
            server.run();
        }
    }

    private void runClient(int serverPort, int uiPort) throws Exception {
        try (ClientTransport clientTransport = new ClientTransport(serverPort)) {
            Client.init(clientTransport, uiPort);
        }
    }
}
