package app.apiRESTful.auth;

public class AuthManager {
    // Token válido para la autenticación (en un escenario real, usarías un sistema de validación más complejo)
    private final String validToken = "PruebaToken*";

    // Método que verifica si el token proporcionado es válido
    public boolean isAuthenticated(String token) {
        // Compara el token recibido con el token válido
        return validToken.equals(token);
    }
}
