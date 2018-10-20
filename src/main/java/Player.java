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

    public final static Player emptyPlayer = new Player("", "");

    void addWin() {
        wins++;
        rating++;
    }

    void addDraw() {
        draws++;
        rating += 0.5;
    }

    void addLose() {
        loses++;
        rating += 0;
    }

    void updateData(Player newData) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return login.equals(player.getLogin()) && password.equals(player.getPassword()) && rating == player.getRating()
                && wins == player.getWins() && draws == player.getDraws() && loses == player.getLoses();
    }
}
