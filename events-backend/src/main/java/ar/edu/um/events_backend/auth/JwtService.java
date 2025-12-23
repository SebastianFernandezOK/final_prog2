package ar.edu.um.events_backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    // Genera un token JWT sin claims adicionales
    public String generateToken() {
        return generateToken(new HashMap<>());
    }

    // Genera un token JWT con claims personalizados (datos extra)
    public String generateToken(Map<String, Object> extraClaims) {
        return buildToken(extraClaims, jwtExpiration);
    }

    // Construye el token JWT con todos sus componentes
    private String buildToken(Map<String, Object> extraClaims, long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)                                           // Claims adicionales (ej: roles, permisos)
                .setSubject("api-access")                                         // Sujeto del token (identificador)
                .setIssuedAt(new Date(System.currentTimeMillis()))               // Fecha de creación del token
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Fecha de expiración (24 horas)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)              // Firma con secret-key usando HMAC-SHA256
                .compact();                                                       // Genera el string final del token
    }

    // Valida si un token es válido (firma correcta y no expirado)
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);           // Intenta parsear el token (valida la firma)
            return !isTokenExpired(token);     // Verifica que no esté expirado
        } catch (Exception e) {
            return false;                      // Si falla el parseo = token inválido o firma incorrecta
        }
    }

    // Verifica si el token está expirado comparando con la fecha actual
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extrae la fecha de expiración del token
    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    // Extrae todos los claims (datos) del token
    // Aquí es donde se valida la firma usando el secret-key
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())  // Usa el secret-key para validar la firma
                .build()
                .parseClaimsJws(token)          // Parsea y valida el token (lanza excepción si es inválido)
                .getBody();                     // Retorna los claims (datos) del token
    }

    // Convierte el secret-key de Base64 a una clave HMAC para firmar/validar
    private Key getSignInKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);  // Decodifica de Base64
        return Keys.hmacShaKeyFor(keyBytes);                      // Crea la clave HMAC-SHA256
    }
}
