package com.etiya.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Etiya CRM Lite - Merkezi Konfigürasyon sunucusu.
 *
 * Spring Cloud Config Server, tüm mikroservislerin ortam bazlı (dev/test/prod/
 * docker) konfigürasyonunu tek bir kaynaktan sunar. Arka planda bu reponun
 * kökündeki {@code configs/} klasörünü bir Git deposu üzerinden okur ve
 * {@code /{application}/{profile}} endpoint'i ile client'lara dağıtır.
 *
 * {@code @EnableConfigServer} bu uygulamayı bir config server'a dönüştürür;
 * uygulamanın kendisi başka bir config server'dan beslenmez (yumurta-tavuk
 * problemini önlemek için kendi konfigürasyonunu yereldan yükler).
 */
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
