package com.etiya.productservice.core.config;

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
    public OpenAPI productServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Etiya CRM Lite - Product Service API")
                .description("Ürün kataloğu (ProductSpec, ProductOffer, Catalog, Campaign) ve satılmış ürün (Product) işlemleri")
                .version("1.0.0"));
    }
}
