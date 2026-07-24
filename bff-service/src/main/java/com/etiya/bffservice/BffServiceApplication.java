package com.etiya.bffservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Etiya CRM Lite - Backend For Frontend (BFF).
 *
 * <p>Ekran bazlı veri toplama katmanı: bir sayfanın ihtiyaç duyduğu veriyi birden
 * çok mikroservisten (customer-service, account-service, ...) senkron olarak toplar,
 * frontend'in beklediği şekle sokup <b>tek</b> yanıt döner. Kendi veritabanı yoktur;
 * durumsuzdur (stateless).
 *
 * <p>Downstream çağrılar Eureka üzerinden {@code lb://} ile yapılır; kullanıcının
 * Bearer token'ı aynen aktarılır (token relay).
 */
@SpringBootApplication
public class BffServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffServiceApplication.class, args);
    }
}
