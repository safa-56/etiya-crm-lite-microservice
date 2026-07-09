package com.etiya.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Etiya CRM Lite - Ürün (Product/ProductCatalog) servisi.
 *
 * <p>N-katmanlı (n-layered) mimari ile geliştirilmiştir:
 * <ul>
 *   <li><b>entities</b>     : JPA entity'leri (Product, ProductOffer, ProductSpec,
 *       Catalog, Campaign, outbox/inbox/projection, ...)</li>
 *   <li><b>dataAccess</b>   : Spring Data JPA repository'leri</li>
 *   <li><b>business</b>     : iş kuralları (rules), servisler, DTO'lar, mapper'lar</li>
 *   <li><b>apiController</b>: REST uçları</li>
 * </ul>
 *
 * <p>Ürün satışı bir <b>choreography Saga</b> ile kesinleşir (Customer ↔ Account
 * modeliyle aynı): ürün PENDING açılır, {@code crm.ProductSaga.events} üzerinden
 * account-service'ten fatura hesabı doğrulaması istenir; sonuca göre ürün
 * ACTIVE (onay) ya da CANCELLED (telafi) olur. Onaylanınca {@code ProductCreated}
 * olayı yayınlanır ve account-service aktif ürün sayısını günceller.
 *
 * <p>Asenkron iletişim <b>Kafka</b> üzerinden; olaylar <b>Transactional Outbox
 * Pattern + Debezium</b> ile yayınlanır (ghost event yok), tüketimde <b>Inbox
 * Pattern</b> ile tekilleştirilir (duplicate consume yok). Cacheleme <b>Redis</b>
 * (Spring Cache) ile sağlanır; cache'ler RedisInsight ile izlenir.
 */
@SpringBootApplication
@EnableCaching
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
