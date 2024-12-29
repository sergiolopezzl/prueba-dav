package app.apiRESTful.dao;

import app.apiRESTful.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductDAO {
    private final List<Product> products = new ArrayList<>();

    // Obtiene todos los productos
    public synchronized List<Product> getAllProducts() {
        return new ArrayList<>(products); // Devuelve una copia para evitar manipulaciones externas
    }

    // Obtiene un producto por su ID
    public synchronized Product getProductById(String id) {
        return products.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    // Agrega un nuevo producto
    public synchronized void addProduct(Product product) {
        if (product.getId() == null || product.getId().isEmpty()) {
            product.setId(UUID.randomUUID().toString()); // Generar un ID único si no se proporciona
        }
        if (products.stream().anyMatch(p -> p.getId().equals(product.getId()))) {
            throw new IllegalArgumentException("A product with the same ID already exists.");
        }
        products.add(product);
    }

    // Actualiza un producto existente
    public synchronized boolean updateProduct(String id, Product updatedProduct) {
        Optional<Product> existingProduct = products.stream().filter(p -> p.getId().equals(id)).findFirst();
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setName(updatedProduct.getName());
            product.setDescription(updatedProduct.getDescription());
            product.setPrice(updatedProduct.getPrice());
            product.setQuantity(updatedProduct.getQuantity());
            return true;
        } else {
            throw new IllegalArgumentException("Product with ID " + id + " not found.");
        }
    }

    // Elimina un producto por su ID
    public synchronized boolean deleteProduct(String id) {
        return products.removeIf(p -> p.getId().equals(id));
    }
}
