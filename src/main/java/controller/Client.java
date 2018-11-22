package controller;

// TODO: need "model.Position from" everywhere to determine the move or model.Figure has info about its current position?

import db.Database;
import model.*;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
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
import web.WebConfig;

import java.io.IOException;

@Controller
public class Client {
    Player player = null;
    Table table = null;

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
        if (player != Player.EMPTY_PLAYER) {
            this.player = player;
            return "main";
        } else {
            return "login";
        }
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(@ModelAttribute("player") Player p, ModelMap map) throws IOException {
        Player player = Database.registerPlayer(p.getLogin(), p.getPassword());
        prepareModelMap(map, player);
        if (player != Player.EMPTY_PLAYER) {
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
        prepareModelMap(map, player, table);
        return "game";
    }

    void updatePlayerData(Player newData) {
        player.updateData(newData);
    }

    void makeMove(Figure f, Position to) {
    }

    void receivedMove(Figure f, Position to) {
    }

    //    void updateTable(Table t);

    public static void main(String[] args) throws Exception {
        Server server = new Server(8085);
        server.setHandler(getServletContextHandler(getContext()));
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

    private void prepareModelMap(ModelMap map, Player player) {
        map.addAttribute("player", player);
    }

    private void prepareModelMap(ModelMap map, Player player, Table table) {
        map.addAttribute("player", player);
        map.addAttribute("table", table);
    }

}
