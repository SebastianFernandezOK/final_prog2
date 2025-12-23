package ar.edu.um.events_proxy.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Filtro de autenticación JWT para el proxy
 * 
 * Este filtro intercepta todas las peticiones HTTP y valida el token JWT.
 * Si el token es válido, marca la petición como autenticada.
 * Si no hay token o es inválido, deja pasar pero Spring Security bloqueará después.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. Extraer el header "Authorization" de la petición HTTP
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        // 2. Si no hay header o no empieza con "Bearer ", dejar pasar sin autenticar
        // (Esto permite que rutas públicas funcionen si las hubiera)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token JWT quitando el prefijo "Bearer " (7 caracteres)
        // Ejemplo: "Bearer abc123" -> "abc123"
        jwt = authHeader.substring(7);

        // 4. Validar el token y verificar que no esté ya autenticado en esta petición
        if (jwtService.isTokenValid(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // 5. Crear un objeto de autenticación con:
            //    - Principal: "proxy-access" (identificador genérico)
            //    - Credentials: null (no necesarias después de validar el token)
            //    - Authorities: lista vacía (sin roles específicos)
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    "proxy-access",
                    null,
                    new ArrayList<>()
            );
            
            // 6. Agregar detalles de la petición (IP, session ID, etc.)
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // 7. Guardar la autenticación en el contexto de seguridad de Spring
            // Esto marca al usuario como autenticado para esta petición
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // 8. Continuar con el siguiente filtro en la cadena
        // Si está autenticado, podrá acceder a rutas protegidas
        // Si no está autenticado, Spring Security lo bloqueará después
        filterChain.doFilter(request, response);
    }
}
