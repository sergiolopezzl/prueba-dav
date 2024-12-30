package app.apiRESTful.controller;

import app.apiRESTful.auth.AuthManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class ProtectedController implements HttpHandler {

    private final AuthManager authManager = new AuthManager();

    public void addCorsHeaders(HttpExchange exchange) {
        System.out.println("to21k");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // Permitir todos los orígenes
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); // Métodos permitidos
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization"); // Encabezados permitidos
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Request received");
    
        // Check for HTTP GET method
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            addCorsHeaders(exchange);
            System.out.println("Method is GET");
    
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
    
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                System.out.println("Token received: " + token);
    
                // Validate token
                if (authManager.validateToken(token)) {
                    sendResponse(exchange, 200, "Request successful. You are authenticated.");
                } else {
                    sendResponse(exchange, 401, "Unauthorized: Invalid token");
                }
            } else {
                sendResponse(exchange, 401, "Unauthorized: Missing or malformed Authorization header");
            }
        } else {
            // Handle other HTTP methods if necessary (e.g., POST, PUT)
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }
    

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        System.out.println("tok");
        os.write(response.getBytes());
        os.close();
    }
}
