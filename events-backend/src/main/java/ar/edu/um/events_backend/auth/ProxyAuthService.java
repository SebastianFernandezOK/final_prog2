package ar.edu.um.events_backend.auth;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para manejar la autenticación con el proxy
 * 
 * Este servicio:
 * 1. Obtiene tokens JWT del endpoint /api/auth/token del backend
 * 2. Cachea el token en memoria para reutilizarlo
 * 3. Renueva automáticamente el token cuando expira (24 horas)
 * 4. Maneja reintentos automáticos en caso de 401
 */
@Service
public class ProxyAuthService {
    private static final Logger logger = LoggerFactory.getLogger(ProxyAuthService.class);

    @Value("${application.security.auth.secret}")
    private String authSecret;

    @Value("${server.address:localhost}")
    private String serverAddress;

    @Value("${server.port:8081}")
    private String serverPort;

    private final WebClient webClient;
    private final Object lock = new Object();

    private String token;
    private long expiryMillis = 0;

    public ProxyAuthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @PostConstruct
    public void init() {
        logger.info("Inicializando ProxyAuthService para autenticación con el proxy");
    }

    /**
     * Obtiene un token JWT válido del backend
     * 
     * Si el token está en cache y no ha expirado, lo retorna.
     * Si no hay token o expiró, obtiene uno nuevo del endpoint /api/auth/token
     */
    private void fetchToken() {
        try {
            // Construir la URL del backend (localhost)
            String baseUrl = "http://" + serverAddress + ":" + serverPort;
            
            logger.info("Obteniendo nuevo token JWT para el proxy desde: {}/api/auth/token", baseUrl);
            
            // Preparar el body con el secret
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("secret", authSecret);

            // Llamar al endpoint /api/auth/token del backend
            JsonNode response = webClient.post()
                    .uri(baseUrl + "/api/auth/token")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("token")) {
                token = response.get("token").asText();
                
                // Extraer el tiempo de expiración del token (24 horas = 86400000 ms)
                // Le restamos 1 minuto de buffer para renovar antes de que expire
                long ttlMillis = 86400000L - 60000L; // 24h - 1min
                expiryMillis = System.currentTimeMillis() + ttlMillis;

                logger.info("Token JWT obtenido exitosamente para el proxy. Expira en {} minutos", ttlMillis / 60000);
            } else {
                logger.error("Respuesta de autenticación no contiene token");
            }
        } catch (Exception e) {
            logger.error("Error al obtener token JWT para el proxy: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo obtener token para el proxy", e);
        }
    }

    /**
     * Verifica si el token está próximo a expirar (menos de 1 minuto)
     */
    private boolean isExpiringSoon() {
        long now = System.currentTimeMillis();
        long bufferMillis = 60000L; // 1 minuto de buffer
        return (expiryMillis - now) < bufferMillis;
    }

    /**
     * Obtiene un token JWT válido
     * 
     * Si no hay token o está por expirar, obtiene uno nuevo.
     * Thread-safe usando sincronización.
     * 
     * @return Token JWT válido para usar con el proxy
     */
    public String getToken() {
        if (token == null || isExpiringSoon()) {
            synchronized (lock) {
                // Double-check después del lock
                if (token == null || isExpiringSoon()) {
                    fetchToken();
                }
            }
        }
        return token;
    }

    /**
     * Invalida el token actual
     * 
     * Se llama cuando el proxy retorna 401, forzando la obtención de un nuevo token
     */
    public void invalidateToken() {
        synchronized (lock) {
            logger.info("Invalidando token del proxy. Se obtendrá uno nuevo en la próxima petición");
            token = null;
            expiryMillis = 0;
        }
    }
}
