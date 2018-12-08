package controller;

import db.Database;
import model.*;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.json.simple.parser.ParseException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import transports.ClientTransport;
import web.WebConfig;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static model.AbstractPosition.fromString;
import static transports.ClientTransport.*;

@Controller
public class ChessmateClient {
    static public ClientTransport staticClientTransport;
    final private ClientTransport client;
    private Player player = null;
    private Table table = null;
    private GameResult result = null;
    private volatile Color playerColor = null;
    private ExecutorService executors = Executors.newFixedThreadPool(1);

    public ChessmateClient() {
        client = staticClientTransport;
    }

    public static void init(ClientTransport clientTransport, int uiPort) throws Exception {
        staticClientTransport = clientTransport;
        Server server = new Server(uiPort);
        server.setHandler(getServletContextHandler(ChessmateClient.getContext()));
        server.start();
        server.join();
    }

    private static ServletContextHandler getServletContextHandler(WebApplicationContext context) throws IOException {
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");

        contextHandler.addServlet(new ServletHolder(new JspServlet()), "*.jsp");
        contextHandler.addServlet(new ServletHolder(new DispatcherServlet(context)), "/");

        contextHandler.addEventListener(new ContextLoaderListener(context));
        contextHandler.setResourceBase(new ClassPathResource(".").getURI().toString());
        contextHandler.setClassLoader(Thread.currentThread().getContextClassLoader());

        return contextHandler;
    }

    private static WebApplicationContext getContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(WebConfig.class);
        return context;
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index(ModelMap map) {
        if (player != null && player != Player.EMPTY_PLAYER) {
            prepareModelMap(map, player);
            return "main";
        }
        prepareModelMap(map, new Player(null, null));
        return "index";
    }

    @RequestMapping(value = "/main", method = RequestMethod.GET)
    public String main(ModelMap map) {
        if (player != null && player != Player.EMPTY_PLAYER) {
            prepareModelMap(map, player);
            return "main";
        }
        prepareModelMap(map, new Player(null, null));
        return "index";
    }

