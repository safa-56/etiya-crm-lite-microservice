package com.etiya.customerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Etiya CRM Lite - Müşteri (Customer) servisi.
 *
 * <p>N-katmanlı (n-layered) mimari ile geliştirilmiştir:
 * <ul>
 *   <li><b>entities</b>     : JPA entity'leri (Customer, IndividualCustomer, ...)</li>
 *   <li><b>dataAccess</b>   : Spring Data JPA repository'leri</li>
 *   <li><b>business</b>     : iş kuralları (rules), servisler, DTO'lar, mapper'lar</li>
 *   <li><b>apiController</b>: REST uçları</li>
 * </ul>
 *
 * <p>Asenkron iletişim <b>Kafka Cloud</b> üzerinden; olaylar
 * <b>Transactional Outbox Pattern + Debezium</b> ile yayınlanır (ghost event yok),
 * tüketimde <b>Inbox Pattern</b> ile tekilleştirme yapılır (duplicate consume yok).
 * Cacheleme <b>Redis</b> (Spring Cache) ile sağlanır.
 */
@SpringBootApplication
@EnableCaching
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}
