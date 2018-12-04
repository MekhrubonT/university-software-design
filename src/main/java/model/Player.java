package model;

import org.json.simple.JSONObject;

import static transports.TransportConstants.*;

/**
 * Created by -- on 20.10.2018.
 */
public class Player {
    private String login;
    private String password;
    private double rating = 0;
    private int wins = 0;
    private int draws = 0;
    private int loses = 0;

    public Player() {}

    public String toJson() {
        JSONObject player = new JSONObject();
        player.put(TRANSPORT_PLAYER_LOGIN, login);
        player.put(TRANSPORT_PLAYER_PASSWORD, login);
        player.put(TRANSPORT_PLAYER_RATING, rating);
        player.put(TRANSPORT_PLAYER_WINS, wins);
        player.put(TRANSPORT_PLAYER_DRAWS, draws);
        player.put(TRANSPORT_PLAYER_LOSES, loses);
        return player.toJSONString();
    }
    public static Player fromJson(JSONObject player) {
        System.out.println(player.toJSONString());
        String login = (String) player.get(TRANSPORT_PLAYER_LOGIN);
        String password = (String) player.get(TRANSPORT_PLAYER_PASSWORD);
        System.out.println(player.get(TRANSPORT_PLAYER_WINS));
        double rating = (double) player.get(TRANSPORT_PLAYER_RATING);
        int wins = ((Long) player.get(TRANSPORT_PLAYER_WINS)).intValue();
        int draws = ((Long) player.get(TRANSPORT_PLAYER_DRAWS)).intValue();
        int loses = ((Long) player.get(TRANSPORT_PLAYER_LOSES)).intValue();
        return new Player(login, password, rating, wins, draws, loses);
    }

    public Player(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public Player(String login, String password, double rating, int wins, int draws, int loses) {
        this.login = login;
        this.password = password;
        this.rating = rating;
        this.wins = wins;
        this.draws = draws;
        this.loses = loses;
    }

    public final static Player EMPTY_PLAYER = new Player("", "");

    public void addWin() {
        wins++;
        rating++;
    }

    public void addDraw() {
        draws++;
        rating += 0.5;
    }

    public void addLose() {
        loses++;
        rating += 0;
    }

    public void updateData(Player newData) {
        assert login.equals(newData.getLogin());
        setRating(newData.rating);
        setWins(newData.wins);
        setDraws(newData.draws);
        setLoses(newData.loses);
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public double getRating() {
        return rating;
    }

    public int getWins() {
        return wins;
    }

    public int getDraws() {
        return draws;
    }

    public int getLoses() {
        return loses;
    }

    public Player setRating(double rating) {
        this.rating = rating;
        return this;
    }

    public Player setWins(int wins) {
        this.wins = wins;
        return this;
    }

    public Player setDraws(int draws) {
        this.draws = draws;
        return this;
    }

    public Player setLoses(int loses) {
        this.loses = loses;
        return this;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;
        return login.equals(player.getLogin()) && password.equals(player.getPassword()) && rating == player.getRating()
                && wins == player.getWins() && draws == player.getDraws() && loses == player.getLoses();
    }

    public String toString() {
        return "Login: " + login + "\n" +
                "Password: " + password + "\n" +
                "Rating: " + rating + "\n" +
                "Wins: " + wins + "\n" +
                "Draws: " + draws + "\n" +
                "Loses: " + loses + "\n";
    }
}
