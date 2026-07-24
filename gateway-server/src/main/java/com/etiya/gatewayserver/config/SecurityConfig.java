package com.etiya.gatewayserver.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Etiya CRM Lite - API Gateway güvenlik konfigürasyonu (reaktif / WebFlux).
 *
 * <p>Gateway, sistemin tek giriş noktasıdır; kimlik doğrulama ve yetki kontrolü
 * burada merkezîleştirilir. Keycloak'ın ({@code etiya-crm} realm) ürettiği JWT
 * access token'ları OAuth2 Resource Server olarak doğrulanır:
 * <ul>
 *   <li>İmza + issuer doğrulaması JWKS üzerinden otomatik yapılır
 *       ({@code spring.security.oauth2.resourceserver.jwt.issuer-uri}).</li>
 *   <li>Keycloak realm rolleri ({@code realm_access.roles}) ve client rolleri
 *       ({@code resource_access.<client>.roles}) Spring authority'lerine
 *       ({@code ROLE_*}) dönüştürülür; ileride route/metot bazlı yetki için kullanılır.</li>
 * </ul>
 *
 * <p>Genel (public) uçlar: actuator health/info ve OpenAPI. Diğer tüm istekler
 * geçerli bir JWT gerektirir.
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    /**
     * Kimlik doğrulaması gerektirmeyen (public) yol desenleri.
     * Health probe'ları ve API dokümantasyonu dışarıya açık kalır.
     */
    private static final String[] PUBLIC_PATHS = {
            "/actuator/health/**",
            "/actuator/info",
            "/actuator/prometheus",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                // Gateway'de oturum tutulmaz; her istek Bearer token ile stateless doğrulanır.
                // CSRF token tabanlı akış için gereksizdir (tarayıcı cookie'si kullanılmaz).
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                // Tarayıcı (Angular :4200) çapraz-origin isteklerine izin ver. Preflight
                // (OPTIONS) zaten permitAll; burada da yanıt CORS başlıkları eklenir.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchange -> exchange
                        // Preflight CORS istekleri serbest.
                        .pathMatchers(org.springframework.http.HttpMethod.OPTIONS).permitAll()
                        .pathMatchers(PUBLIC_PATHS).permitAll()
                        // Diğer tüm uçlar geçerli JWT ister.
                        .anyExchange().authenticated())
                // JWT Resource Server: issuer-uri konfigürasyondan gelir (Keycloak realm).
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter())));

        return http.build();
    }

    /**
     * Tarayıcı tabanlı SPA (Angular geliştirme sunucusu :4200) için CORS politikası.
     *
     * <p>Bearer token ile çalışıldığından cookie/credential paylaşımı gerekmez
     * ({@code allowCredentials=false}); origin açıkça listelenir. Tüm iş uçları
     * ({@code /**}) bu politikayı kullanır.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Keycloak JWT'sindeki rolleri Spring authority'lerine dönüştürür.
     * {@code scope}/{@code scp} claim'lerinden gelen {@code SCOPE_*} authority'lerine
     * ek olarak {@code realm_access.roles} ve {@code resource_access.*.roles}
     * değerlerini {@code ROLE_*} authority'si olarak ekler.
     */
    private ReactiveJwtAuthenticationConverter keycloakJwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();

        Converter<Jwt, Collection<GrantedAuthority>> combined = jwt -> Stream
                .concat(scopes.convert(jwt).stream(), keycloakRoles(jwt).stream())
                .collect(Collectors.toSet());

        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(
                new ReactiveJwtGrantedAuthoritiesConverterAdapter(combined));
        return converter;
    }

    /**
     * {@code realm_access.roles} ve {@code resource_access.<client>.roles} içindeki
     * rolleri {@code ROLE_<rol>} authority'lerine map'ler.
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> keycloakRoles(Jwt jwt) {
        Stream<String> realmRoles = Stream.ofNullable(jwt.getClaimAsMap("realm_access"))
                .map(m -> (List<String>) m.get("roles"))
                .filter(java.util.Objects::nonNull)
                .flatMap(List::stream);

        Stream<String> clientRoles = Stream.ofNullable(jwt.getClaimAsMap("resource_access"))
                .flatMap(m -> m.values().stream())
                .filter(v -> v instanceof Map)
                .map(v -> (Map<String, Object>) v)
                .map(m -> (List<String>) m.get("roles"))
                .filter(java.util.Objects::nonNull)
                .flatMap(List::stream);

        return Stream.concat(realmRoles, clientRoles)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }
}
