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

/**
 * Clase AuthController
 * Controlador encargado de manejar las solicitudes de autenticación de usuarios.
 * Utiliza el framework HttpServer para gestionar las solicitudes HTTP.
 * 
 * Funcionalidades principales:
 * - Verificar las credenciales del usuario contra una base de datos.
 * - Generar un token JWT para usuarios autenticados.
 * - Responder con mensajes JSON y encabezados CORS.
 */
public class AuthController implements HttpHandler {

    private final AuthManager authManager = new AuthManager();

    /**
     * Agrega encabezados CORS a la respuesta.
     * Esto permite solicitudes desde diferentes dominios.
     * 
     * @param exchange El objeto HttpExchange asociado a la solicitud HTTP.
     */
    public void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // Permitir todos los orígenes
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); // Métodos permitidos
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization"); // Encabezados permitidos
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
    }

    /**
     * Maneja las solicitudes HTTP dirigidas a este controlador.
     * 
     * - Si el método es POST:
     *   1. Lee el cuerpo de la solicitud y lo interpreta como JSON.
     *   2. Valida las credenciales del usuario contra la base de datos.
     *   3. Si las credenciales son válidas, genera un token JWT y lo devuelve en la respuesta.
     * 
     * - Si el método no es POST, devuelve un error 405.
     * 
     * @param exchange El objeto HttpExchange asociado a la solicitud HTTP.
     * @throws IOException Si ocurre un error al leer o escribir la solicitud/respuesta.
     */
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
                    String token = authManager.generateToken(username, 15);
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

    /**
     * Envía una respuesta HTTP con un código de estado y un cuerpo de texto.
     * 
     * @param exchange El objeto HttpExchange asociado a la solicitud HTTP.
     * @param statusCode Código de estado HTTP (e.g., 200, 401, 500).
     * @param response El cuerpo de la respuesta como texto.
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
