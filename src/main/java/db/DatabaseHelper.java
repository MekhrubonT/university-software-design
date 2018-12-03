package db;

import java.io.IOException;
import java.sql.*;

/**
 * Created by -- on 20.10.2018.
 */
public class DatabaseHelper {
    private final static String URL = "jdbc:postgresql://192.168.1.35:5432/chess";
    private final static String USER = "chessmaster";
    private final static String PASSWORD = "chess";

    private final static String LOCAL_URL = "jdbc:sqlite:test.db";

    private static void databaseRequest(CheckedConsumer<Statement> func) {
        try (Connection c = DriverManager.getConnection(LOCAL_URL, USER, PASSWORD)) {
            Statement stmt = c.createStatement();

            func.accept(stmt);
            stmt.close();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int databaseRequest(CheckedFunction<Statement, Integer> func) {
        try (Connection c = DriverManager.getConnection(LOCAL_URL, USER, PASSWORD)) {
            Statement stmt = c.createStatement();

            int res = func.apply(stmt);
            stmt.close();
            return res;
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void databaseUpdate(String sql) {
        databaseRequest((Statement stmt) -> stmt.executeUpdate(sql));
    }

    public static void databaseQuery(String sql, CheckedConsumer<ResultSet> consumer) {
        databaseRequest((Statement stmt) -> {
            ResultSet result = stmt.executeQuery(sql);
            consumer.accept(result);
            result.close();
        });
    }

    public static int databaseQuery(String sql, CheckedFunction<ResultSet, Integer> consumer) {
        return databaseRequest((Statement stmt) -> {
            ResultSet result = stmt.executeQuery(sql);
            int res = consumer.apply(result);
            result.close();
            return res;
        });
    }

    @FunctionalInterface
    public interface CheckedConsumer<T> {
        void accept(T t) throws IOException, SQLException;
    }

    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply(T t) throws IOException, SQLException;
    }
}
