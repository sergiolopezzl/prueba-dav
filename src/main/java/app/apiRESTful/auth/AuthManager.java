package app.apiRESTful.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class AuthManager {

    // Clave secreta para HMAC (debe ser suficientemente larga para HS256)
    private static final String SECRET_KEY_STRING = "@H$256!Cl4v3^S3gur4#C0n_L3tr4s&Númer0s%2024*+_>8XyZ!";

    // Convertir la clave secreta a un formato adecuado para HMAC (HS256)
    // Esto convierte la cadena SECRET_KEY_STRING en un objeto SecretKey que se usa para firmar el JWT
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());

    /**
     * Genera un token JWT para el usuario dado con una expiración determinada.
     * El tiempo se especifica en minutos.
     * @param username El nombre del usuario que será el sujeto del token.
     * @param tiempo El tiempo en minutos hasta que el token expire.
     * @return El token JWT generado.
     */
    public String generateToken(String username, int tiempo) {
        // Usando el Builder de JWT para crear un nuevo token
        return Jwts.builder()
                .setSubject(username) // Establece el nombre de usuario como sujeto del token
                .setIssuedAt(new Date()) // Fecha de emisión del token
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * tiempo)) // Tiempo de expiración del token (en milisegundos)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) // Firma el token con la clave secreta utilizando el algoritmo HS256
                .compact(); // Genera el token compactado
    }

    /**
     * Genera un token JWT para el usuario dado, con una expiración de 1 segundo.
     * @param username El nombre del usuario.
     * @return El token JWT generado con expiración de 1 segundo.
     */
    public String generateTokenUnSegundo(String username) {
        // Este método genera un token con una expiración muy corta (1 segundo)
        return Jwts.builder()
                .setSubject(username) // Establece el nombre de usuario como sujeto del token
                .setIssuedAt(new Date()) // Fecha de emisión del token
                .setExpiration(new Date(System.currentTimeMillis() + 1000)) // Expiración a los 1 segundo (1000 milisegundos)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) // Firma el token con la clave secreta utilizando el algoritmo HS256
                .compact(); // Genera el token compactado
    }

    /**
     * Valida un token JWT.
     * Este método intenta analizar el token y verificar su firma.
     * Si el token ha expirado o tiene algún problema, se captura una excepción correspondiente.
     * @param token El token JWT a validar.
     * @return Verdadero si el token es válido, falso si es inválido o ha ocurrido un error.
     */
    public boolean validateToken(String token) {
        try {
            // Analiza el token con la clave secreta y valida su firma
            Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY) // Se asegura de usar la misma clave secreta para verificar la firma
                .build()
                .parseClaimsJws(token); // Si el token es válido, se parsea y se verifica su firma
            return true;
        } catch (ExpiredJwtException e) {
            // Captura excepciones específicas de JWT, y muestra mensajes de error apropiados
            System.err.println("El token ha expirado: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("El token no es soportado: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("El token está mal formado: " + e.getMessage());
        } catch (SignatureException e) {
            System.err.println("La firma del token no es válida: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("El token está vacío o nulo: " + e.getMessage());
        }
        return false; // Si alguna excepción es lanzada, el token es considerado inválido
    }

    /**
     * Extrae el nombre de usuario del token JWT.
     * Este método devuelve el nombre de usuario almacenado en el campo "subject" del token JWT.
     * @param token El token JWT del cual se extraerá el nombre de usuario.
     * @return El nombre de usuario extraído del token, o null si ocurre un error.
     */
    public String getUsernameFromToken(String token) {
        try {
            // Parseamos el token para obtener sus reclamos (claims) y extraemos el sujeto (username)
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY) // Usamos la clave secreta para validar la firma
                    .build()
                    .parseClaimsJws(token) // Analiza el JWT y extrae sus claims
                    .getBody()
                    .getSubject(); // Extrae el "subject" que contiene el nombre de usuario
        } catch (Exception e) {
            // Si ocurre algún error al procesar el token, mostramos un mensaje de error
            System.err.println("Error al obtener el usuario del token: " + e.getMessage());
            return null; // Retornamos null en caso de error
        }
    }
}
