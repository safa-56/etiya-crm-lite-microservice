package com.etiya.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Etiya CRM Lite - Service Discovery sunucusu.
 *
 * {@link EnableEurekaServer} anotasyonu bu uygulamayı bir Eureka registry
 * sunucusuna dönüştürür. Diğer mikroservisler (customer, account vb.)
 * bu sunucuya kayıt olur ve birbirlerini isimle (logical service id)
 * üzerinden keşfeder.
 */
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
