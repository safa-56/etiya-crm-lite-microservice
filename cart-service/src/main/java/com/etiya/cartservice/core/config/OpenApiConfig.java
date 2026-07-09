package com.etiya.cartservice.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI meta bilgisi.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cartServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Etiya CRM Lite - Cart Service API")
                .description("Sepet (Cart) ve sepet satırı (CartItem) işlemleri: "
                        + "sepet CRUD, katalogdan teklif ekleme ve kampanya (paket) ekleme")
                .version("1.0.0"));
    }
}
