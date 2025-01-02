package app.apiRESTful;

import app.apiRESTful.auth.AuthManager;
import junit.framework.TestCase;

public class AuthManagerTest extends TestCase {

    private AuthManager authManager;
    private String validToken;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        authManager = new AuthManager();
        validToken = authManager.generateToken("testUser",2);
    }

    public void testGenerateToken() {
        String token = authManager.generateToken("testUser",2);
        assertNotNull("El token no debería ser nulo", token);
        assertFalse("El token no debería estar vacío", token.isEmpty());
    }

    public void testValidateToken_ValidToken() {
        assertTrue("El token válido debería ser reconocido como válido", authManager.validateToken(validToken));
    }

    public void testValidateToken_ExpiredToken() throws InterruptedException {
        // Generar un token con un tiempo de expiración muy corto (1 min)
        String shortLivedToken = authManager.generateTokenUnSegundo("shortLivedUser");
        Thread.sleep(2000); // Esperar a que el token expire
        assertFalse("El token expirado no debería ser válido", authManager.validateToken(shortLivedToken));
    }

    public void testValidateToken_InvalidSignature() {
        // Modificar el token para que tenga una firma inválida
        String tamperedToken = validToken.substring(0, validToken.length() - 1) + "X";
        assertFalse("Un token con firma inválida no debería ser válido", authManager.validateToken(tamperedToken));
    }

    public void testGetUsernameFromToken_ValidToken() {
        String username = authManager.getUsernameFromToken(validToken);
        assertNotNull("El nombre de usuario no debería ser nulo", username);
        assertEquals("El nombre de usuario debería coincidir", "testUser", username);
    }

    public void testGetUsernameFromToken_InvalidToken() {
        String tamperedToken = validToken.substring(0, validToken.length() - 1) + "X";
        String username = authManager.getUsernameFromToken(tamperedToken);
        assertNull("El nombre de usuario debería ser nulo para un token inválido", username);
    }

    public void testGetUsernameFromToken_ExpiredToken() throws InterruptedException {
        String shortLivedToken = authManager.generateTokenUnSegundo("shortLivedUser");
        Thread.sleep(2000); // Esperar a que el token expire
        String username = authManager.getUsernameFromToken(shortLivedToken);
        assertNull("El nombre de usuario debería ser nulo para un token expirado", username);
    }
}
