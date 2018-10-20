import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by -- on 20.10.2018.
 */
public class Database {
    static Player registerPlayer(String login, String password) throws IOException {
        int count = DatabaseHelper.databaseQuery("SELECT COUNT(*) FROM PLAYERS WHERE LOGIN=\"" + login + "\"",
                rs -> rs.next() ? rs.getInt(1) : 0);
        if (count == 0) {
            DatabaseHelper.databaseUpdate("INSERT INTO PLAYERS " +
                    "(LOGIN, PASSWORD, RATING, WINS, DRAWS, LOSES) VALUES (\"" + login + "\",\"" + password + "\",0,0,0,0)");
            return new Player(login, password);
        } else {
            return Player.emptyPlayer;
        }

    }

    static Player getPlayer(String login, String password) {
        int count = DatabaseHelper.databaseQuery("SELECT COUNT(*) FROM PLAYERS WHERE LOGIN=\"" + login + "\" AND PASSWORD=\"" + password + "\"",
                rs -> rs.next() ? rs.getInt(1) : 0);
        if (count == 0) {
            return Player.emptyPlayer;
        } else {
            Player player = new Player(login, password);
            DatabaseHelper.databaseQuery("SELECT * FROM PLAYERS WHERE LOGIN=\"" + login + "\" AND PASSWORD=\"" + password + "\"",
                    (DatabaseHelper.CheckedConsumer<ResultSet>) rs -> player.setRating(rs.getDouble("RATING")).setWins(rs.getInt("WINS")).setDraws(rs.getInt("DRAWS")).setLoses(rs.getInt("LOSES")));
            return player;
        }
    }

    static void updateAfterGame(Player p1, Player p2, GameResult res) {
        // TODO
    }

    static List<Player> getTop() {
        // TODO
        return new ArrayList<>();
    }

    static void createDatabase() throws IOException {
        DatabaseHelper.databaseUpdate("CREATE TABLE IF NOT EXISTS PLAYERS" +
                "(LOGIN    TEXT PRIMARY KEY NOT NULL, " +
                " PASSWORD TEXT             NOT NULL, " +
                " RATING   DOUBLE           NOT NULL, " +
                " WINS     INT              NOT NULL, " +
                " DRAWS    INT              NOT NULL, " +
                " LOSES    INT              NOT NULL)");
    }
}
