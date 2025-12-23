package ar.edu.um.events_backend.controller;

import ar.edu.um.events_backend.auth.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;

    @Value("${application.security.auth.secret}")
    private String authSecret;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Endpoint público para obtener un token JWT
     * 
     * Request body esperado: {"secret": "mi-secret-super-seguro-2025"}
     * 
     * Flujo:
     * 1. Cliente envía el secret en el body
     * 2. Se valida contra el secret configurado en application.yaml
     * 3. Si es válido, se genera un token JWT firmado que dura 24 horas
     * 4. Se retorna el token al cliente para usar en futuras peticiones
     */
    @PostMapping("/token")
    public ResponseEntity<?> getToken(@RequestBody Map<String, String> request) {
        // 1. Extraer el secret del body de la petición
        String secret = request.get("secret");
        
        System.out.println("Secret recibido: " + secret);
        System.out.println("Secret esperado: " + authSecret);
        
        // 2. Validar que el secret sea correcto
        // Si no coincide con el configurado en application.yaml, rechazar
        if (secret == null || !secret.equals(authSecret)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid secret");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);  // 401 Unauthorized
        }

        // 3. Si el secret es válido, generar un token JWT
        // Este token estará firmado con el secret-key y durará 24 horas
        String token = jwtService.generateToken();
        
        // 4. Retornar el token al cliente
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        
        return ResponseEntity.ok(response);  // 200 OK con el token
    }
}
