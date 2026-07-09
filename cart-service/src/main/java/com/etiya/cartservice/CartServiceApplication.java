package com.etiya.cartservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Etiya CRM Lite - Sepet (Cart) servisi.
 *
 * <p>N-katmanlı (n-layered) mimari ile geliştirilmiştir:
 * <ul>
 *   <li><b>entities</b>     : JPA entity'leri (Cart, CartItem, outbox/inbox ve
 *       product-service'ten beslenen offer/campaign projeksiyonları)</li>
 *   <li><b>dataAccess</b>   : Spring Data JPA repository'leri</li>
 *   <li><b>business</b>     : iş kuralları (rules), servisler, DTO'lar, mapper'lar,
 *       Kafka tüketici binding'leri (messaging)</li>
 *   <li><b>apiController</b>: REST uçları</li>
 * </ul>
 *
 * <p>Sepete ürün eklemenin iki yolu vardır (FR-014): (1) katalogdan doğrudan bir
 * <b>product offer</b> seçmek; (2) içinde birden çok teklif bulunan bir
 * <b>campaign</b>'i tek paket fiyatıyla bir bütün olarak eklemek.
 *
 * <p>Offer ve campaign bilgisi product-service'e <b>senkron bağımlılık kurulmadan</b>,
 * account ↔ customer / product ↔ account modeliyle aynı şekilde <b>Kafka event
 * listener</b>'ları üzerinden alınır: product-service teklif/kampanya olaylarını
 * <b>Transactional Outbox + Debezium</b> ile yayınlar (ghost event yok); cart-service
 * bunları <b>Inbox Pattern</b> ile tekilleştirerek (duplicate consume yok) yerel bir
 * read-model'e (projeksiyon) işler. Sepet, ekleme anında bu projeksiyondan doğrulama
 * ve fiyat snapshot'ı yapar. Cacheleme <b>Redis</b> (Spring Cache) ile sağlanır;
 * cache'ler RedisInsight ile izlenir.
 */
@SpringBootApplication
@EnableCaching
public class CartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }
}
