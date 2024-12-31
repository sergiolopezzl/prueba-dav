package app.apiRESTful.controller;

import com.sun.net.httpserver.HttpExchange;
import app.apiRESTful.dao.UserDAOSQL;
import app.apiRESTful.model.User;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class UserController {
    private final UserDAOSQL userDAOSQL;

    public UserController(UserDAOSQL userDAOSQL) {
        this.userDAOSQL = userDAOSQL;
    }

    // Método para añadir encabezados CORS
    public void addCorsHeaders(HttpExchange exchange) {
        System.out.println("565656");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // Permitir todos los orígenes
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); // Métodos permitidos
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization"); // Encabezados permitidos
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
    }

    // Obtener todos los usuarios
    public void handleGetUsers(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        String method = exchange.getRequestMethod();

        if ("GET".equals(method)) {
            List<User> users = userDAOSQL.getAllUsers();
            sendResponse(exchange, 200, users.toString());
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    // Agregar un usuario
    public void handleAddUser(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        String method = exchange.getRequestMethod();

        if ("POST".equals(method)) {
            String body = new String(exchange.getRequestBody().readAllBytes());
            String username = extractJsonValue(body, "username");
            String password = extractJsonValue(body, "password");

            if (username == null || password == null) {
                sendResponse(exchange, 400, "Invalid user data");
                return;
            }

            User user = new User();
            user.setUsername(username);
            user.setPassword(password);

            if (userDAOSQL.addUser(user)) {
                sendResponse(exchange, 201, "User added");
            } else {
                sendResponse(exchange, 500, "Failed to add user");
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    // Actualizar un usuario
    public void handleUpdateUser(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        String method = exchange.getRequestMethod();

        if ("PUT".equals(method)) {
            String idStr = extractIdFromUri(exchange.getRequestURI().toString());
            if (idStr == null) {
                sendResponse(exchange, 400, "User ID is required");
                return;
            }

            int id = Integer.parseInt(idStr);  // Convertir el ID de la URI a un entero
            String body = new String(exchange.getRequestBody().readAllBytes());

            String username = extractJsonValue(body, "username");
            String password = extractJsonValue(body, "password");

            if (username == null || password == null) {
                sendResponse(exchange, 400, "Invalid user data");
                return;
            }

            // Crear el objeto User con los nuevos datos
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);

            // Llamar al método updateUser del DAO para actualizar el usuario en la base de datos
            if (userDAOSQL.updateUser(id, user)) {
                sendResponse(exchange, 200, "User updated");
            } else {
                sendResponse(exchange, 404, "User not found");
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }


    // Eliminar un usuario
    public void handleDeleteUser(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        String method = exchange.getRequestMethod();

        if ("DELETE".equals(method)) {
            String idStr = extractIdFromUri(exchange.getRequestURI().toString());
            if (idStr == null) {
                sendResponse(exchange, 400, "User ID is required");
                return;
            }

            int id = Integer.parseInt(idStr);
            if (userDAOSQL.deleteUser(id)) {
                sendResponse(exchange, 200, "User deleted");
            } else {
                sendResponse(exchange, 404, "User not found");
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    // Métodos auxiliares
    private String extractIdFromUri(String uri) {
        String[] parts = uri.split("/");
        return parts.length > 2 ? parts[2] : null;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

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
