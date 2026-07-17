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

## 2. Debezium Outbox Connector kaydı (otomatik)

**Elle bir şey yapmanıza gerek yok.** `docker compose up` ile birlikte gelen
`debezium-registrar` servisi, `infra/debezium/register-*.json` dosyalarının
hepsini Kafka Connect'e kaydeder ve işi bitince durur.

Nasıl çalışır:

1. `debezium-connect` healthcheck'i geçene (REST API hazır olana) kadar bekler.
2. Her JSON için `PUT /connectors/<name>/config` çağırır. Bu **idempotent**'tir:
   connector yoksa oluşturulur, varsa config'i güncellenir — replication slot ve
   offset'ler korunduğu için tekrar tekrar çalıştırmak güvenlidir.
3. Connector'lar `outbox_events` tablosunu ister. Servis henüz ayağa kalkmadıysa
   tablo yoktur ve task `FAILED` olur; registrar bu durumda connector'ı
   `RUNNING` olana kadar restart ederek yeniden dener (varsayılan: 10 sn arayla
   30 deneme ≈ 5 dk). Yani servislerin şemayı oluşturmasını beklemeye gerek yok.

Kayıtları görmek / doğrulamak:

```bash
docker compose -f infra/docker-compose.yml logs debezium-registrar
curl -s http://localhost:8083/connectors | jq
curl -s http://localhost:8083/connectors/customer-outbox-connector/status | jq
```

Kafka UI'dan da izlenebilir: http://localhost:8090 → **Kafka Connect** sekmesi.

### Yeni connector eklemek

`infra/debezium/` altına `register-<servis>-connector.json` adında bir dosya
bırakın — dosya adı deseni `register-*.json` ile eşleştiği sürece registrar onu
otomatik alır. Sonra registrar'ı tekrar çalıştırın (compose'un tamamını yeniden
başlatmaya gerek yok):

```bash
docker compose -f infra/docker-compose.yml up debezium-registrar
```

Aynı komut, mevcut bir connector'ın JSON'unu değiştirdiğinizde config'i
güncellemek için de kullanılır.

### Elle kayıt (opsiyonel)

Registrar'ı devre dışı bırakmadan da REST API'yi doğrudan kullanabilirsiniz:

```bash
curl -i -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @infra/debezium/register-outbox-connector.json
```

Connector adları: `customer-outbox-connector`, `account-outbox-connector`,
`product-outbox-connector`, `cart-outbox-connector`, `order-outbox-connector`.

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
