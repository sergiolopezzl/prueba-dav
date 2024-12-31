package app.database;

import java.sql.*;
import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseHelper {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String DB_USER = dotenv.get("DB_USER");
    private static final String DB_PASSWORD = dotenv.get("DB_PASSWORD");

    // Conectar a la base de datos
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Verificar si un usuario existe
    public static boolean isValidUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
}