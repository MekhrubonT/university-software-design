import org.junit.Before;
import org.junit.BeforeClass;

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
        assertEquals(player, Player.emptyPlayer);
    }

    @org.junit.Test
    public void getNonExistingPlayer() throws Exception {
        Player player = Database.getPlayer("maxim", "longpassword");
        assertEquals(player, Player.emptyPlayer);
    }

    @org.junit.Test
    public void getExistingPlayer() throws Exception {
        Player player = Database.getPlayer("artem", "123456");
        assertEquals(player, new Player("artem", "123456"));
    }

    @org.junit.Test
    public void getExistingPlayerWithWrongPassword() throws Exception {
        Player player = Database.getPlayer("artem", "longpassword");
        assertEquals(player, Player.emptyPlayer);
    }

}