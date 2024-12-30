package app.apiRESTful.controller;

import app.apiRESTful.auth.AuthManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class ProtectedController implements HttpHandler {

    private final AuthManager authManager = new AuthManager();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        // Validar el token recibido
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (authManager.validateToken(token)) {
                // Si el token es válido, procesamos la solicitud
                sendResponse(exchange, 200, "Request successful. You are authenticated.");
            } else {
                sendResponse(exchange, 401, "Unauthorized");
            }
        } else {
            sendResponse(exchange, 401, "Unauthorized");
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
