import org.junit.Before;
import org.junit.BeforeClass;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by -- on 20.10.2018.
 */
public class DatabaseTest {

    @BeforeClass
    public static void allSetUp() throws Exception {
        Database.createDatabase();
    }

    @Before
    public void setUp() throws Exception {
        DatabaseHelper.databaseUpdate("DELETE FROM PLAYERS");
        DatabaseHelper.databaseUpdate("INSERT INTO PLAYERS " +
                "(LOGIN, PASSWORD, RATING, WINS, DRAWS, LOSES) VALUES (\"artem\",\"123456\",0,0,0,0)");
        DatabaseHelper.databaseUpdate("INSERT INTO PLAYERS " +
                "(LOGIN, PASSWORD, RATING, WINS, DRAWS, LOSES) VALUES (\"mekh\",\"000000\",0,0,0,0)");
        DatabaseHelper.databaseUpdate("INSERT INTO PLAYERS " +
                "(LOGIN, PASSWORD, RATING, WINS, DRAWS, LOSES) VALUES (\"roman\",\"qwerty\",0,0,0,0)");

    }

    @org.junit.Test
    public void registerNonExistingPlayer() throws Exception {
        Player player = Database.registerPlayer("maxim", "longpassword");
        assertEquals(player, new Player("maxim", "longpassword"));
    }

    @org.junit.Test
    public void registerExistingPlayer() throws Exception {
        Player player = Database.registerPlayer("artem", "longpassword");
        assertEquals(player, Player.EMPTY_PLAYER);
    }

    @org.junit.Test
    public void getNonExistingPlayer() throws Exception {
        Player player = Database.getPlayer("maxim", "longpassword");
        assertEquals(player, Player.EMPTY_PLAYER);
    }

    @org.junit.Test
    public void getExistingPlayer() throws Exception {
        Player player = Database.getPlayer("artem", "123456");
        assertEquals(player, new Player("artem", "123456"));
    }

    @org.junit.Test
    public void getExistingPlayerWithWrongPassword() throws Exception {
        Player player = Database.getPlayer("artem", "longpassword");
        assertEquals(player, Player.EMPTY_PLAYER);
    }

    @org.junit.Test
    public void updateAfterWin() throws Exception {
        Player p1 = new Player("artem", "123456");
        Player p2 = new Player("mekh", "000000");
        Database.updateAfterGame(p1, p2, GameResult.WIN);
        p1.setRating(p1.getRating() + 1);
        p1.setWins(p1.getWins() + 1);
        p2.setRating(p2.getRating() + 0);
        p2.setLoses(p2.getLoses() + 1);
        assertEquals(p1, Database.getPlayer("artem", "123456"));
        assertEquals(p2, Database.getPlayer("mekh", "000000"));
    }

    @org.junit.Test
    public void updateAfterDraw() throws Exception {
        Player p1 = new Player("artem", "123456");
        Player p2 = new Player("roman", "qwerty");
        Database.updateAfterGame(p1, p2, GameResult.DRAW);
        p1.setRating(p1.getRating() + 0.5);
        p1.setDraws(p1.getDraws() + 1);
        p2.setRating(p2.getRating() + 0.5);
        p2.setDraws(p2.getDraws() + 1);
        assertEquals(p1, Database.getPlayer("artem", "123456"));
        assertEquals(p2, Database.getPlayer("roman", "qwerty"));
    }

    @org.junit.Test
    public void updateAfterLose() throws Exception {
        Player p1 = new Player("roman", "qwerty");
        Player p2 = new Player("mekh", "000000");
        Database.updateAfterGame(p1, p2, GameResult.LOSE);
        p1.setRating(p1.getRating() + 0);
        p1.setLoses(p1.getLoses() + 1);
        p2.setRating(p2.getRating() + 1);
        p2.setWins(p2.getWins() + 1);
        assertEquals(p1, Database.getPlayer("roman", "qwerty"));
        assertEquals(p2, Database.getPlayer("mekh", "000000"));
    }

    @org.junit.Test
    public void getTop() throws Exception {
        Player p1 = new Player("artem", "123456");
        Player p2 = new Player("mekh", "000000");
        Player p3 = new Player("roman", "qwerty");

        Database.updateAfterGame(p1, p2, GameResult.WIN);
        p1 = Database.getPlayer(p1.getLogin(), p1.getPassword());
        p2 = Database.getPlayer(p2.getLogin(), p2.getPassword());
        Database.updateAfterGame(p1, p3, GameResult.DRAW);
        p1 = Database.getPlayer(p1.getLogin(), p1.getPassword());
        p3 = Database.getPlayer(p3.getLogin(), p3.getPassword());
        Database.updateAfterGame(p3, p2, GameResult.LOSE);
        p3 = Database.getPlayer(p3.getLogin(), p3.getPassword());
        p2 = Database.getPlayer(p2.getLogin(), p2.getPassword());
        List<Player> top = Database.getTop();

        for (Player p : top) {
            System.out.println(p);
        }

        assertEquals(3, top.size());
        assertEquals("artem", top.get(0).getLogin());
        assertEquals("mekh", top.get(1).getLogin());
        assertEquals("roman", top.get(2).getLogin());
    }

}