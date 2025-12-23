package ar.edu.um.events_proxy.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * Servicio para validar tokens JWT en el proxy
 * 
 * Este servicio SOLO valida tokens, no los genera.
 * Los tokens son generados por el backend y validados aquí usando el mismo secret-key.
 */
@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    /**
     * Valida si un token JWT es válido
     * 
     * Verifica:
     * 1. Que la firma sea correcta (usando el secret-key)
     * 2. Que el token no esté expirado
     * 
     * @param token El token JWT a validar
     * @return true si el token es válido, false si es inválido o expirado
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);           // Intenta parsear el token (valida la firma)
            return !isTokenExpired(token);     // Verifica que no esté expirado
        } catch (Exception e) {
            return false;                      // Si falla el parseo = token inválido o firma incorrecta
        }
    }

    /**
     * Verifica si el token está expirado comparando con la fecha actual
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrae la fecha de expiración del token
     */
    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * Extrae todos los claims (datos) del token
     * 
     * Aquí es donde se valida la firma usando el secret-key.
     * Si la firma no coincide, lanza una excepción.
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())     // Usa el secret-key para validar la firma
                .build()
                .parseSignedClaims(token)       // Parsea y valida el token (lanza excepción si es inválido)
                .getPayload();                  // Retorna los claims (datos) del token
    }

    /**
     * Convierte el secret-key de Base64 a una clave HMAC para validar
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);  // Decodifica de Base64
        return Keys.hmacShaKeyFor(keyBytes);                      // Crea la clave HMAC-SHA256
    }
}
