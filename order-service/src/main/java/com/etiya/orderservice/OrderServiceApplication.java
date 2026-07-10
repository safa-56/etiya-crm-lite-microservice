package com.etiya.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Etiya CRM Lite - Sipariş (Order) servisi.
 *
 * <p>N-katmanlı (n-layered) mimari ile geliştirilmiştir:
 * <ul>
 *   <li><b>entities</b>     : JPA entity'leri (Order, OrderItem, outbox/inbox)</li>
 *   <li><b>dataAccess</b>   : Spring Data JPA repository'leri</li>
 *   <li><b>business</b>     : iş kuralları (rules), servisler, DTO'lar, mapper'lar,
 *       Kafka tüketici binding'leri (messaging)</li>
 *   <li><b>apiController</b>: REST uçları</li>
 * </ul>
 *
 * <p>Servis, sepetten siparişe geçişi (checkout — <b>FR-016 Siparişin Tamamlanması</b>)
 * karşılar: kullanıcı bir sepeti "Submit Order" ile onayladığında sistem benzersiz bir
 * sipariş numarası üretir, sepetteki teklifleri/toplam tutarı sipariş satırlarına
 * snapshot'lar ve servis adresini kaydeder.
 *
 * <p>Sepetin otoriter içeriği (satırlar, fiyat, toplam) cart-service'e <b>senkron çağrı
 * yapılmadan</b>, account ↔ customer / product ↔ account / cart ↔ product modeliyle aynı
 * şekilde bir <b>choreography Saga</b> ile alınır: sipariş {@code PENDING} açılır ve
 * cart-service'ten doğrulama istenir (Transactional Outbox + Debezium — ghost event yok);
 * cart-service (doğrulayıcı) sepeti otoriter kontrol edip satır/toplam snapshot'ıyla sonuç
 * yayınlar; order-service sonucu <b>Inbox Pattern</b> ile tekilleştirerek (duplicate consume
 * yok) siparişi CONFIRMED (onay) ya da CANCELLED (telafi) yapar. Cacheleme <b>Redis</b>
 * (Spring Cache) ile sağlanır; cache'ler RedisInsight ile izlenir.
 */
@SpringBootApplication
@EnableCaching
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
