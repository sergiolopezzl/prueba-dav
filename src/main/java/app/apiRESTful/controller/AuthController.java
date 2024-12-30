package app.apiRESTful.controller;

import app.apiRESTful.model.User;
import app.database.DatabaseHelper;
import app.apiRESTful.auth.AuthManager;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Scanner;

public class AuthController implements HttpHandler {

    private final AuthManager authManager = new AuthManager();

    public void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // Permitir todos los orígenes
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); // Métodos permitidos
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization"); // Encabezados permitidos
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
    }
        
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if ("POST".equals(exchange.getRequestMethod())) {
            try {
                // Leer el cuerpo de la solicitud
                InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder bodyBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    bodyBuilder.append(line);
                }
                String requestBody = bodyBuilder.toString();

                // Usar Gson para analizar el JSON
                Gson gson = new Gson();
                User credentials = gson.fromJson(requestBody, User.class);

                String username = credentials.getUsername();
                String password = credentials.getPassword();
                
                // Verificar si el usuario existe en la base de datos
                if (DatabaseHelper.isValidUser(username, password)) {
                    
                    // Generar el token si las credenciales son válidas
                    String token = authManager.generateToken(username);
                    System.out.println(token);
                    // Enviar la respuesta con el token en formato JSON válido
                    sendResponse(exchange, 200, "{\"token\": \"" + token + "\"}");
                } else {
                    sendResponse(exchange, 401, "Unauthorized");
                }
            } catch (JsonSyntaxException e) {
                sendResponse(exchange, 400, "Bad Request: Invalid JSON format");
            } catch (SQLException e) {
                sendResponse(exchange, 500, "Internal Server Error");
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }


    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
