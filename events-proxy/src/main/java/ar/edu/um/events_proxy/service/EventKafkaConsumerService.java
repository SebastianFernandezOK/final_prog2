package ar.edu.um.events_proxy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EventKafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(EventKafkaConsumerService.class);
    
    private final WebClient webClient;
    
    @Value("${backend.base-url}")
    private String backendBaseUrl;
    
    @Value("${backend.sync-endpoint}")
    private String syncEndpoint;

    public EventKafkaConsumerService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Listens to Kafka messages about event changes.
     * When a message arrives, notifies the backend to sync its local database.
     * 
     * @param message The JSON message received from Kafka
     */
    @KafkaListener(topics = "eventos-actualizacion", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeEventUpdate(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        long startTime = System.currentTimeMillis();
        
        logger.info("╔════════════════════════════════════════════════════════════════");
        logger.info("║ KAFKA NOTIFICATION RECEIVED");
        logger.info("╠════════════════════════════════════════════════════════════════");
        logger.info("║ Timestamp: {}", timestamp);
        logger.info("║ Topic: event-updates");
        logger.info("║ Message Content: {}", message);
        logger.info("╚════════════════════════════════════════════════════════════════");
        
        try {
            String fullUrl = backendBaseUrl + syncEndpoint;
            logger.info("→ Sending notification to backend: {}", fullUrl);
            
            String response = webClient.post()
                    .uri(fullUrl)
                    .bodyValue(message)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            logger.info("╔════════════════════════════════════════════════════════════════");
            logger.info("║ NOTIFICATION SENT SUCCESSFULLY");
            logger.info("╠════════════════════════════════════════════════════════════════");
            logger.info("║ Backend URL: {}", fullUrl);
            logger.info("║ Response: {}", response != null ? response : "No response body");
            logger.info("║ Processing Time: {} ms", processingTime);
            logger.info("║ Status: SUCCESS");
            logger.info("╚════════════════════════════════════════════════════════════════");
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            
            logger.error("╔════════════════════════════════════════════════════════════════");
            logger.error("║ NOTIFICATION FAILED");
            logger.error("╠════════════════════════════════════════════════════════════════");
            logger.error("║ Error Message: {}", e.getMessage());
            logger.error("║ Processing Time: {} ms", processingTime);
            logger.error("║ Status: FAILED");
            logger.error("╚════════════════════════════════════════════════════════════════");
            logger.error("Full stack trace:", e);
        }
    }
}
