package app.apiRESTful.controller;

import com.sun.net.httpserver.HttpExchange;
import app.apiRESTful.dao.ProductDAO;
import app.apiRESTful.model.Product;
import app.apiRESTful.auth.AuthManager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ProductController {

    private final ProductDAO productDAO;
    private final AuthManager authManager;

    public ProductController(ProductDAO productDAO, AuthManager authManager) {
        this.productDAO = productDAO;
        this.authManager = authManager;
    }

    // Método para añadir encabezados CORS a la respuesta
    public void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // Permitir todos los orígenes
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); // Métodos permitidos
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization"); // Encabezados permitidos
    }

    // Maneja la solicitud GET para obtener todos los productos
    public void handleGetProducts(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange); // Agregar encabezados CORS
        String method = exchange.getRequestMethod();
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        // Validar autorización
        if (!authManager.isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }

        if ("GET".equals(method)) {
            List<Product> products = productDAO.getAllProducts();
            sendResponse(exchange, 200, products.toString());
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    // Maneja la solicitud POST para agregar un producto
    public void handleAddProduct(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange); // Agregar encabezados CORS
        String method = exchange.getRequestMethod();
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        // Validar autorización
        if (!authManager.isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }

        if ("POST".equals(method)) {
            System.out.println("iu");
            String body = new String(exchange.getRequestBody().readAllBytes());
            System.out.println("iu2");
            try {
                // Parsear los datos del producto desde el cuerpo de la solicitud
                String name = extractJsonValue(body, "name");
                String description = extractJsonValue(body, "description");
                Double price = Double.parseDouble(extractJsonValue(body, "price"));
                int quantity = Integer.parseInt(extractJsonValue(body, "quantity"));

                // Validar los datos del producto
                if (name == null || description == null || price <= 0 || quantity < 0) {
                    sendResponse(exchange, 400, "Invalid product data");
                    return;
                }
        
                Product product = new Product(name, description, price, quantity);
                productDAO.addProduct(product);
                sendResponse(exchange, 201, "Product added: " + product.toString());
            } catch (Exception e) {
                sendResponse(exchange, 400, "Invalid request body"+e);
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    // Maneja la solicitud PUT para actualizar un producto
    public void handleUpdateProduct(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange); // Agregar encabezados CORS
        String method = exchange.getRequestMethod();
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        // Validar autorización
        if (!authManager.isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }

        if ("PUT".equals(method)) {
            String body = new String(exchange.getRequestBody().readAllBytes());
            try {
                // Extraer el ID del producto desde la URL
                String[] pathParts = exchange.getRequestURI().getPath().split("/");
                if (pathParts.length < 3) {
                    throw new IllegalArgumentException("Product ID is required");
                }
                String productId = pathParts[2];
        
                // Obtener el producto existente
                Product existingProduct = productDAO.getProductById(productId);
                if (existingProduct == null) {
                    sendResponse(exchange, 404, "Product not found");
                    return;
                }
        
                // Actualizar solo los campos presentes en el JSON
                String name = extractJsonValue(body, "name");
                String description = extractJsonValue(body, "description");
                String priceStr = extractJsonValue(body, "price");
                String quantityStr = extractJsonValue(body, "quantity");
                System.out.println(priceStr.getClass().getSimpleName());
                System.out.println(priceStr);
                if (name != null && !"".equals(name)) {
                    existingProduct.setName(name);
                }
                if (description != null && !"".equals(description)) {
                    existingProduct.setDescription(description);
                }
                if (priceStr != null && !"null".equals(priceStr)) {
                    System.out.println(priceStr.getClass().getSimpleName());
                    System.out.println(priceStr);
                    try {
                        double price = Double.parseDouble(priceStr);
                        existingProduct.setPrice(price);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid price value"+e);
                    }
                }
                if (quantityStr != null && !"null".equals(quantityStr)) {
                    try {
                        int quantity = Integer.parseInt(quantityStr);
                        existingProduct.setQuantity(quantity);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid quantity value");
                    }
                }
        
                // Guardar los cambios (en este caso, los cambios ya están aplicados al objeto existente)
                productDAO.updateProduct(productId, existingProduct);
        
                sendResponse(exchange, 200, "Product updated: " + existingProduct.toString());
            } catch (Exception e) {
                sendResponse(exchange, 400, "Invalid request body: " + e.getMessage());
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
        
    }

    // Maneja la solicitud DELETE para eliminar un producto
    public void handleDeleteProduct(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange); // Agregar encabezados CORS
        String method = exchange.getRequestMethod();
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        // Validar autorización
        if (!authManager.isAuthenticated(authHeader)) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }

        if ("DELETE".equals(method)) {
            String productId = extractIdFromUri(exchange.getRequestURI().toString());
            if (productId == null) {
                sendResponse(exchange, 400, "Product ID is required in the URL");
                return;
            }

            boolean success = productDAO.deleteProduct(productId);
            if (success) {
                sendResponse(exchange, 200, "Product deleted.");
            } else {
                sendResponse(exchange, 404, "Product not found.");
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    // Método para extraer el ID del producto desde la URI
    private String extractIdFromUri(String uri) {
        String[] parts = uri.split("/");
        return parts.length > 2 ? parts[2] : null;
    }

    // Enviar respuesta HTTP al cliente
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    //Método para Extraer Valores de JSON
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) {
            return null; // Si no se encuentra la clave, retornar null
        }
        startIndex += searchKey.length();
        
        // Determinar si el valor es un string (comillas) o un número
        char firstChar = json.charAt(startIndex);
        if (firstChar == '"') { // Valor entre comillas
            int endIndex = json.indexOf('"', startIndex + 1);
            return json.substring(startIndex + 1, endIndex); // Extraer sin las comillas
        } else { // Valor numérico
            int endIndex = json.indexOf(',', startIndex);
            if (endIndex == -1) { // Último valor
                endIndex = json.indexOf('}', startIndex);
            }
            return json.substring(startIndex, endIndex).trim(); // Retornar valor como string
        }
    }

}