    @RequestMapping(value = "/goto_main", method = RequestMethod.POST)
    public String gotoMain(@ModelAttribute("player") Player p, ModelMap map) {
        table = null;
        result = null;
        playerColor = null;
        prepareModelMap(map, player);
        return "main";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(ModelMap map) {
        if (player != null && player != Player.EMPTY_PLAYER) {
            prepareModelMap(map, player);
            return "main";
        }
        prepareModelMap(map, new Player(null, null));
        return "login";
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String register(ModelMap map) {
        if (player != null && player != Player.EMPTY_PLAYER) {
            prepareModelMap(map, player);
            return "main";
        }
        prepareModelMap(map, new Player(null, null));
        return "register";
    }

    @RequestMapping(value = "/goto-login", method = RequestMethod.POST)
    public String gotoLogin(ModelMap map) {
        prepareModelMap(map, new Player(null, null));
        return "login";
    }

    @RequestMapping(value = "/goto-register", method = RequestMethod.POST)
    public String gotoRegister(ModelMap map) {
        prepareModelMap(map, new Player(null, null));
        return "register";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@ModelAttribute("player") Player p, ModelMap map) throws IOException, ParseException {
        Player player = Player.fromJSON(client.login(p.getLogin(), p.getPassword()));
        prepareModelMap(map, player);
        if (!Player.EMPTY_PLAYER.equals(player)) {
            this.player = player;
            return "main";
        } else {
            return "login";
        }
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(@ModelAttribute("player") Player p, ModelMap map) throws IOException, ParseException {
        Player player = Player.fromJSON(client.register(p.getLogin(), p.getPassword()));
        prepareModelMap(map, player);
        if (!Player.EMPTY_PLAYER.equals(player)) {
            this.player = player;
            return "main";
        } else {
            return "register";
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public String logout(@ModelAttribute("player") Player p, ModelMap map) throws IOException {
        // TODO если игрок вышел, то хорошо бы сообщить серверу, чтобы он обработал это событие
        // TODO например, если идет игра, послал бы сопернику какой-нибудь флаг
        client.logout();
        player = null;
        table = null;
        result = null;
        playerColor = null;
        prepareModelMap(map, new Player(null, null));
        return "index";
    }

    @RequestMapping(value = "/new-game", method = RequestMethod.POST)
    public String createNewGame(@ModelAttribute("player") Player p, ModelMap map) throws IOException {
        table = new TableImpl();
        result = null;
        playerColor = null;
        client.setTable(table);
        client.joinGame();
        prepareModelMap(map, player, table, new RawMove(), "");
        return "rival_wait";
    }

    @RequestMapping(value = "/rival_wait", method = RequestMethod.POST)
    public String rivalWait(@ModelAttribute("player") Player p, ModelMap map) throws IOException, ParseException {
        playerColor = client.receiveColor();

        if (playerColor != null) {
            // TODO: check if other player found and get playerColor
            prepareModelMap(map, player, table, playerColor, new RawMove(), "");
            if (playerColor == Color.WHITE) {
                return "game";
            } else {
                return "move_wait";
            }
        }

        prepareModelMap(map, player, table, new RawMove(), "");
        return "rival_wait";
    }

    @RequestMapping(value = "/make_move", method = RequestMethod.POST)
    public String makeMove(@ModelAttribute("move") RawMove move, ModelMap map) {
        System.out.println("ChessmateClient.makeMove");
        try {
            Position from = fromString(move.getFrom());
            Position to = fromString(move.getTo());
            table.makeMove(playerColor, from, to);
            client.sendMove(from, to);
        } catch (IllegalMoveException | IllegalPositionException | IOException | ParseException e) {
            prepareModelMap(map, player, table, playerColor, new RawMove(), e.getMessage());
            return "game";
        }
        prepareModelMap(map, player, table, playerColor, new RawMove(), "");
        return "move_wait";
    }

    @RequestMapping(value = "/move_wait", method = RequestMethod.POST)
    public String moveWait(ModelMap map) throws IllegalMoveException, IllegalPositionException, ParseException, IOException {
        System.out.println("ChessmateClient.moveWait");

        switch (client.checkMove()) {
            case MOVE_NONE:
                prepareModelMap(map, player, table, playerColor, new RawMove(), "");
                return "move_wait";
            case MOVE_DONE:
                prepareModelMap(map, player, table, playerColor, new RawMove(), "");
                return "game";
            case MOVE_CHEKMATE_WIN:
                result = GameResult.WIN;
                player.addWin();
                break;
            case MOVE_CHECKMATE_LOSE:
                result = GameResult.LOSE;
                player.addLose();
                break;
            case MOVE_STALEMATE:
                result = GameResult.DRAW;
                player.addDraw();
                break;
            default:
                throw new RuntimeException("[false]");
        }

        prepareModelMap(map, player, table, result, playerColor);
        return "after_game";
    }

    @RequestMapping(value = "/stats", method = RequestMethod.POST)
    public String getStats(ModelMap map) {
        List<Player> top = Database.getTop();
        prepareModelMap(map, top);
        return "stats";
    }

    private void prepareModelMap(ModelMap map, Player player) {
        map.addAttribute("player", player);
    }

    private void prepareModelMap(ModelMap map, List<Player> top) {
        map.addAttribute("top", top);
    }

    private void prepareModelMap(ModelMap map, Player player, Table table, GameResult result, Color playerColor) {
        map.addAttribute("player", player);
        map.addAttribute("table", table);
        map.addAttribute("result", result);
        map.addAttribute("color", playerColor);
    }

    private void prepareModelMap(ModelMap map, Player player, Table table, RawMove move, String exception) {
        map.addAttribute("player", player);
        map.addAttribute("table", table);
        map.addAttribute("move", move);
        map.addAttribute("exception", exception);
    }

    private void prepareModelMap(ModelMap map, Player player, Table table, Color playerColor, RawMove move, String exception) {
        map.addAttribute("player", player);
        map.addAttribute("table", table);
        map.addAttribute("color", playerColor);
        map.addAttribute("move", move);
        map.addAttribute("exception", exception);
    }
}
