package app.apiRESTful;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import app.apiRESTful.controller.AuthController;
import app.apiRESTful.controller.ProductController;
import app.apiRESTful.controller.ProtectedController;
import app.apiRESTful.dao.ProductDAOSQL;
import app.apiRESTful.auth.AuthManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class App {

    public static void main(String[] args) throws IOException {
        ProductDAOSQL productDAOSQL = new ProductDAOSQL();
        ProductController productController = new ProductController(productDAOSQL);

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        try {
            // Cargar el driver JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
            // Registrar el handler para el login
            server.createContext("/login", exchange -> {
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    productController.addCorsHeaders(exchange);
                    exchange.sendResponseHeaders(200, -1); // No hay cuerpo de respuesta
                    
                    return;
                }
                
                productController.addCorsHeaders(exchange);
                
                new AuthController().handle(exchange);
            });

            // Registrar el handler para el endpoint protegido
            server.createContext("/protected", exchange -> {
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    productController.addCorsHeaders(exchange);

                    exchange.sendResponseHeaders(200, -1); // No hay cuerpo de respuesta
                    System.out.println("Request receggggd");
                    
                }
                System.out.println("Request receiveg");
                productController.addCorsHeaders(exchange);
                System.out.println("Request receivewewewewed");
                new ProtectedController().handle(exchange);
            });

        // Registrar el handler para productos
        server.createContext("/products", exchange -> {
            String method = exchange.getRequestMethod();
        
            // Manejar preflight (OPTIONS)
            if ("OPTIONS".equalsIgnoreCase(method)) {
                productController.addCorsHeaders(exchange);
                exchange.sendResponseHeaders(200, -1); // No hay cuerpo de respuesta
                return;
            }
        
            // Validar el token
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                productController.addCorsHeaders(exchange);
                sendResponse(exchange, 401, "Unauthorized: Missing or malformed Authorization header");
                return;
            }
        
            String token = authHeader.substring(7);
            if (!new AuthManager().validateToken(token)) {
                productController.addCorsHeaders(exchange);
                sendResponse(exchange, 401, "Unauthorized: Invalid token");
                return;
            }
        
            // Procesar la solicitud si el token es válido
            if ("GET".equalsIgnoreCase(method)) {
                productController.handleGetProducts(exchange);
            } else if ("POST".equalsIgnoreCase(method)) {
                productController.handleAddProduct(exchange);
            } else if ("PUT".equalsIgnoreCase(method)) {
                productController.handleUpdateProduct(exchange);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                productController.handleDeleteProduct(exchange);
            } else {
                productController.addCorsHeaders(exchange);
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        });
    
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8000");
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
