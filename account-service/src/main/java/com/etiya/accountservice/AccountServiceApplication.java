package com.etiya.accountservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Etiya CRM Lite - Fatura hesabı (Account/BillingAccount) servisi.
 *
 * <p>N-katmanlı (n-layered) mimari ile geliştirilmiştir:
 * <ul>
 *   <li><b>entities</b>     : JPA entity'leri (BillingAccount, outbox/inbox, ...)</li>
 *   <li><b>dataAccess</b>   : Spring Data JPA repository'leri</li>
 *   <li><b>business</b>     : iş kuralları (rules), servisler, DTO'lar, mapper'lar</li>
 *   <li><b>apiController</b>: REST uçları</li>
 * </ul>
 *
 * <p>Asenkron iletişim <b>Kafka</b> üzerinden; olaylar
 * <b>Transactional Outbox Pattern + Debezium</b> ile yayınlanır (ghost event yok),
 * tüketimde <b>Inbox Pattern</b> ile tekilleştirme yapılır (duplicate consume yok).
 * Cacheleme <b>Redis</b> (Spring Cache) ile sağlanır; cache'ler RedisInsight ile izlenir.
 */
@SpringBootApplication
@EnableCaching
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
