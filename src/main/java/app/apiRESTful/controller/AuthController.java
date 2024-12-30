package app.apiRESTful.controller;

import app.database.DatabaseHelper;
import app.apiRESTful.auth.AuthManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Scanner;

public class AuthController implements HttpHandler {

    private final AuthManager authManager = new AuthManager();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            Scanner scanner = new Scanner(exchange.getRequestBody(), StandardCharsets.UTF_8.name());
            String requestBody = scanner.useDelimiter("\\A").next();
            scanner.close();

            // Extraer las credenciales (en este caso, JSON con username y password)
            String[] credentials = requestBody.split("&");
            String username = credentials[0].split("=")[1];
            String password = credentials[1].split("=")[1];

            try {
                // Verificar si el usuario existe en la base de datos
                if (DatabaseHelper.isValidUser(username, password)) {
                    // Generar el token si las credenciales son válidas
                    String token = authManager.generateToken(username);
                    sendResponse(exchange, 200, "Token: " + token);
                } else {
                    sendResponse(exchange, 401, "Unauthorized");
                }
            } catch (SQLException e) {
                sendResponse(exchange, 500, "Internal Server Error");
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    // Método para enviar la respuesta HTTP
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
