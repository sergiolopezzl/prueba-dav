package app.apiRESTful.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class AuthManager {

    // Clave secreta para HMAC (debe ser suficientemente larga para HS256)
    private static final String SECRET_KEY_STRING = "@H$256!Cl4v3^S3gur4#C0n_L3tr4s&Númer0s%2024*+_>8XyZ!";

    // Convertir la clave secreta a un formato adecuado para HMAC
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());

    /**
     * Genera un token JWT para el usuario dado.
     * @param username El nombre del usuario.
     * @return El token JWT generado.
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) // 15 minutos
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) // Usar clave segura
                .compact();
    }

    /**
     * Valida un token JWT.
     * @param token El token JWT.
     * @return Verdadero si el token es válido, falso en caso contrario.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY) // Usar clave segura
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
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
        return false;
    }

    /**
     * Extrae el nombre de usuario del token JWT.
     * @param token El token JWT.
     * @return El nombre de usuario extraído.
     */
    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY) // Usar clave segura
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            System.err.println("Error al obtener el usuario del token: " + e.getMessage());
            return null;
        }
    }
}
