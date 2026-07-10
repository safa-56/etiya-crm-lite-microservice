package com.etiya.searchservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Etiya CRM Lite - Müşteri arama (Search) servisi.
 *
 * <p>FR-002 "Müşteri Arama ve Görüntüleme" ekranını karşılayan bir <b>CQRS
 * read-model</b>'dir. Yazma tarafına (müşteri/hesap CRUD mantığına) dokunmaz;
 * mevcut Kafka olay akışlarını ({@code crm.Customer.events},
 * {@code crm.Account.events}) dinleyip tek bir denormalize
 * {@code customer_search_index} tablosu tutar ve tek bir uçtan
 * ({@code GET /api/v1/search/customers}) sorgular.
 *
 * <p>N-katmanlı (n-layered) mimari; olay tüketiminde <b>Inbox Pattern</b> ile
 * tekilleştirme yapılır (duplicate consume yok). Cacheleme <b>Redis</b> (Spring
 * Cache) ile sağlanır. Servisin kendi outbox'ı yoktur (yalnızca tüketici).
 */
@SpringBootApplication
@EnableCaching
public class SearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
