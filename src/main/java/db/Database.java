package db;

import model.GameResult;
import model.Player;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by -- on 20.10.2018.
 */
public class Database {
    public static Player registerPlayer(String login, String password) throws IOException {
        int count = DatabaseHelper.databaseQuery("SELECT COUNT(*) FROM PLAYERS WHERE LOGIN=\"" + login + "\"",
                rs -> rs.next() ? rs.getInt(1) : 0);
        if (count == 0) {
            DatabaseHelper.databaseUpdate("INSERT INTO PLAYERS " +
                    "(LOGIN, PASSWORD, RATING, WINS, DRAWS, LOSES) VALUES (\"" + login + "\",\"" + password + "\",0,0,0,0)");
            return new Player(login, password);
        } else {
            return Player.EMPTY_PLAYER;
        }

    }

    public static Player getPlayer(String login, String password) {
        int count = DatabaseHelper.databaseQuery("SELECT COUNT(*) FROM PLAYERS WHERE LOGIN=\"" + login + "\" AND PASSWORD=\"" + password + "\"",
                rs -> rs.next() ? rs.getInt(1) : 0);
        if (count == 0) {
            return Player.EMPTY_PLAYER;
        } else {
            Player player = new Player(login, password);
            DatabaseHelper.databaseQuery("SELECT * FROM PLAYERS WHERE LOGIN=\"" + login + "\" AND PASSWORD=\"" + password + "\"",
                    (DatabaseHelper.CheckedConsumer<ResultSet>) rs -> player.setRating(rs.getDouble("RATING")).setWins(rs.getInt("WINS")).setDraws(rs.getInt("DRAWS")).setLoses(rs.getInt("LOSES")));
            return player;
        }
    }

    public static void updateAfterGame(Player p1, Player p2, GameResult res) {
        String sql;
        switch (res) {
            case WIN:
                sql = "UPDATE PLAYERS SET RATING=" + (p1.getRating() + 1) + ", WINS=" + (p1.getWins() + 1) + " WHERE LOGIN=\"" + p1.getLogin() + "\"";
                DatabaseHelper.databaseUpdate(sql);
                sql = "UPDATE PLAYERS SET RATING=" + (p2.getRating() + 0) + ", LOSES=" + (p2.getLoses() + 1) + " WHERE LOGIN=\"" + p2.getLogin() + "\"";
                DatabaseHelper.databaseUpdate(sql);
                break;
            case DRAW:
                sql = "UPDATE PLAYERS SET RATING=" + (p1.getRating() + 0.5) + ", DRAWS=" + (p1.getDraws() + 1) + " WHERE LOGIN=\"" + p1.getLogin() + "\"";
                DatabaseHelper.databaseUpdate(sql);
                sql = "UPDATE PLAYERS SET RATING=" + (p2.getRating() + 0.5) + ", DRAWS=" + (p2.getDraws() + 1) + " WHERE LOGIN=\"" + p2.getLogin() + "\"";
                DatabaseHelper.databaseUpdate(sql);
                break;
            case LOSE:
                sql = "UPDATE PLAYERS SET RATING=" + (p1.getRating() + 0) + ", LOSES=" + (p1.getLoses() + 1) + " WHERE LOGIN=\"" + p1.getLogin() + "\"";
                DatabaseHelper.databaseUpdate(sql);
                sql = "UPDATE PLAYERS SET RATING=" + (p2.getRating() + 1) + ", WINS=" + (p2.getWins() + 1) + " WHERE LOGIN=\"" + p2.getLogin() + "\"";
                DatabaseHelper.databaseUpdate(sql);
                break;
        }
    }

    public static List<Player> getTop() {
        String sql = "SELECT * FROM PLAYERS ORDER BY RATING DESC LIMIT 10";
        List<Player> top = new ArrayList<>();
        DatabaseHelper.databaseQuery(sql,
                rs -> {
                    while (rs.next()) {
                        top.add(new Player(rs.getString("LOGIN"), rs.getString("PASSWORD"),
                                rs.getDouble("RATING"), rs.getInt("WINS"),
                                rs.getInt("DRAWS"), rs.getInt("LOSES")));
                    }
                });
        return top;
    }

    public static void createDatabase() throws IOException {
        DatabaseHelper.databaseUpdate("CREATE TABLE IF NOT EXISTS PLAYERS" +
                "(LOGIN    TEXT PRIMARY KEY NOT NULL, " +
                " PASSWORD TEXT             NOT NULL, " +
                " RATING   DOUBLE           NOT NULL, " +
                " WINS     INT              NOT NULL, " +
                " DRAWS    INT              NOT NULL, " +
                " LOSES    INT              NOT NULL)");
    }
}
