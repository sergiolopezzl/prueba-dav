package app.apiRESTful;
import app.apiRESTful.controller.ProductController;
import app.apiRESTful.controller.ProtectedController;
import app.apiRESTful.controller.UserController;
import app.apiRESTful.dao.ProductDAOSQL;
import app.apiRESTful.dao.UserDAOSQL;
import app.apiRESTful.model.Product;
import app.apiRESTful.model.User;
import com.sun.net.httpserver.HttpExchange;
import junit.framework.TestCase;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ControllerTests extends TestCase {

    public void testProductControllerGetProducts() throws IOException {
        // Arrange
        ProductDAOSQL mockDAO = mock(ProductDAOSQL.class);
        ProductController controller = new ProductController(mockDAO);
        HttpExchange exchange = mock(HttpExchange.class);

        List<Product> mockProducts = Arrays.asList(
            new Product("Product1", "Description1", 10.0, 5),
            new Product("Product2", "Description2", 20.0, 3)
        );

        when(mockDAO.getAllProducts()).thenReturn(mockProducts);
        when(exchange.getRequestMethod()).thenReturn("GET");
        OutputStream os = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(os);

        // Act
        controller.handleGetProducts(exchange);

        // Assert
        verify(exchange).sendResponseHeaders(eq(200), anyInt());
        assertTrue(os.toString().contains("Product1"));
    }

    public void testProductControllerAddProduct() throws IOException {
        // Arrange
        ProductDAOSQL mockDAO = mock(ProductDAOSQL.class);
        ProductController controller = new ProductController(mockDAO);
        HttpExchange exchange = mock(HttpExchange.class);

        String mockBody = "{\"name\":\"Product1\",\"description\":\"Description1\",\"price\":10.0,\"quantity\":5}";
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(mockBody.getBytes()));
        OutputStream os = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(os);

        // Act
        controller.handleAddProduct(exchange);

        // Assert
        verify(mockDAO).addProduct(any(Product.class));
        verify(exchange).sendResponseHeaders(eq(201), anyInt());
        assertTrue(os.toString().contains("Product added"));
    }

    public void testProtectedControllerHandleRequest() throws IOException {
        // Arrange
        ProtectedController controller = new ProtectedController();
        HttpExchange exchange = mock(HttpExchange.class);

        when(exchange.getRequestMethod()).thenReturn("GET");
        OutputStream os = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(os);

        // Act
        controller.handle(exchange);

        // Assert
        verify(exchange).sendResponseHeaders(eq(200), anyInt());
        assertTrue(os.toString().contains("Protected resource"));
    }

    public void testUserControllerGetUsers() throws IOException {
        // Arrange
        UserDAOSQL mockDAO = mock(UserDAOSQL.class);
        UserController controller = new UserController(mockDAO);
        HttpExchange exchange = mock(HttpExchange.class);

        // Mock de la lista de usuarios
        List<User> mockUsers = Arrays.asList(
            new User(1, "user1", "password1"),
            new User(2, "user2", "password2")
        );

        // Configurar mocks
        when(mockDAO.getAllUsers()).thenReturn(mockUsers);
        when(exchange.getRequestMethod()).thenReturn("GET");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(os);

        // Act
        controller.handleGetUsers(exchange);

        // Assert
        verify(exchange).sendResponseHeaders(eq(200), anyInt());
        String response = os.toString();
        assertTrue(response.contains("user1"));
        assertTrue(response.contains("user2"));
    }

    public void testUserControllerAddUser() throws IOException {
        // Arrange
        UserDAOSQL mockDAO = mock(UserDAOSQL.class);
        UserController controller = new UserController(mockDAO);
        HttpExchange exchange = mock(HttpExchange.class);

        String mockBody = "{\"username\":\"user1\",\"password\":\"password1\"}";
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(new java.io.ByteArrayInputStream(mockBody.getBytes()));
        OutputStream os = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(os);

        // Act
        controller.handleAddUser(exchange);

        // Assert
        verify(mockDAO).addUser(any(User.class));
        verify(exchange).sendResponseHeaders(eq(201), anyInt());
        assertTrue(os.toString().contains("User added"));
    }
}
