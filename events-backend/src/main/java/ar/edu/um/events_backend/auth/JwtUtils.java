package ar.edu.um.events_backend.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;

public final class JwtUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JwtUtils() {}

    public static JsonNode decodePayload(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return null;
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            return MAPPER.readTree(payload);
        } catch (Exception e) {
            return null;
        }
    }

    public static Long getExpSeconds(String jwt) {
        JsonNode payload = decodePayload(jwt);
        if (payload == null || !payload.has("exp")) return null;
        return payload.get("exp").asLong();
    }

    public static Long getIatSeconds(String jwt) {
        JsonNode payload = decodePayload(jwt);
        if (payload == null || !payload.has("iat")) return null;
        return payload.get("iat").asLong();
    }

    public static String prettyPayload(String jwt) {
        JsonNode p = decodePayload(jwt);
        if (p == null) return null;
        try { return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(p); }
        catch (Exception e) { return p.toString(); }
    }
}
