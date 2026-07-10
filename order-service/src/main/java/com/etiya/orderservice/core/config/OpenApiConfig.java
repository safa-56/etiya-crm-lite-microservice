package com.etiya.orderservice.core.config;

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
    public OpenAPI orderServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Etiya CRM Lite - Order Service API")
                .description("Sipariş (Order) işlemleri: sepetten siparişe geçiş "
                        + "(checkout / Submit Order - FR-016), sipariş görüntüleme ve iptal")
                .version("1.0.0"));
    }
}
