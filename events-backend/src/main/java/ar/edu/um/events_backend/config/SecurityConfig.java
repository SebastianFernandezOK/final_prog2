package ar.edu.um.events_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import ar.edu.um.events_backend.auth.JwtAuthFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Configuración de la cadena de filtros de seguridad
     * 
     * Define qué rutas son públicas y cuáles requieren autenticación JWT
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF (no necesario para APIs REST stateless)
                .csrf(csrf -> csrf.disable())
                
                // Configurar autorización de peticiones HTTP
                .authorizeHttpRequests(auth -> auth
                        // Ruta pública: permite obtener token sin autenticación
                        .requestMatchers("/api/auth/token").permitAll()
                        
                        // Rutas internas: para comunicación entre microservicios
                        .requestMatchers("/internal/**").permitAll()
                        
                        // Todas las demás rutas requieren autenticación JWT
                        .anyRequest().authenticated()
                )
                
                // Configurar sesiones como STATELESS (sin estado en servidor)
                // Cada petición debe incluir el token JWT
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Agregar el filtro JWT antes del filtro de autenticación por usuario/contraseña
                // Esto intercepta todas las peticiones para validar el token JWT
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
