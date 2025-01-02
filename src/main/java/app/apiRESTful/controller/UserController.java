package app.apiRESTful.controller;

import com.sun.net.httpserver.HttpExchange;
import app.apiRESTful.dao.UserDAOSQL;
import app.apiRESTful.model.User;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Controlador para gestionar operaciones relacionadas con usuarios.
 * Implementa métodos para manejar solicitudes HTTP (GET, POST, PUT, DELETE) 
 * y delega operaciones a un DAO (Data Access Object) para interactuar con la base de datos.
 */
public class UserController {
    private final UserDAOSQL userDAOSQL;

    /**
     * Constructor que inicializa el DAO utilizado para acceder a los datos de usuarios.
     *
     * @param userDAOSQL Instancia de UserDAOSQL que se usará para las operaciones de base de datos.
     */
    public UserController(UserDAOSQL userDAOSQL) {
        this.userDAOSQL = userDAOSQL;
    }

    /**
     * Añade encabezados CORS (Cross-Origin Resource Sharing) a la respuesta HTTP.
     * Permite solicitudes desde cualquier origen y especifica métodos y encabezados permitidos.
     *
     * @param exchange Objeto HttpExchange que representa la solicitud y la respuesta.
     */
    public void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
    }

    /**
     * Maneja solicitudes GET para obtener todos los usuarios.
     *
     * @param exchange Objeto HttpExchange que contiene la solicitud y la respuesta.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    public void handleGetUsers(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        String method = exchange.getRequestMethod();

        if ("GET".equals(method)) {
            // Obtiene todos los usuarios de la base de datos.
            List<User> users = userDAOSQL.getAllUsers();
            sendResponse(exchange, 200, users.toString()); // Devuelve los usuarios como una lista en texto.
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    /**
     * Maneja solicitudes POST para agregar un nuevo usuario.
     *
     * @param exchange Objeto HttpExchange que contiene la solicitud y la respuesta.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    public void handleAddUser(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        String method = exchange.getRequestMethod();

        if ("POST".equals(method)) {
            // Lee el cuerpo de la solicitud y extrae los valores necesarios.
            String body = new String(exchange.getRequestBody().readAllBytes());
            String username = extractJsonValue(body, "username");
            String password = extractJsonValue(body, "password");

            if (username == null || password == null) {
                sendResponse(exchange, 400, "Invalid user data"); // Datos inválidos en la solicitud.
                return;
            }

            // Crea un nuevo usuario y lo guarda en la base de datos.
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);

            if (userDAOSQL.addUser(user)) {
                sendResponse(exchange, 201, "User added"); // Usuario agregado exitosamente.
            } else {
                sendResponse(exchange, 500, "Failed to add user"); // Error al agregar usuario.
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    /**
     * Maneja solicitudes PUT para actualizar un usuario existente.
     *
     * @param exchange Objeto HttpExchange que contiene la solicitud y la respuesta.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    public void handleUpdateUser(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        String method = exchange.getRequestMethod();

        if ("PUT".equals(method)) {
            // Obtiene el ID del usuario de la URI.
            String idStr = extractIdFromUri(exchange.getRequestURI().toString());
            if (idStr == null) {
                sendResponse(exchange, 400, "User ID is required"); // ID del usuario no proporcionado.
                return;
            }

            int id = Integer.parseInt(idStr); // Convierte el ID a un entero.
            String body = new String(exchange.getRequestBody().readAllBytes());
            String username = extractJsonValue(body, "username");
            String password = extractJsonValue(body, "password");

            if (username == null || password == null) {
                sendResponse(exchange, 400, "Invalid user data"); // Datos inválidos en la solicitud.
                return;
            }

            // Actualiza los datos del usuario en la base de datos.
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);

            if (userDAOSQL.updateUser(id, user)) {
                sendResponse(exchange, 200, "User updated"); // Usuario actualizado exitosamente.
            } else {
                sendResponse(exchange, 404, "User not found"); // Usuario no encontrado.
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    /**
     * Maneja solicitudes DELETE para eliminar un usuario.
     *
     * @param exchange Objeto HttpExchange que contiene la solicitud y la respuesta.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    public void handleDeleteUser(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        String method = exchange.getRequestMethod();

        if ("DELETE".equals(method)) {
            // Obtiene el ID del usuario de la URI.
            String idStr = extractIdFromUri(exchange.getRequestURI().toString());
            if (idStr == null) {
                sendResponse(exchange, 400, "User ID is required"); // ID del usuario no proporcionado.
                return;
            }

            int id = Integer.parseInt(idStr);
            if (userDAOSQL.deleteUser(id)) {
                sendResponse(exchange, 200, "User deleted"); // Usuario eliminado exitosamente.
            } else {
                sendResponse(exchange, 404, "User not found"); // Usuario no encontrado.
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    /**
     * Extrae el ID del usuario de la URI.
     *
     * @param uri URI de la solicitud.
     * @return El ID del usuario como String, o null si no se encuentra.
     */
    private String extractIdFromUri(String uri) {
        String[] parts = uri.split("/");
        return parts.length > 2 ? parts[2] : null;
    }

    /**
     * Envía una respuesta HTTP al cliente.
     *
     * @param exchange   Objeto HttpExchange que contiene la solicitud y la respuesta.
     * @param statusCode Código de estado HTTP.
     * @param response   Cuerpo de la respuesta como String.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    /**
     * Extrae un valor de una cadena JSON basada en la clave especificada.
     *
     * @param json JSON como String.
     * @param key  Clave del valor a extraer.
     * @return El valor asociado con la clave, o null si no se encuentra.
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) {
            return null;
        }
        startIndex += searchKey.length();
        char firstChar = json.charAt(startIndex);
        if (firstChar == '"') {
            int endIndex = json.indexOf('"', startIndex + 1);
            return json.substring(startIndex + 1, endIndex);
        } else {
            int endIndex = json.indexOf(',', startIndex);
            if (endIndex == -1) {
                endIndex = json.indexOf('}', startIndex);
            }
            return json.substring(startIndex, endIndex).trim();
        }
    }
}
