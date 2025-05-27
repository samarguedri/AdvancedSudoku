package advancedsudoku;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sudoku_game";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "samar123";

    public static void savePlayer(String userName) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT IGNORE INTO players (name, score) VALUES (?, 0)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, userName);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateScore(String userName, int score) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "UPDATE players SET score = ? WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, score);
                stmt.setString(2, userName);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

