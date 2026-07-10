# infra — Altyapı Servisleri

`customer-service`, `account-service` ve `product-service` için gereken altyapıyı
ayağa kaldırır: **PostgreSQL**, **Redis**, **RedisInsight**, **Kafka (local, KRaft)**,
**Kafka UI** ve **Debezium (Kafka Connect)**.

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
| PostgreSQL    | `localhost:5432`          | db'ler: `customerdb`, `accountdb`, `productdb`, `cartdb`, `orderdb` |
| Redis         | `localhost:6379`          | cache                                      |
| RedisInsight  | http://localhost:5540     | Redis host olarak `redis:6379` ekleyin     |
| Kafka         | `localhost:9092` (host)   | konteyner içi: `kafka:29092`               |
| Kafka UI      | http://localhost:8090     | topic/mesaj/consumer group izleme          |
| Kafka Connect | http://localhost:8083     | Debezium REST API                          |

## 2. Debezium Outbox Connector kaydı

Servis en az bir kez çalışıp `outbox_events` tablosunu oluşturduktan sonra:

```bash
# customer-service (db: customerdb)
curl -i -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @infra/debezium/register-outbox-connector.json

# account-service (db: accountdb)
curl -i -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @infra/debezium/register-account-connector.json

# product-service (db: productdb)
curl -i -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @infra/debezium/register-product-connector.json

# cart-service (db: cartdb)
curl -i -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @infra/debezium/register-cart-connector.json

# order-service (db: orderdb)
curl -i -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @infra/debezium/register-order-connector.json

# Durum:
curl -s http://localhost:8083/connectors/customer-outbox-connector/status | jq
curl -s http://localhost:8083/connectors/account-outbox-connector/status | jq
curl -s http://localhost:8083/connectors/product-outbox-connector/status | jq
curl -s http://localhost:8083/connectors/cart-outbox-connector/status | jq
curl -s http://localhost:8083/connectors/order-outbox-connector/status | jq
```

> **Not (Sepete ekleme Saga'sı):** cart-service sepete ekleme kararını product-service
> ile bir choreography Saga üzerinden verir (`crm.CartSaga.events`). cart, doğrulama
> isteğini `cartdb` outbox'ına yazar (yeni `cart-outbox-connector` bunu yönlendirir);
> product-service teklifi/kampanyayı doğrulayıp sonucu `productdb` outbox'ına yazar
> (**mevcut product connector** yönlendirir). İki tarafın `CartSaga` kayıtları da
> aggregate_type ile aynı topic'e düşer — **ek connector gerekmez**.

> **Not (Sepetten siparişe geçiş Saga'sı):** order-service, checkout kararını (FR-016)
> cart-service ile bir choreography Saga üzerinden verir (`crm.OrderCheckoutSaga.events`).
> order, doğrulama isteğini `orderdb` outbox'ına yazar (yeni `order-outbox-connector`
> bunu yönlendirir); cart-service sepeti doğrulayıp sonucu `cartdb` outbox'ına yazar
> (**mevcut cart connector** yönlendirir). İki tarafın `OrderCheckoutSaga` kayıtları da
> aggregate_type ile aynı topic'e düşer — **ek connector gerekmez** (order connector
> yalnızca order-service'in kendi outbox'ı için gerekir).

> **accountdb var mı?** `accountdb`, postgres ilk açılışında
> `postgres/init/01-create-databases.sql` ile oluşturulur (yalnızca boş volume'de).
> Var olan bir kurulumda elle: `docker exec -it crm-postgres psql -U postgres -c "CREATE DATABASE accountdb;"`

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
