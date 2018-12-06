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

@Controller
public class Client {
    static public ClientTransport staticClientTransport;

    private Player player = null;
    private Table table = null;
    private GameResult result = null;
    final private ClientTransport client;
    private volatile Color playerColor = null;
    ExecutorService executors = Executors.newFixedThreadPool(1);

    public Client() {
        client = staticClientTransport;
    }

    public static void init() throws Exception {
        try {
            Server server = new Server(8088);
            server.setHandler(getServletContextHandler(Client.getContext()));
            server.start();
            server.join();
        } catch (Exception e) {
            Server server = new Server(8089);
            server.setHandler(getServletContextHandler(Client.getContext()));
            server.start();
            server.join();
        }
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
        // TODO если игрок вышел, то хорошо бы сообщить серверу, чтобы он обработал это событие
        // TODO например, если идет игра, послал бы сопернику какой-нибудь флаг
        player = null;
        table = null;
        result = null;
        playerColor = null;
        prepareModelMap(map, new Player(null, null));
        return "index";
    }

    @RequestMapping(value = "/new-game", method = RequestMethod.POST)
    public String createNewGame(@ModelAttribute("player") Player p, ModelMap map) throws IOException, ParseException, IllegalMoveException {
        table = new TableImpl();
        result = null;
        client.setTable(table);
        executors.submit(() -> {
            try {
                playerColor = client.joinGame();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        prepareModelMap(map, player, table, new RawMove(), "");
        // TODO Here new game button was pressed and need to send request to server
        return "rival_wait";
    }

    @RequestMapping(value = "/rival_wait", method = RequestMethod.POST)
    public String rivalWait(@ModelAttribute("player") Player p, ModelMap map) {
        if (playerColor != null) {
            // TODO: check if other player found and get playerColor
            prepareModelMap(map, player, table, playerColor, new RawMove(), "");
            if (playerColor == Color.WHITE){
                return "game";
            } else {
                executors.submit(() -> {
                    try {
                        client.waitForMove();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (IllegalMoveException e) {
                        e.printStackTrace();
                    }
                });
                return "move_wait";
            }
        } else {
            prepareModelMap(map, player, table, new RawMove(), "");
            return "rival_wait";
        }
    }

    @RequestMapping(value = "/make_move", method = RequestMethod.POST)
    public String makeMove(@ModelAttribute("move") RawMove move, ModelMap map) throws IOException, ParseException {
        try {
            Position from = parsePosition(move.getFrom());
            Position to = parsePosition(move.getTo());
            table.makeMove(playerColor, from, to);
            executors.submit(() -> {
                try {
                    client.sendMove(from, to);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (IllegalMoveException e) {
                    e.printStackTrace();
                }
            });
        } catch (IllegalMoveException e) {
            prepareModelMap(map, player, table, playerColor, new RawMove(), e.getMessage());
            return "game";
        }
        switch (table.getCurrentState()) {
            case NONE:
                prepareModelMap(map, player, table, playerColor, new RawMove(), "");
                return "move_wait";
            case CHECK:
                prepareModelMap(map, player, table, playerColor, new RawMove(), "");
                return "move_wait";
            case CHECKMATE:
                result = GameResult.WIN;
                player.addWin();
                Database.updatePlayer(player);
                prepareModelMap(map, player, table, result, playerColor);
                return "after_game";
            case STALEMATE:
                result = GameResult.DRAW;
                player.addDraw();
                Database.updatePlayer(player);
                prepareModelMap(map, player, table, result, playerColor);
                return "after_game";
               default:
                return "";
        }
    }

    @RequestMapping(value = "/move_wait", method = RequestMethod.POST)
    public String moveWait(ModelMap map) {
        if (table.getCurrentTurn() == playerColor) {
            switch (table.getCurrentState()) {
                case NONE:
                    prepareModelMap(map, player, table, playerColor, new RawMove(), "");
                    return "game";
                case CHECK:
                    prepareModelMap(map, player, table, playerColor, new RawMove(), "");
                    return "game";
                case CHECKMATE:
                    result = GameResult.LOSE;
                    player.addLose();
                    Database.updatePlayer(player);
                    prepareModelMap(map, player, table, result, playerColor);
                    return "after_game";
                case STALEMATE:
                    result = GameResult.DRAW;
                    player.addDraw();
                    Database.updatePlayer(player);
                    prepareModelMap(map, player, table, result, playerColor);
                    return "after_game";
                default:
                    return "";
            }
        } else {
            prepareModelMap(map, player, table, playerColor, new RawMove(), "");
            return "move_wait";
        }
    }

    @RequestMapping(value = "/stats", method = RequestMethod.POST)
    public String getStats(ModelMap map) {
        List<Player> top = Database.getTop();
        prepareModelMap(map, top);
        return "stats";
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
            column = col - 'a';
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
