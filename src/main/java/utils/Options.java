package utils;

public class Options {
    public enum ServerOrClient {
        SERVER, CLIENT
    }

    public final ServerOrClient appType;
    public final int port;
    public final int serverPort; // only for Client option
    public final String serverHost; // only for Client option

    private Options(ServerOrClient appType, int port, int serverPort, String serverHost) {
        this.appType = appType;
        this.port = port;
        this.serverPort = serverPort;
        this.serverHost = serverHost;
    }

    public static Options parseOptions(String[] args) throws OptionsParseException {
        int i = 0;

        ServerOrClient appType = null;
        int port = -1;
        int serverPort = -1;
        String serverHost = null;
        while (i < args.length) {
            if (args[i].equals("-s") || args[i].equals("-server")) {
                if (appType != null) {
                    throw new OptionsParseException("Redefinition of app type: " + args[i] + ", previous type: " + appType);
                }
                appType = ServerOrClient.SERVER;
                i++;
            } else if (args[i].equals("-c") || args[i].equals("-connection")) {
                if (appType != null) {
                    throw new OptionsParseException("Redefinition of app type: " + args[i] + ", previous type: " + appType);
                }
                appType = ServerOrClient.CLIENT;
                i++;
            } else if (args[i].equals("-port")) {
                port = parsePort(port, "port", args[i + 1]);
                i += 2;
            } else if (args[i].startsWith("-p")) {
                port = parsePort(port, "port", args[i].substring(2));
                i++;
            } else if (args[i].equals("-server_port")) {
                if (appType != ServerOrClient.CLIENT) {
                    throw new OptionsParseException("Server port should be set only for Client app type");
                }
                serverPort = parsePort(serverPort, "server port", args[i + 1]);
                i += 2;
            } else if (args[i].startsWith("-sp")) {
                if (appType != ServerOrClient.CLIENT) {
                    throw new OptionsParseException("Server port should be set only for Client app type");
                }
                serverPort = parsePort(serverPort, "server port", args[i].substring(3));
                i++;
            } else if (args[i].equals("-server_host")) {
                if (appType != ServerOrClient.CLIENT) {
                    throw new OptionsParseException("Server host should be set only for Client app type");
                }
                serverHost = args[i+1];
                i += 2;
            } else if (args[i].startsWith("-sh")) {
                if (appType != ServerOrClient.CLIENT) {
                    throw new OptionsParseException("Server host should be set only for Client app type");
                }
                serverHost = args[i].substring(3);
                i++;
            }else {
                throw new OptionsParseException("Unrecognised run argument: " + args[i]);
            }
        }
        if (port == -1) {
            throw new OptionsParseException("Port is not provided");
        }
        if (appType == ServerOrClient.CLIENT && serverPort == -1) {
            throw new OptionsParseException("Server port is not provided");
        }
        if (appType == ServerOrClient.CLIENT && serverHost == null) {
            throw new OptionsParseException("Server host is not provided");
        }
        return new Options(appType, port, serverPort, serverHost);
    }

    private static int parsePort(int oldPort, String portName, String port) throws OptionsParseException {
        if (oldPort != -1) {
            throw new OptionsParseException("Redifinition of " + portName);
        }
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            throw new OptionsParseException("Bad " + portName + " number: " + port);
        }
    }

    public static void printHelpMessage() {
        System.out.println("The chessmate app run arguments: \n" +
                "-server | -s              - to run server app\n" +
                "-connection | -c              - to run connection app\n" +
                "-port num | -pnum         - to set ran application port, num - port number\n" +
                "-server_port num | -spnum - server port for connection to connect, only connection apps\n" +
                "-server_host str | -shstr - server host for connection to connect, only connection apps\n" +
                "Examples:\n" +
                "java App -s -p8081\n" +
                "java App -c -sp8081 -port 8088\n" +
                "java App -connection -server_port 8081 -port 8089\n"
        );
    }
}
