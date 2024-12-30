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
        ProductDAOSQL ProductDAOSQL = new ProductDAOSQL();
        AuthManager authManager = new AuthManager();
        ProductController productController = new ProductController(ProductDAOSQL, authManager);

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        try {
            // Cargar el driver JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Registrar el handler para el login
        server.createContext("/login", new AuthController());

        // Registrar el handler para el endpoint protegido
        server.createContext("/protected", new ProtectedController());

        server.createContext("/products", exchange -> {
            String method = exchange.getRequestMethod();
                   
            // Manejar preflight (OPTIONS)
            if ("OPTIONS".equals(method)) {
                productController.addCorsHeaders(exchange);
                exchange.sendResponseHeaders(200, -1); // No hay cuerpo de respuesta
                return;
            }

            // Manejar otros métodos HTTP (GET, POST, PUT, DELETE)
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (!authManager.isAuthenticated(authHeader)) {
                sendResponse(exchange, 401, "Unauthorized");
                return;
            }
            switch (method) {
                case "GET":
                    productController.handleGetProducts(exchange);
                    break;
                case "POST":
                    productController.handleAddProduct(exchange);
                    break;
                case "PUT":
                    productController.handleUpdateProduct(exchange);
                    break;
                case "DELETE":
                    productController.handleDeleteProduct(exchange);
                    break;
                default:
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
