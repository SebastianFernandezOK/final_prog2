package ar.edu.um.events_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import ar.edu.um.events_backend.auth.ExternalAuthService;
import ar.edu.um.events_backend.auth.ProxyAuthService;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    @Bean("externalApiClient")
    public WebClient externalApiClient(WebClient.Builder builder, ExternalAuthService authService, @Value("${externalAuth.base-url}") String baseUrl) {
        ExchangeFilterFunction authFilter = ExchangeFilterFunction.ofRequestProcessor(request -> {
            String token = authService.getToken();
            ClientRequest req = ClientRequest.from(request)
                    .headers(h -> h.setBearerAuth(token))
                    .build();
            return Mono.just(req);
        });

        ExchangeFilterFunction retryOn401 = (request, next) ->
                next.exchange(request).flatMap(response -> {
                    if (response.statusCode().value() == 401) {
                        return response.bodyToMono(String.class).defaultIfEmpty("").flatMap(b -> {
                            authService.invalidateToken();
                            String newToken = authService.getToken();
                            ClientRequest retry = ClientRequest.from(request)
                                    .headers(h -> h.setBearerAuth(newToken))
                                    .build();
                            return next.exchange(retry);
                        });
                    }
                    return Mono.just(response);
                });

        return builder.baseUrl(baseUrl).filter(authFilter).filter(retryOn401).build();
    }

    /**
     * WebClient para comunicarse con el proxy
     * 
     * Incluye:
     * - Autenticación JWT automática usando ProxyAuthService
     * - Renovación automática del token cuando expira (401)
     */
    @Bean("proxyClient")
    public WebClient proxyClient(WebClient.Builder builder, ProxyAuthService proxyAuthService, @Value("${proxy.base-url:http://localhost:8082}") String proxyBaseUrl) {
        // Filtro para agregar el token JWT en cada petición
        ExchangeFilterFunction authFilter = ExchangeFilterFunction.ofRequestProcessor(request -> {
            String token = proxyAuthService.getToken();
            ClientRequest req = ClientRequest.from(request)
                    .headers(h -> h.setBearerAuth(token))
                    .build();
            return Mono.just(req);
        });

        // Filtro para manejar 401 (token expirado) y reintentar con nuevo token
        ExchangeFilterFunction retryOn401 = (request, next) ->
                next.exchange(request).flatMap(response -> {
                    if (response.statusCode().value() == 401) {
                        // Token expirado, invalidar y obtener uno nuevo
                        return response.bodyToMono(String.class).defaultIfEmpty("").flatMap(b -> {
                            proxyAuthService.invalidateToken();
                            String newToken = proxyAuthService.getToken();
                            ClientRequest retry = ClientRequest.from(request)
                                    .headers(h -> h.setBearerAuth(newToken))
                                    .build();
                            return next.exchange(retry);
                        });
                    }
                    return Mono.just(response);
                });

        return builder.baseUrl(proxyBaseUrl).filter(authFilter).filter(retryOn401).build();
    }
}
