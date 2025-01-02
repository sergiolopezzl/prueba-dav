package app.apiRESTful.controller;

import com.sun.net.httpserver.HttpExchange;
import app.apiRESTful.dao.ProductDAOSQL;
import app.apiRESTful.model.Product;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * ProductController
 * 
 * Clase encargada de manejar las solicitudes relacionadas con los productos en el sistema.
 * Implementa métodos para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar) en productos.
 */
public class ProductController {

    private final ProductDAOSQL ProductDAOSQL;

    /**
     * Constructor del controlador.
     * 
     * @param ProductDAOSQL Instancia del DAO para interactuar con la base de datos.
     */
    public ProductController(ProductDAOSQL ProductDAOSQL) {
        this.ProductDAOSQL = ProductDAOSQL;
    }

    /**
     * Añade encabezados CORS a la respuesta para permitir solicitudes de diferentes orígenes.
     * 
     * @param exchange Objeto HttpExchange que representa la solicitud y la respuesta.
     */
    public void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
    }

    /**
     * Maneja la solicitud GET para obtener todos los productos.
     * 
     * @param exchange Objeto HttpExchange que representa la solicitud y la respuesta.
     * @throws IOException Si ocurre un error al manejar la solicitud.
     */
    public void handleGetProducts(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        String method = exchange.getRequestMethod();

        if ("GET".equals(method)) {
            List<Product> products = ProductDAOSQL.getAllProducts();
            sendResponse(exchange, 200, products.toString());
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    /**
     * Maneja la solicitud POST para agregar un producto.
     * 
     * @param exchange Objeto HttpExchange que representa la solicitud y la respuesta.
     * @throws IOException Si ocurre un error al manejar la solicitud.
     */
    public void handleAddProduct(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        String method = exchange.getRequestMethod();

        if ("POST".equals(method)) {
            String body = new String(exchange.getRequestBody().readAllBytes());

            try {
                // Parsear y validar los datos del producto
                String name = extractJsonValue(body, "name");
                String description = extractJsonValue(body, "description");
                Double price = Double.parseDouble(extractJsonValue(body, "price"));
                int quantity = Integer.parseInt(extractJsonValue(body, "quantity"));

                if (name == null || description == null || price <= 0 || quantity < 0) {
                    sendResponse(exchange, 400, "Invalid product data");
                    return;
                }

                Product product = new Product(name, description, price, quantity);
                ProductDAOSQL.addProduct(product);
                sendResponse(exchange, 201, "Product added: " + product.toString());
            } catch (Exception e) {
                sendResponse(exchange, 400, "Invalid request body: " + e.getMessage());
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    /**
     * Maneja la solicitud PUT para actualizar un producto existente.
     * 
     * @param exchange Objeto HttpExchange que representa la solicitud y la respuesta.
     * @throws IOException Si ocurre un error al manejar la solicitud.
     */
    public void handleUpdateProduct(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        String method = exchange.getRequestMethod();

        if ("PUT".equals(method)) {
            String body = new String(exchange.getRequestBody().readAllBytes());
            try {
                String[] pathParts = exchange.getRequestURI().getPath().split("/");
                if (pathParts.length < 3) {
                    throw new IllegalArgumentException("Product ID is required");
                }
                String productId = pathParts[2];

                Product existingProduct = ProductDAOSQL.getProductById(productId);
                if (existingProduct == null) {
                    sendResponse(exchange, 404, "Product not found");
                    return;
                }

                // Actualizar campos presentes en el JSON
                String name = extractJsonValue(body, "name");
                String description = extractJsonValue(body, "description");
                String priceStr = extractJsonValue(body, "price");
                String quantityStr = extractJsonValue(body, "quantity");

                if (name != null && !"".equals(name)) existingProduct.setName(name);
                if (description != null && !"".equals(description)) existingProduct.setDescription(description);
                if (priceStr != null && !"null".equals(priceStr)) {
                    existingProduct.setPrice(Double.parseDouble(priceStr));
                }
                if (quantityStr != null && !"null".equals(quantityStr)) {
                    existingProduct.setQuantity(Integer.parseInt(quantityStr));
                }

                ProductDAOSQL.updateProduct(productId, existingProduct);
                sendResponse(exchange, 200, "Product updated: " + existingProduct.toString());
            } catch (Exception e) {
                sendResponse(exchange, 400, "Invalid request body: " + e.getMessage());
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    /**
     * Maneja la solicitud DELETE para eliminar un producto.
     * 
     * @param exchange Objeto HttpExchange que representa la solicitud y la respuesta.
     * @throws IOException Si ocurre un error al manejar la solicitud.
     */
    public void handleDeleteProduct(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        String method = exchange.getRequestMethod();

        if ("DELETE".equals(method)) {
            String productId = extractIdFromUri(exchange.getRequestURI().toString());
            if (productId == null) {
                sendResponse(exchange, 400, "Product ID is required in the URL");
                return;
            }

            boolean success = ProductDAOSQL.deleteProduct(productId);
            if (success) {
                sendResponse(exchange, 200, "Product deleted.");
            } else {
                sendResponse(exchange, 404, "Product not found.");
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    /**
     * Extrae el ID del producto desde la URI.
     * 
     * @param uri URI de la solicitud.
     * @return ID del producto si existe, null en caso contrario.
     */
    private String extractIdFromUri(String uri) {
        String[] parts = uri.split("/");
        return parts.length > 2 ? parts[2] : null;
    }

    /**
     * Envía una respuesta HTTP al cliente.
     * 
     * @param exchange   Objeto HttpExchange.
     * @param statusCode Código de estado HTTP.
     * @param response   Cuerpo de la respuesta.
     * @throws IOException Si ocurre un error al enviar la respuesta.
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    /**
     * Extrae el valor de una clave JSON en formato String.
     * 
     * @param json Cuerpo del JSON.
     * @param key  Clave a buscar.
     * @return Valor de la clave como String o null si no se encuentra.
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return null;

        startIndex += searchKey.length();
        char firstChar = json.charAt(startIndex);
        if (firstChar == '"') {
            int endIndex = json.indexOf('"', startIndex + 1);
            return json.substring(startIndex + 1, endIndex);
        } else {
            int endIndex = json.indexOf(',', startIndex);
            if (endIndex == -1) endIndex = json.indexOf('}', startIndex);
            return json.substring(startIndex, endIndex).trim();
        }
    }
}
