package app.apiRESTful.dao;

import app.apiRESTful.model.User;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOSQL {
 
    private static final Dotenv dotenv = Dotenv.load();
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String DB_USER = dotenv.get("DB_USER");
    private static final String DB_PASSWORD = dotenv.get("DB_PASSWORD");

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Crear un usuario
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Obtener un usuario por ID
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                User user = new User();
                user.setUsername(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Obtener todos los usuarios
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                User user = new User();
                user.setUsername(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Actualizar un usuario
    public boolean updateUser(int id, User user) {
        String sql = "UPDATE users SET username = ?, password = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setInt(3, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Eliminar un usuario
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
