package app.apiRESTful.dao;

import app.apiRESTful.model.User;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase `UserDAOSQL` para manejar las operaciones CRUD relacionadas con usuarios
 * en la base de datos utilizando JDBC. 
 * Las credenciales y URL de la base de datos se cargan desde variables de entorno.
 */
public class UserDAOSQL {

    // Carga las variables de entorno para la conexión a la base de datos
    private static final Dotenv dotenv = Dotenv.load();
    private static final String DB_URL = dotenv.get("DB_URL"); // URL de la base de datos.
    private static final String DB_USER = dotenv.get("DB_USER"); // Usuario de la base de datos.
    private static final String DB_PASSWORD = dotenv.get("DB_PASSWORD"); // Contraseña de la base de datos.

    /**
     * Establece una conexión con la base de datos.
     *
     * @return Un objeto `Connection` activo.
     * @throws SQLException Si ocurre un error durante la conexión.
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Crea un nuevo usuario en la base de datos.
     *
     * @param user Objeto `User` que contiene los datos del usuario a crear.
     * @return `true` si el usuario fue creado exitosamente, `false` en caso contrario.
     */
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername()); // Establece el nombre de usuario.
            statement.setString(2, user.getPassword()); // Establece la contraseña.
            return statement.executeUpdate() > 0; // Retorna true si se afectó al menos una fila.
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Retorna false si ocurre algún error.
        }
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id ID del usuario a buscar.
     * @return Un objeto `User` si se encuentra el usuario, o `null` si no existe.
     */
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id); // Establece el ID del usuario en la consulta.
            ResultSet resultSet = statement.executeQuery(); // Ejecuta la consulta.
            if (resultSet.next()) {
                User user = new User();
                user.setUsername(resultSet.getString("username")); // Asigna el nombre de usuario.
                user.setPassword(resultSet.getString("password")); // Asigna la contraseña.
                return user; // Retorna el usuario encontrado.
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Retorna null si no se encuentra el usuario o ocurre un error.
    }

    /**
     * Obtiene una lista de todos los usuarios registrados en la base de datos.
     *
     * @return Una lista de objetos `User` que representa a todos los usuarios.
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            // Itera sobre el resultado y agrega cada usuario a la lista.
            while (resultSet.next()) {
                User user = new User();
                user.setUsername(resultSet.getString("username")); // Asigna el nombre de usuario.
                user.setPassword(resultSet.getString("password")); // Asigna la contraseña.
                users.add(user); // Agrega el usuario a la lista.
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users; // Retorna la lista de usuarios.
    }

    /**
     * Actualiza los datos de un usuario existente en la base de datos.
     *
     * @param id   ID del usuario a actualizar.
     * @param user Objeto `User` que contiene los nuevos datos del usuario.
     * @return `true` si el usuario fue actualizado exitosamente, `false` en caso contrario.
     */
    public boolean updateUser(int id, User user) {
        String sql = "UPDATE users SET username = ?, password = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername()); // Establece el nuevo nombre de usuario.
            statement.setString(2, user.getPassword()); // Establece la nueva contraseña.
            statement.setInt(3, id); // Establece el ID del usuario a actualizar.
            return statement.executeUpdate() > 0; // Retorna true si se afectó al menos una fila.
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Retorna false si ocurre algún error.
        }
    }

    /**
     * Elimina un usuario de la base de datos.
     *
     * @param id ID del usuario a eliminar.
     * @return `true` si el usuario fue eliminado exitosamente, `false` en caso contrario.
     */
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id); // Establece el ID del usuario a eliminar.
            return statement.executeUpdate() > 0; // Retorna true si se afectó al menos una fila.
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Retorna false si ocurre algún error.
        }
    }
}
