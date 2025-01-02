package app.apiRESTful;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppTest extends TestCase {

    private static final String BASE_URL = "http://localhost:8000";
    private static boolean serverStarted = false; // Variable para verificar si el servidor ya ha sido iniciado

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Verifica si el servidor ya está iniciado
        if (!serverStarted) {
            // Inicia el servidor en un hilo separado solo si no se ha iniciado previamente
            new Thread(() -> {
                try {
                    App.main(new String[]{});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            // Esperar unos segundos para que el servidor inicie
            Thread.sleep(2000);
            serverStarted = true; // Marca el servidor como iniciado
        }
    }

    private HttpURLConnection createConnection(String endpoint, String method, String token) throws Exception {
        String url = BASE_URL + endpoint;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        if (token != null) {
            connection.setRequestProperty("Authorization", "Bearer " + token);
        }
        connection.setDoOutput(true);
        return connection;
    }

    public void testLoginEndpoint() throws Exception {
        HttpURLConnection connection = createConnection("/login", "POST", null);

        String jsonInput = "{\"username\":\"sergio\", \"password\":\"123\"}";
        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonInput.getBytes());
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        assertEquals("El código de respuesta debería ser 200 para un login exitoso", 200, responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        assertTrue("La respuesta debería contener un token", response.toString().contains("token"));
    }

    public void testProtectedEndpointWithoutToken() throws Exception {
        HttpURLConnection connection = createConnection("/protected", "GET", null);

        int responseCode = connection.getResponseCode();
        assertEquals("El código de respuesta debería ser 401 si no se envía un token", 401, responseCode);
    }

    public void testProductsEndpointWithInvalidToken() throws Exception {
        HttpURLConnection connection = createConnection("/products", "GET", "invalidToken");

        int responseCode = connection.getResponseCode();
        assertEquals("El código de respuesta debería ser 401 si se envía un token inválido", 401, responseCode);
    }

    public void testOptionsRequest() throws Exception {
        HttpURLConnection connection = createConnection("/products", "OPTIONS", null);

        int responseCode = connection.getResponseCode();
        assertEquals("El código de respuesta debería ser 200 para una solicitud OPTIONS", 200, responseCode);
    }
}
