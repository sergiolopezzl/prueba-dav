package app.apiRESTful.dao;

import app.apiRESTful.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOSQL {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mydatabase";
    private static final String DB_USER = "myuser";
    private static final String DB_PASSWORD = "mypassword";

    // Obtiene todos los productos
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Product product = new Product(
                        resultSet.getString("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getDouble("price"),
                        resultSet.getInt("quantity")
                );
                products.add(product);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    // Obtiene un producto por su ID
    public Product getProductById(String id) {
        String query = "SELECT * FROM products WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Product(
                            resultSet.getString("id"),
                            resultSet.getString("name"),
                            resultSet.getString("description"),
                            resultSet.getDouble("price"),
                            resultSet.getInt("quantity")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Agrega un nuevo producto
    public void addProduct(Product product) {
        String query = "INSERT INTO products (id, name, description, price, quantity) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, product.getId());
            preparedStatement.setString(2, product.getName());
            preparedStatement.setString(3, product.getDescription());
            preparedStatement.setDouble(4, product.getPrice());
            preparedStatement.setInt(5, product.getQuantity());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Actualiza un producto existente
    public void updateProduct(String id, Product updatedProduct) {
        String query = "UPDATE products SET name = ?, description = ?, price = ?, quantity = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, updatedProduct.getName());
            preparedStatement.setString(2, updatedProduct.getDescription());
            preparedStatement.setDouble(3, updatedProduct.getPrice());
            preparedStatement.setInt(4, updatedProduct.getQuantity());
            preparedStatement.setString(5, id);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Elimina un producto por su ID
    public boolean deleteProduct(String id) {
        String query = "DELETE FROM products WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, id);

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
