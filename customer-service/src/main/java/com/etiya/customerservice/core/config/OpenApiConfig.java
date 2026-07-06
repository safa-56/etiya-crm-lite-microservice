package com.etiya.customerservice.core.config;

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
    public OpenAPI customerServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Etiya CRM Lite - Customer Service API")
                .description("Bireysel müşteri (IndividualCustomer) CRUD işlemleri")
                .version("1.0.0"));
    }
}
