package com.etiya.accountservice.core.config;

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
    public OpenAPI accountServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Etiya CRM Lite - Account Service API")
                .description("Fatura hesabı (BillingAccount) CRUD işlemleri")
                .version("1.0.0"));
    }
}
