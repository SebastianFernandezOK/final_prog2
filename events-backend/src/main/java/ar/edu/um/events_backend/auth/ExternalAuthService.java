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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class ExternalAuthService {
    private static final Logger logger = LoggerFactory.getLogger(ExternalAuthService.class);

    @Value("${externalAuth.base-url}")
    private String baseUrl;

    @Value("${externalAuth.login-path}")
    private String loginPath;

    @Value("${externalAuth.username}")
    private String username;

    @Value("${externalAuth.password}")
    private String password;

    @Value("${externalAuth.buffer-seconds:30}")
    private int bufferSeconds;

    private final WebClient webClient;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Object lock = new Object();

    private String token;
    private long expiryMillis = 0;
    private ScheduledFuture<?> scheduledRefresh;

    public ExternalAuthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @PostConstruct
    public void init() {
        logger.info("Inicializando ExternalAuthService");
    }

    private void fetchAndSchedule() {
        try {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", username);
            credentials.put("password", password);

            JsonNode response = webClient.post()
                    .uri(baseUrl + loginPath)
                    .bodyValue(credentials)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("id_token")) {
                token = response.get("id_token").asText();
                
                long expSeconds = JwtUtils.getExpSeconds(token);
                long iatSeconds = JwtUtils.getIatSeconds(token);
                long ttlSeconds = expSeconds - iatSeconds;
                long refreshInSeconds = Math.max(ttlSeconds - bufferSeconds, 10);

                expiryMillis = System.currentTimeMillis() + (ttlSeconds * 1000);

                if (scheduledRefresh != null) {
                    scheduledRefresh.cancel(false);
                }
                scheduledRefresh = scheduler.schedule(this::fetchAndSchedule, refreshInSeconds, TimeUnit.SECONDS);

                logger.info("Token obtenido exitosamente. Expira en {} segundos. Próxima renovación en {} segundos",
                        ttlSeconds, refreshInSeconds);
            } else {
                logger.error("Respuesta de autenticación no contiene id_token");
            }
        } catch (Exception e) {
            logger.error("Error al obtener token de autenticación: {}", e.getMessage(), e);
            scheduledRefresh = scheduler.schedule(this::fetchAndSchedule, 60, TimeUnit.SECONDS);
        }
    }

    private boolean isExpiringSoon() {
        long now = System.currentTimeMillis();
        long bufferMillis = bufferSeconds * 1000L;
        return (expiryMillis - now) < bufferMillis;
    }

    public String getToken() {
        if (token == null || isExpiringSoon()) {
            synchronized (lock) {
                if (token == null || isExpiringSoon()) {
                    fetchAndSchedule();
                }
            }
        }
        return token;
    }

    public void invalidateToken() {
        synchronized (lock) {
            token = null;
            expiryMillis = 0;
            if (scheduledRefresh != null) {
                scheduledRefresh.cancel(false);
                scheduledRefresh = null;
            }
        }
    }
}
