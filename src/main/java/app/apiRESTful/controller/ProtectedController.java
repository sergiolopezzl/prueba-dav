package app.apiRESTful.controller;

import app.apiRESTful.auth.AuthManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Controller que maneja solicitudes protegidas mediante autenticación basada en tokens.
 * Implementa el manejo de métodos HTTP y la validación de tokens de autorización.
 */
public class ProtectedController implements HttpHandler {

    // Instancia del manejador de autenticación
    private final AuthManager authManager = new AuthManager();

    /**
     * Añade encabezados CORS a la respuesta para permitir solicitudes desde cualquier origen
     * y especificar métodos y encabezados permitidos.
     *
     * @param exchange Objeto HttpExchange que representa la solicitud y la respuesta.
     */
    public void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // Permitir todos los orígenes
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); // Métodos permitidos
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization"); // Encabezados permitidos
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true"); // Permitir credenciales
    }

    /**
     * Maneja solicitudes HTTP dirigidas al endpoint protegido.
     * Solo permite solicitudes GET con un token válido en el encabezado Authorization.
     *
     * @param exchange Objeto HttpExchange que contiene la solicitud HTTP y donde se envía la respuesta.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Request received");

        // Verifica si el método HTTP es GET
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            addCorsHeaders(exchange); // Añade encabezados CORS
            System.out.println("Method is GET");

            // Obtiene el encabezado Authorization
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7); // Extrae el token eliminando el prefijo "Bearer "
                System.out.println("Token received: " + token);

                // Valida el token utilizando AuthManager
                if (authManager.validateToken(token)) {
                    sendResponse(exchange, 200, "Request successful. You are authenticated.");
                } else {
                    sendResponse(exchange, 401, "Unauthorized: Invalid token");
                }
            } else {
                sendResponse(exchange, 401, "Unauthorized: Missing or malformed Authorization header");
            }
        } else {
            // Maneja métodos HTTP no permitidos
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    /**
     * Envía una respuesta HTTP al cliente.
     *
     * @param exchange   Objeto HttpExchange que contiene la solicitud y la respuesta.
     * @param statusCode Código de estado HTTP (por ejemplo, 200 para éxito, 401 para no autorizado).
     * @param response   Cuerpo de la respuesta como String.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length); // Envía encabezados con el código de estado y longitud del cuerpo
        OutputStream os = exchange.getResponseBody(); // Obtiene el flujo de salida para enviar el cuerpo
        os.write(response.getBytes()); // Escribe el cuerpo de la respuesta
        os.close(); // Cierra el flujo de salida
    }
}
