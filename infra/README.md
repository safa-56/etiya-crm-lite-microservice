# infra — Altyapı Servisleri

`customer-service` için gereken altyapıyı ayağa kaldırır: **PostgreSQL**, **Redis**,
**RedisInsight**, **Kafka (local, KRaft)**, **Kafka UI** ve **Debezium (Kafka Connect)**.

> **Kafka (local container):** Kafka broker'ı bu compose'da yer alır (Zookeeper'sız
> KRaft, tek node). Uygulama **Spring Cloud Stream (Kafka binder)** ile bağlanır;
> Debezium outbox olaylarını bu yerel broker'a yazar. Kafka Cloud kullanılmaz.

## 1. Kurulum

```bash
cp infra/.env.example infra/.env      # opsiyonel; default değerler de çalışır
docker compose -f infra/docker-compose.yml up -d
```

| Servis        | Adres                     | Not                                        |
|---------------|---------------------------|--------------------------------------------|
| PostgreSQL    | `localhost:5432`          | db: `customerdb`                           |
| Redis         | `localhost:6379`          | cache                                      |
| RedisInsight  | http://localhost:5540     | Redis host olarak `redis:6379` ekleyin     |
| Kafka         | `localhost:9092` (host)   | konteyner içi: `kafka:29092`               |
| Kafka UI      | http://localhost:8090     | topic/mesaj/consumer group izleme          |
| Kafka Connect | http://localhost:8083     | Debezium REST API                          |

## 2. Debezium Outbox Connector kaydı

Servis en az bir kez çalışıp `outbox_events` tablosunu oluşturduktan sonra:

```bash
curl -i -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @infra/debezium/register-outbox-connector.json

# Durum:
curl -s http://localhost:8083/connectors/customer-outbox-connector/status | jq
```

### Nasıl çalışır? (Transactional Outbox + Debezium)

1. `IndividualCustomerManager`, iş verisini (`customers`, `individual_customers`, …)
   **ve** bir `outbox_events` kaydını **aynı DB transaction'ında** yazar.
2. Transaction commit olur → veri ve olay atomik olarak kalıcı olur
   (**ghost event** yok: ya ikisi de olur ya hiçbiri).
3. Debezium, Postgres WAL'ını (logical replication) izler; yeni `outbox_events`
   satırını yakalar ve **EventRouter SMT** ile `crm.<aggregateType>.events`
   topic'ine (yerel Kafka broker'ı) publish eder.
4. Tüketiciler bu olayları okurken **Inbox Pattern** (`inbox_messages`) ile
   `message_id` bazlı idempotency uygular → **duplicate consume** engellenir.

## 3. Temizlik

```bash
docker compose -f infra/docker-compose.yml down          # servisleri durdur
docker compose -f infra/docker-compose.yml down -v       # + volume'leri sil
```
