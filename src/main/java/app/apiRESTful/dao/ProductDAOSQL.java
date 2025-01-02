package app.apiRESTful.dao;

import app.apiRESTful.model.Product;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase `ProductDAOSQL` para manejar la interacción con la base de datos relacionada con productos.
 * Utiliza JDBC para conectar y realizar operaciones CRUD en la base de datos.
 */
public class ProductDAOSQL {

    // Carga las variables de entorno necesarias para la conexión a la base de datos.
    private static final Dotenv dotenv = Dotenv.load();
    private static final String DB_URL = dotenv.get("DB_URL"); // URL de la base de datos.
    private static final String DB_USER = dotenv.get("DB_USER"); // Usuario de la base de datos.
    private static final String DB_PASSWORD = dotenv.get("DB_PASSWORD"); // Contraseña de la base de datos.

    /**
     * Obtiene todos los productos de la base de datos.
     *
     * @return Una lista de objetos `Product` representando todos los productos en la base de datos.
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            // Itera sobre el resultado y crea objetos Product.
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

    /**
     * Obtiene un producto por su ID.
     *
     * @param id El ID del producto a buscar.
     * @return Un objeto `Product` si se encuentra, o `null` si no existe.
     */
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

    /**
     * Agrega un nuevo producto a la base de datos.
     *
     * @param product El objeto `Product` que contiene los datos del producto a agregar.
     */
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

    /**
     * Actualiza un producto existente en la base de datos.
     *
     * @param id             El ID del producto a actualizar.
     * @param updatedProduct El objeto `Product` con los nuevos datos del producto.
     */
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

    /**
     * Elimina un producto por su ID.
     *
     * @param id El ID del producto a eliminar.
     * @return `true` si se eliminó el producto con éxito, o `false` si no se encontró.
     */
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
