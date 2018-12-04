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

@Controller
public class Client {
    static public ClientTransport staticClientTransport;

    private Player player = null;
    private Table table = null;
    private GameResult result = null;
    final private ClientTransport client;

    public Client() {
        client = staticClientTransport;
    }

    public static void init() throws Exception {
        Server server = new Server(8089);
        server.setHandler(getServletContextHandler(Client.getContext()));
        server.start();
        server.join();
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
    public String gotoMain(@ModelAttribute("player") Player p, ModelMap map) throws IOException {
        prepareModelMap(map, p);
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
    public String login(@ModelAttribute("player") Player p, ModelMap map) {
        Player player = Database.getPlayer(p.getLogin(), p.getPassword());
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
        Player player = Player.fromJson(client.register(p.getLogin(), p.getPassword()));
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
        player = null;
        prepareModelMap(map, new Player(null, null));
        return "index";
    }

    @RequestMapping(value = "/new-game", method = RequestMethod.POST)
    public String createNewGame(@ModelAttribute("player") Player p, ModelMap map) {
        table = new TableImpl();
        result = null;
        prepareModelMap(map, player, table, new RawMove(), "");
        return "game";
    }

    @RequestMapping(value = "/make_move", method = RequestMethod.POST)
    public String makeMove(@ModelAttribute("move") RawMove move, ModelMap map) {
        try {
            Position from = parsePosition(move.getFrom());
            Position to = parsePosition(move.getTo());
            table.makeMove(table.getCurrentTurn(), from, to);
        } catch (IllegalMoveException e) {
            prepareModelMap(map, player, table, new RawMove(), e.getMessage());
            return "game";
        }
        switch (table.getCurrentState()) {
            case NONE:
                prepareModelMap(map, player, table, new RawMove(), "");
                return "game";
            case CHECK:
                prepareModelMap(map, player, table, new RawMove(), "");
                return "game";
            case CHECKMATE:
                result = GameResult.WIN;
                player.addWin();
                Database.updatePlayer(player);
                prepareModelMap(map, player, table, result);
                return "after_game";
            case STALEMATE:
                result = GameResult.DRAW;
                player.addDraw();
                Database.updatePlayer(player);
                prepareModelMap(map, player, table, result);
                return "after_game";
            default:
                return "";
        }
    }

    private Position parsePosition(String pos) throws IllegalMoveException {
        int row;
        int column;
        try {
            char col = pos.charAt(0);
            row = Integer.parseInt(pos.substring(1, 2)) - 1;
            if (col > 'h' || col < 'a' || row > 7 || row < 0) {
                throw new IllegalMoveException("Illegal string for move position: " + pos);
            }
            column = 'h' - col;
        } catch (Exception e) {
            throw new IllegalMoveException("Illegal string for move position: " + pos);
        }
        return new PositionImpl(row, column);
    }

    void updatePlayerData(Player newData) {
        player.updateData(newData);
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

    private void prepareModelMap(ModelMap map, Player player) {
        map.addAttribute("player", player);
    }

    private void prepareModelMap(ModelMap map, Player player, Table table, GameResult result) {
        map.addAttribute("player", player);
        map.addAttribute("table", table);
        map.addAttribute("result", result);
    }

    private void prepareModelMap(ModelMap map, Player player, Table table, RawMove move, String exception) {
        map.addAttribute("player", player);
        map.addAttribute("table", table);
        map.addAttribute("move", move);
        map.addAttribute("exception", exception);
    }
}
