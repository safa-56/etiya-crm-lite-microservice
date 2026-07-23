# Etiya CRM Lite - Project Brain

> Bu dosya projenin canlı hafızasıdır. Mimari kararlar, standartlar ve ilerleme
> burada tutulur. Projede ilerledikçe güncellenir.

_Son güncelleme: 2026-07-23_

---

## 1. Proje Özeti

Etiya CRM Lite, mikroservis mimarisiyle geliştirilen bir CRM uygulamasıdır. Her
servis bağımsız olarak paketlenir (`jar`) ve çalıştırılır; ortak yapı ve bağımlılık
yönetimi merkezi bir **parent POM** üzerinden sağlanır.

**Modüller (9):**

| Servis             | Port | DB          | Rolü                                                        |
|--------------------|------|-------------|--------------------------------------------------------------|
| `config-server`    | 8888 | -           | Merkezi konfigürasyon (Spring Cloud Config, Git backend)     |
| `eureka-server`    | 8761 | -           | Servis keşfi (Netflix Eureka)                                 |
| `gateway-server`   | 8080 | -           | API Gateway (Spring Cloud Gateway), tüm dış erişimin girişi   |
| `customer-service` | 8081 | `customerdb`| Müşteri, bireysel müşteri, adres, iletişim bilgisi            |
| `account-service`  | 8082 | `accountdb` | Fatura hesabı (Billing Account)                               |
| `product-service`  | 8084 | `productdb` | Katalog, teknik özellik, teklif, kampanya, satılan ürün        |
| `cart-service`     | 8085 | `cartdb`    | Sepet (Cart) ve sepet satırları                                |
| `order-service`    | 8086 | `orderdb`   | Sipariş (Order) — sepetten siparişe geçiş / checkout (FR-016) |
| `search-service`   | 8087 | `searchdb`  | Müşteri arama (FR-002) — CQRS read-model, olay tüketicisi     |

İş servisleri (`customer` / `account` / `product` / `cart` / `order`) hepsi aynı
**n-katmanlı** şablonu izler ve aralarındaki dağıtık işlemler **choreography Saga** ile
yürür (bkz. §6). `search-service` de aynı n-katmanlı şablonu izler ama saga'ya
katılmaz: yalnızca customer + account olay akışlarını tüketen bir **CQRS read-model**'dir
(bkz. §8.6).

## 2. Teknoloji Yığını (Tech Stack)

| Katman              | Teknoloji                          | Not                                    |
|---------------------|-------------------------------------|-----------------------------------------|
| Dil                 | Java                                 | 25                                       |
| Framework           | Spring Boot                          | 4.0.0 (parent), Spring Cloud 2025.1.0    |
| Build aracı         | Maven                                | 3.9+                                     |
| Veritabanı          | PostgreSQL                           | per-service DB, `wal_level=logical`      |
| Servis keşfi        | Netflix Eureka                       | -                                         |
| API Gateway         | Spring Cloud Gateway (WebFlux)       | -                                         |
| Merkezi config      | Spring Cloud Config (Git backend)    | bu repo, `main` dalı                     |
| Asenkron iletişim   | Kafka (local, KRaft, tek node)       | Spring Cloud Stream (Kafka binder)       |
| Güvenilir yayın     | Transactional Outbox + Debezium      | ghost event yok                          |
| Idempotent tüketim  | Inbox Pattern                        | duplicate consume yok                    |
| Dağıtık işlem       | Choreography Saga                    | merkezi orkestratör yok                  |
| Cache               | Redis (Spring Cache) + RedisInsight  | -                                         |
| DTO eşleme          | MapStruct                            | -                                         |
| API dokümantasyonu  | springdoc-openapi (Swagger UI)       | -                                         |
| Konfigürasyon       | YAML (`application.yml`)             | `properties` kullanılmaz                 |
| Paketleme           | JAR (alt servisler)                  | parent `pom`                             |

## 3. Maven Koordinatları

- **groupId:** `etiya.com`
- **artifactId:** `crm-lite` (parent)
- **version:** `1.0.0-SNAPSHOT`
- **packaging:** `pom` (parent), `jar` (alt servisler)

## 4. Parent POM Kararları

Kök dizindeki [pom.xml](../pom.xml) tüm servisler için ortak parent'tır.

**Alınan kararlar ve gerekçeleri:**

1. **`spring-boot-starter-parent`'tan miras alındı.**
   Spring Boot BOM'u (dependency management), plugin management ve varsayılan
   konfigürasyonlar tüm alt servislere otomatik iner. Sürüm çakışmaları önlenir.

2. **Parent packaging = `pom` (jar değil).**
   Maven kuralı gereği parent olarak kullanılan bir artifact `pom` paketlenmek
   zorundadır. `jar` paketlemesi **alt servisler** için geçerlidir.

3. **Konfigürasyon YAML üzerinden.**
   Servisler `application.yml` kullanır. `spring-boot-configuration-processor`
   parent'a eklendi (IDE'de yaml auto-complete/metadata desteği için).

4. **Merkezi versiyon yönetimi.**
   BOM dışındaki bağımlılıklar (`springdoc`, `mapstruct`) `properties` +
   `dependencyManagement` ile merkezileştirildi. Alt POM'lar versiyon yazmaz.
   `spring-cloud.version` (2025.1.0) her iş servisinin kendi POM'unda tanımlıdır
   (Spring Cloud BOM importu için) ve tüm servislerde **aynı** olmalıdır.

5. **Ortak bağımlılıklar (tüm servislerde):** `spring-boot-configuration-processor`
   (optional), `lombok` (optional), `spring-boot-starter-test` (test).

6. **Annotation processor sırası:** Lombok → MapStruct (compiler plugin'de
   `annotationProcessorPaths` ile sabitlendi).

### 4.1. Yeni servis ekleme prosedürü

1. Servis dizini oluşturulur (örn. `cart-service/`), n-katmanlı paket yapısı
   kurulur: `entities`, `dataAccess`, `business` (+ `rules`, `mappers`,
   `constants`, `dtos`, `messaging`), `apiController`, `core` (cross-cutting).
2. Servis POM'unda parent olarak `etiya.com:crm-lite:1.0.0-SNAPSHOT` gösterilir;
   `spring-cloud.version` property'si eklenir (bkz. örnek aşağıda).
3. Kök `pom.xml` içindeki `<modules>` bloğuna eklenir.
4. `configs/<service>/` altına 4 profil dosyası eklenir: `application.yml`
   (ortak), `application-dev.yml`, `application-docker.yml`, `application-prod.yml`.
   Servisin **yerel** `src/main/resources/application.yml`'i yalnızca bootstrap
   bilgisi (isim, aktif profil, config-server importu) taşır; `application-test.yml`
   ise hermetik testler için yerelde kalır (bkz. §5).
5. `configs/` altındaki yeni dosyalar **commit + push** edilmeden config-server
   bunları göremez (Git backend uzak repoyu okur — bkz. §5).
6. `gateway-server`'ın route listesine (`configs/gateway-server/application.yml`)
   yeni servisin path prefix'i eklenir.
7. Servisler arası dağıtık işlem gerekiyorsa **Saga** kurulur (bkz. §6); tek
   yönlü/lokal entegrasyon yeterliyse Outbox + Inbox ile asenkron olay akışı kurulur.
8. `infra/docker-compose.yml`'e servis bloğu (yorum satırı olarak, diğerleriyle
   aynı desende) ve gerekiyorsa `infra/postgres/init/01-create-databases.sql`'e
   yeni DB, `infra/debezium/register-<service>-connector.json`'a yeni connector
   eklenir (yalnızca servisin **kendi** outbox'ı için; saga kanalları genelde
   mevcut connector'lar üzerinden zaten yönlenir).

Örnek alt servis POM `<parent>` bloğu:

```xml
<parent>
    <groupId>etiya.com</groupId>
    <artifactId>crm-lite</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
<artifactId>cart-service</artifactId>
<packaging>jar</packaging>
```

## 5. Merkezi Konfigürasyon (Spring Cloud Config)

`config-server` modülü, tüm servislerin ortam bazlı konfigürasyonunu tek bir
kaynaktan sunar (`@EnableConfigServer`).

**Mimari kararlar ve gerekçeleri:**

1. **Git backend + tek repo.** Config server, bu reponun kendisini Git backend
   olarak kullanır (`https://github.com/safa-56/etiya-crm-lite-microservice.git`,
   dal: `main`). Konfigürasyonlar kök `configs/` klasöründe, servis adına göre
   klasörlenir: `configs/<service>/application-<profile>.yml`.

2. **`search-paths: configs/{application}`.** `{application}` placeholder'ı,
   istek yapan servisin `spring.application.name` değerine göre çözülür; config
   server yalnızca o servisin klasörüne bakar. Port: **8888**.

3. **Client bağlantısı `spring.config.import` ile.** Bootstrap context yerine
   (Spring Cloud 2020+ standardı) her servisin yerel `application.yml`'inde:
   `import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}`.
   `optional:` öneki, config server erişilemezse servisin yereldeki ayarlarla
   açılmasına izin verir (dayanıklılık) — ama bu durumda datasource gibi
   merkezde tanımlı ayarlar **eksik kalır** (bkz. tuzak notu aşağıda).

4. **Yerel vs. merkezi ayrımı.** Servislerin yerel `application.yml` dosyaları
   yalnızca **bootstrap** bilgisi tutar (isim, aktif profil, config importu).
   `dev/docker/prod` ayarları merkezi `configs/` altına taşındı. **`test`
   profili** ise hermetik (ağdan bağımsız) testler için her serviste **yerel**
   kalır (H2, Kafka/Eureka kapalı — bkz. §8).

5. **`config-server` kendi konfigürasyonunu yereldan yükler** (yumurta-tavuk
   problemi); başka bir config server'dan beslenmez.

6. **Servis açılış sırası:** config-server → eureka-server → gateway-server →
   iş servisleri. Bu nedenle `config-server`, kök POM `<modules>` listesinde
   ilk sıradadır.

**ÖNEMLİ / sık karşılaşılan tuzak:** Git backend uzak depoyu klonlar; `configs/`
altına yeni bir dosya eklendiğinde (ör. yeni bir servis için) bu dosyalar
config-server tarafından görülmeden önce **commit + push** edilmelidir. Aksi
halde servis `optional:` sayesinde yine de açılır ama datasource/Kafka/Redis gibi
merkezde tanımlı ayarları bulamaz ve şu hatayı verir: *"Failed to configure a
DataSource: 'url' attribute is not specified..."*. Çözüm: `configs/<service>/*`
dosyalarını `main`'e push et, servisi yeniden başlat. Private repo için
`CONFIG_GIT_USERNAME` / `CONFIG_GIT_PASSWORD` ortam değişkenleri kullanılır.

## 6. İş Servisi Şablonu (n-layered)

Tüm iş servisleri (`customer`, `account`, `product`, `cart`, `order`) **aynı** n-katmanlı
şablonu izler. İlk kurulan `customer-service` referans alınarak sonrakiler
birebir aynı desenle inşa edildi.

**Katmanlar (paketler):**
- **entities** — JPA entity'leri + `entities/enums`, `entities/outbox`, `entities/inbox`
- **dataAccess** — Spring Data JPA repository'leri
- **business** — `business/rules` (iş kuralı sınıfları), `business/mappers`
  (MapStruct), `business/constants` (`Messages`, olay sabitleri), `business/dtos`
  (`requests`, `responses`, `events`), `business/messaging` (Kafka consumer
  binding'leri), `business/abstracts` (servis arayüzleri), `business/concretes`
  (manager implementasyonları)
- **apiController** — REST uçları
- **core** — cross-cutting: `core/config` (Redis, OpenAPI), `core/constants`
  (`CacheNames`), `core/crosscutting/exceptions` (`BusinessException`,
  `GlobalExceptionHandler`)

**Ortak mimari kararlar (tüm iş servislerinde aynı):**

1. **Veritabanı: PostgreSQL (per-service DB).** Her servis kendi şemasını yönetir
   ve kendi Debezium replication slot'una sahiptir. Debezium CDC için
   `wal_level=logical` gerekir (bkz. `infra/docker-compose.yml`).

2. **`BaseEntity` (@MappedSuperclass).** Ortak alanlar `id, created_date,
   updated_date, deleted_date, is_active` burada; tüm entity'ler miras alır.
   Soft-delete `is_active` + `deleted_date` ile yapılır. Zaman damgaları
   `@PrePersist`/`@PreUpdate` ile otomatik atanır. `id` stratejisi `IDENTITY`.

3. **Asenkron iletişim: Kafka (local container) + Transactional Outbox + Debezium.**
   Manager, iş verisi ile `outbox_events` kaydını **aynı transaction**'da yazar
   (ghost event yok). Debezium (Kafka Connect) outbox tablosunu izleyip
   `EventRouter` SMT ile `crm.<aggregateType>.events` topic'ine yayınlar.
   Broker **local container** (KRaft, Zookeeper'sız, tek node); uygulama tüketici
   tarafında **Spring Cloud Stream (Kafka binder)** fonksiyonel binding kullanır
   (`<isim>Consumer-in-0`, `java.util.function.Consumer<Message<String>>` bean'i).
   Tüketiciler yalnızca `app.kafka.enabled=true` iken devreye girer (test
   profilinde `false` — hermetik test). Kafka Cloud kullanılmaz.

4. **Duplicate consume: Inbox Pattern.** `inbox_messages` tablosu (PK
   `message_id`) + `InboxService.process(messageId, eventType, handler)` ile
   idempotent tüketim. `messageId`, olay Kafka anahtarına + payload hash'ine göre
   üretilir (aynı agregada ardışık farklı olayların ayrı işlenmesi, gerçek
   tekrarların atlanması için).

5. **Cacheleme: Redis (Spring Cache).** `RedisCacheConfig` ile JSON serileştirme
   (`GenericJacksonJsonRedisSerializer`, tip bilgisiyle) ve cache bazlı TTL
   (`CacheNames` sabitleri, tekil kayıt ~5-10 dk, liste ~2 dk). `@Cacheable` /
   `@CacheEvict` / `@CachePut` / `@Caching` manager metodlarında kullanılır.
   Cache'ler **RedisInsight** (`localhost:5540`) ile izlenir.

6. **İş kuralları `business/rules` altında** ayrı sınıfta (`@Service`) toplanır
   ve ilgili manager'a **constructor injection** ile enjekte edilir. Kural
   ihlalinde `BusinessException` fırlatılır. **Mesajlar** `business/constants/
   Messages` sabitlerinden gelir — magic string kullanılmaz. İstek/yanıt için
   **DTO** (Java `record`) + **MapStruct** mapper (`componentModel = "spring"`,
   `unmappedTargetPolicy = ReportingPolicy.IGNORE`) kullanılır. Sistem tarafından
   atanan / türetilen alanlar mapper'da `ignore = true` ile atlanıp manager'da set
   edilir.

7. **Hata yönetimi:** `core` altında merkezi `@RestControllerAdvice`
   (`GlobalExceptionHandler`) RFC 7807 `ProblemDetail` formatında yanıt döner:
   `BusinessException` → 400, `MethodArgumentNotValidException` (`@Valid`) → 400
   (alan bazlı detay), beklenmeyen `Exception` → 500 (kök neden istemciye
   sızdırılmaz, sunucuda loglanır).

8. **Sayfalama:** Liste uçları `Pageable` alır, `PagedResponse<T>` (content +
   pageNumber/pageSize/totalElements/totalPages/last) döner.

9. **Soft-delete deseni:** Silme fiziksel değildir; `isActive=false` +
   `deletedDate=now()` set edilip kaydedilir. Repository'ler yalnızca aktif
   kayıtları döndüren türetilmiş sorgular sağlar (`findByIdAndIsActiveTrue`,
   `existsBy...AndIsActiveTrue` vb.).

**Altyapı:** Kök `infra/docker-compose.yml` → PostgreSQL, Redis, RedisInsight,
Kafka (KRaft), Kafka UI, Debezium (Kafka Connect). Debezium outbox connector
tanımları: `infra/debezium/register-<service>-connector.json`. Bkz.
`infra/README.md`.

## 7. Choreography Saga Pattern

Servisler arası **dağıtık işlemler** (bir serviste yazma kararının başka bir
servisin otoriter verisine bağlı olduğu durumlar) merkezi orkestratör olmadan,
**choreography-based Saga** ile yürütülür: her servis olayları dinler, kendi
adımını yapar, doğrulama başarısızsa **telafi (compensation)** eder. Dört saga
mevcuttur; hepsi aynı deseni izler ve mevcut **Outbox + Debezium + Inbox**
altyapısı üzerine kuruludur.

**Ortak desen:**
- **Tek kanal / tek topic.** Aggregate tipi `<Saga>` olan tüm outbox kayıtları —
  hangi servisin DB'sinden gelirse gelsin — Debezium EventRouter ile aynı
  topic'e (`crm.<Saga>.events`) yönlenir. **Yeni Debezium connector genelde
  gerekmez**: her servisin kendi outbox connector'ı zaten `aggregate_type`'a
  göre dinamik yönlendirme yapar (`route.topic.replacement:
  crm.${routedByValue}.events`).
- **Self-consume önleme.** Her iki taraf da aynı topic'i dinler; payload
  içindeki `eventType` alanına bakarak yalnızca kendini ilgilendiren olayları
  işler, kendi ürettiği olayları atlar (döngü yok).
- **İdempotency.** Inbox Pattern + **durum bazlı yönlendirme**: sonuç yalnızca
  agrega hâlâ `PENDING` durumundaysa uygulanır (aksi hâlde saga zaten tamamlanmış
  demektir, sonuç atlanır).
- **Asenkron tamamlanma.** Başlatan endpoint hemen döner (agrega `PENDING`
  durumda görünür); sonuç saga ile asenkron gelir.

### 7.1. Billing Account Saga — account-service ↔ customer-service

**Kanal:** `crm.BillingAccountSaga.events`.

customer-service **doğrulayıcı (participant)** rolündedir: gelen isteklerde
(`...CreationRequested` / `...AddressChangeRequested`) müşteri + adresi **kendi
DB'sinden otoriter** doğrular → `...CustomerValidated` (adres snapshot'ıyla) ya
da `...CustomerValidationFailed` (neden ile) yayınlar. account-service sonucu
hesabın durumuna göre yönlendirir. **CQRS read-model (müşteri projeksiyonu)
kaldırıldı**; tüm cross-service yazma kararları saga'dan geçer.

**Akış 1 — Create Billing Account:**
1. account-service hesabı `PENDING` açar (adres boş) → `...CreationRequested`.
2. customer-service doğrular → Validated/Failed.
3. account-service: Validated → `ACTIVE` (otoriter adres yazılır); Failed →
   `CANCELLED` + soft-delete (**telafi**).

**Akış 2 — Update Billing Account Address:**
1. account-service adres-dışı alanları senkron günceller; adres değiştiyse yeni
   adresi `pendingAddressId`'de tutar (hesap ACTIVE kalır) → `...AddressChangeRequested`.
2. customer-service (aynı doğrulama) → Validated/Failed.
3. account-service: Validated → yeni adresi uygular (`addressId` + metin),
   beklemeyi temizler; Failed → beklemeyi temizler, **eski adres korunur** (telafi).

**Durumlar:** `AccountStatus` = `PENDING → ACTIVE | CANCELLED` (+ `PASSIVE`
silmede). Idempotency: Inbox + hesap durumuna göre yönlendirme (PENDING →
create; `pendingAddressId` → update; ikisi de yoksa atla).

### 7.2. Product Sale Saga — product-service ↔ account-service

**Kanal:** `crm.ProductSaga.events`.

product-service **başlatıcı**, account-service **doğrulayıcı**dır: yeni satılan
ürün için fatura hesabının var ve `ACTIVE` olduğu account-service'ten otoriter
doğrulanır.

**Akış:**
1. product-service ürünü `PENDING` açar → `ProductSaleRequested`
   (`ProductSagaRequestedPayload{productId, billingAccountId}`).
2. account-service: hesap aktif + `ACTIVE` mi? → `ProductAccountValidated` /
   `ProductAccountValidationFailed` (neden: `SAGA_BILLING_ACCOUNT_NOT_FOUND` /
   `SAGA_BILLING_ACCOUNT_NOT_ACTIVE`).
3. product-service: Validated → ürün `ACTIVE` olur ve **ayrıca**
   `crm.Product.events`'e `ProductCreated` yayınlanır (account-service bunu
   dinleyip `active_product_count` sayacını artırır — bu ayrı, saga-dışı bir
   olay akışıdır); Failed → ürün `CANCELLED` + soft-delete (**telafi**).

Idempotency: yalnızca `PENDING` durumundaki ürünler sonuçla ileri
götürülür/telafi edilir (`ProductSagaManager.applyValidationResult`).

### 7.3. Cart Item Saga — cart-service ↔ product-service

**Kanal:** `crm.CartSaga.events`.

cart-service **başlatıcı**, product-service **doğrulayıcı**dır. Sepete
eklenen bir teklif ya da kampanyanın var/aktif olduğu ve güncel fiyatı,
cart-service tarafından **senkron sorulmaz ve yerel bir read-model
(projeksiyon) olarak tutulmaz** — bu, ilk taslakta denenip saga modeline uygun
olmadığı için kaldırılan bir yaklaşımdı (bkz. §11 değişiklik günlüğü). Bunun
yerine tamamen saga üzerinden, olay-tabanlı doğrulanır.

**Akış:**
1. cart-service sepet satırını `PENDING` açar (ad/fiyat henüz boş) →
   `CartItemValidationRequested`
   (`CartItemSagaRequestedPayload{cartItemId, itemType, productOfferId|campaignId}`).
2. product-service (`CartSagaParticipantManager`), satır türüne göre kendi
   otoriter DB'sinden doğrular:
   - **OFFER** → teklif aktif mi? Doğruysa ad + liste fiyatı.
   - **CAMPAIGN** → kampanya aktif mi? Doğruysa ad + paket fiyatı (`campaignPrice`)
     + paket içeriği (aktif `CampaignOffer` satırlarından türetilen teklif listesi).
   → `CartItemValidated` (`name`, `unitPrice`, `offers[]`) ya da
   `CartItemValidationFailed` (neden: `SAGA_CART_PRODUCT_OFFER_NOT_FOUND` /
   `SAGA_CART_CAMPAIGN_NOT_FOUND` / `SAGA_CART_ITEM_TYPE_UNKNOWN`).
3. cart-service (`CartSagaManager`): Validated → satır `ACTIVE`, `name`/
   `unitPrice` snapshot'ı yazılır; CAMPAIGN ise paket içeriği `CartItemLine`
   satırlarına (silinip yeniden) yazılır. Failed → satır `CANCELLED` + soft-delete
   (**telafi**).

**Durumlar:** `CartItemStatus` = `PENDING → ACTIVE | CANCELLED`. Sepet toplamı
yalnızca `ACTIVE` satırların Σ(`unitPrice × quantity`)'idir (PENDING satırlar
toplama 0 katkı verir). Idempotency: yalnızca `PENDING` satırlar sonuçla ileri
götürülür/telafi edilir.

### 7.4. Order Checkout Saga — order-service ↔ cart-service

**Kanal:** `crm.OrderCheckoutSaga.events`.

order-service **başlatıcı**, cart-service **doğrulayıcı**dır. Bu saga, FR-016
"Siparişin Tamamlanması" (Submit Order) akışını karşılar: kullanıcı bir sepeti
onayladığında sipariş oluşturulur, ama sepetin otoriter içeriği (satırlar, fiyat,
toplam, sahiplik) cart-service'ten **senkron sorulmaz** — saga ile alınır.

**Akış:**
1. order-service siparişi `PENDING` açar: sistem tarafından üretilen benzersiz bir
   sipariş numarası (`orderNumber` = `ORD-XXXXXXXX`) ve FR-015'te seçilen servis
   adresi (`serviceAddress` metin snapshot'ı) yazılır; satırlar/toplam henüz boştur →
   `OrderCheckoutRequested` (`OrderCheckoutRequestedPayload{orderId, cartId}`).
2. cart-service (`OrderCheckoutParticipantManager`) sepeti kendi otoriter DB'sinden
   doğrular: sepet var/aktif mi ve **en az bir `ACTIVE` (onaylanmış) satırı** var mı?
   → `OrderCartValidated` (`customerId`, `accountId`, `totalAmount`, `items[]` — her
   kalemin türü/ad/fiyat/adet snapshot'ı) ya da `OrderCartValidationFailed` (neden:
   `SAGA_CART_NOT_FOUND` / `SAGA_CART_EMPTY`).
3. order-service (`OrderSagaManager`): Validated → sipariş `CONFIRMED` olur; sahiplik,
   toplam ve kalemler `OrderItem` satırlarına snapshot'lanır. Failed → sipariş
   `CANCELLED` + soft-delete (**telafi**).

**Durumlar:** `OrderStatus` = `PENDING → CONFIRMED | CANCELLED`. Idempotency: yalnızca
`PENDING` siparişler sonuçla ileri götürülür/telafi edilir (Inbox + durum bazlı
yönlendirme). **Yeni Debezium connector** order-service'in kendi outbox'ı için gerekir
(`register-order-connector.json`); saga sonuç kayıtları (cart-service'ten) mevcut cart
connector'ı üzerinden aynı topic'e yönlenir. cart-service artık **iki** consumer taşır:
`cartSagaConsumer` (sepete ekleme saga'sının sonuç tüketicisi) + `orderCheckoutRequestConsumer`
(sipariş saga'sının istek/doğrulayıcı tüketicisi).

### 7.5. Order → Product Provizyonu — order-service ↔ product-service

**Kanal:** `crm.Order.events` (saga-dışı entegrasyon olayı).

Order ile Product **ayrı domainlerdir**; sipariş oluşturmak tek başına product-service'te
`Product` üretmez. Bu adım o boşluğu kapatır: sipariş CONFIRMED olduğunda kalemleri
otomatik olarak ürünlere provizyone edilir; böylece fatura hesabının
`activeProductCount` sayacı artar ve ürünler hesap detayında (FR-013) görünür.

**Akış:**
1. order-service (`OrderSagaManager.confirm`): sipariş CONFIRMED olduktan sonra aynı
   (Inbox) transaction'ında outbox'a `OrderConfirmed`
   (`OrderConfirmedPayload{orderId, accountId, addressId, items[]}`) yazar. `items[]`
   her kalemin türü (OFFER/CAMPAIGN), teklif/kampanya kimliği, ad, fiyat ve adet
   snapshot'ını taşır.
2. product-service (`OrderProvisioningConsumerConfig` → `ProductProvisioningManager`):
   olayı Inbox ile idempotent tüketir ve her kalemi ürüne dönüştürür:
   - **OFFER** → teklif kimliğinden **tek** `Product` (ödenen fiyat = sipariş snapshot'ı).
   - **CAMPAIGN** → kampanyanın paket içeriği (aktif `CampaignOffer` bağları)
     product-service'in **otoriter** DB'sinden çözülür; her teklif için bir `Product`
     üretilir (kampanya bağı `campaign_id` ile korunur, ödenen fiyat = teklifin liste
     fiyatı). Sipariş yalnızca `campaignId` taşır; paket açılımı burada yapılır.
3. Üretilen her ürün `PENDING` açılır ve **mevcut Product Sale Saga'sı** (§7.2,
   `crm.ProductSaga.events`) ile fatura hesabına karşı doğrulanır — REST üzerinden ürün
   ekleme (`ProductManager.add`) ile birebir aynı adım. Onayda ürün ACTIVE olur ve
   `crm.Product.events`'e `ProductCreated` yayınlanır → account-service sayacı artırır.

**Idempotency:** Sipariş olayı `messageId` üzerinden Inbox ile tekilleştirilir (aynı
sipariş olayı tekrar gelse ürünler bir kez üretilir). Ürün üretimi + saga istekleri
(outbox) + inbox kaydı **aynı transaction**'da atomik commit edilir. Ek Debezium
connector gerekmez: order-service'in mevcut connector'ı (`register-order-connector.json`)
`aggregate_type=Order` kayıtlarını EventRouter ile `crm.Order.events`'e yönlendirir.
product-service artık **üç** consumer taşır: `productSagaConsumer` + `cartSagaConsumer`
+ `orderProvisioningConsumer`.

## 8. Servis Bazlı Notlar

### 8.1. customer-service (ilk iş servisi, port 8081, DB `customerdb`)

- **Kalıtım = JPA `JOINED`.** ER modeli birebir uygulandı: `Customer` (kök,
  `customers`) ← `IndividualCustomer` (`individual_customers`,
  `@PrimaryKeyJoinColumn(customer_id)`; 1-1 paylaşılan PK). `Address` ve
  `CustomerContactInfo` müşteriyle N-1 (`customer_id` FK).
- Billing Account Saga'sında **doğrulayıcı** roldedir (bkz. §7.1).
- **Party modeli.** Müşteri doğrudan değil, `Party → PartyRole → Customer` zinciri
  üzerinden modellenir (legacy SID hizası). Durum/tip bilgisi ayrı kolonlarda değil
  `general_status` / `general_type` referans tablolarına FK ile bağlanır; her servis
  bu tabloların yalnızca kendi `entity_code_name` dilimine sahiptir
  (`PartyReferenceCodes`).
- **Sistem kullanıcısı (`SystemUser`).** Keycloak kullanıcısının domain karşılığıdır ve
  `Customer` ile **aynı deseni** izler: bir `PartyRole`'e 1-1 bağlanır, farkı yalnızca rol
  tipidir (`CUST` vs. `USER`). Kullanıcı burada **oluşturulmaz** — otoritesi Keycloak'tır;
  kayıt yalnızca `keycloak_user_id` (JWT `sub`) ile referans verir, parola/rol kopyalanmaz.
  Zincir kullanıcının **ilk isteğinde** kurulur (lazy provisioning,
  `SystemUserProvisioningFilter` → `SystemUserManager` → `SystemUserProvisioner`); okuma
  yolu JVM içi bir küme + tek indeksli `exists` sorgusuyla ucuz tutulur, yazma yolu
  party/rol/kullanıcıyı **tek transaction'da** commit eder. Aynı `Party` her iki rolü de
  taşıyabildiği için (hem müşterimiz hem kullanıcımız olan kişi) `PartyRoleManager.deactivate`
  party'yi ancak **son aktif rol** düştüğünde pasifleştirir.
- `Gender`/`Nationality` şimdilik ham referans (`gender_id`/`nationality_id`);
  ilgili lookup servis/tablo eklenince FK'ye bağlanacak (açık madde, bkz. §10).

### 8.2. account-service (port 8082, DB `accountdb`)

- Fatura hesabı (`BillingAccount`): `AccountStatus` (`PENDING/ACTIVE/CANCELLED/
  PASSIVE`), `AccountType`, `activeProductCount` (Product olaylarından
  güncellenen sayaç — `crm.Product.events` tüketicisi, saga-dışı).
- Billing Account Saga'sında **başlatıcı**, Product Sale Saga'sında
  **doğrulayıcı** roldedir (bkz. §7.1, §7.2).
- Silme kuralı: `activeProductCount > 0` olan hesap silinemez (iş hatası,
  birebir sabit mesaj: *"The billing account cannot be deleted because it has
  active products."*).

### 8.3. product-service (port 8084, DB `productdb`)

**Katalog (kategori) vs. Kampanya (paket) modeli** — Teklif Seçimi
(FR-014 / UC-EACRML-014) analizindeki iki farklı kavram, domain'de **bilinçli
olarak farklı** modellenir:

1. **Katalog = zorunlu kategori (1-N).** Her `ProductOffer` **tam olarak bir**
   `Catalog`'a aittir (`catalog_id` NOT NULL, `@ManyToOne`). Katalog bir
   kategoridir (Ev İnterneti, Mobil, Superbox, TV, Sabit Hat). Teklif
   oluştururken `catalogId` zorunludur. Catalog sekmesi araması:
   `GET /product-offers?catalogId=`.

2. **Kampanya = opsiyonel paket/bundle (N-N).** Bir `Campaign` birden çok
   teklifi `CampaignOffer` (N-N) ile paketler; bir teklif birden çok kampanyada
   olabilir ve kampanyaya girmesi **zorunlu değildir**. Kampanya, tek
   `campaignPrice` ile sepete **bir bütün olarak** eklenir. Kampanya, içindeki
   teklifleriyle birlikte tek çağrıda kurulur: `POST /campaigns` gövdesi
   `{ name, campaignPrice, offerIds[] }`. `CampaignResponse` paketi + türetilmiş
   fiyatları döner: `listPriceTotal` (Σ liste), `savings` (indirim =
   listPriceTotal − campaignPrice) ve `offers[]` (`{offerId, offerName,
   listPrice}`).

   **Kural notu:** "kampanya fiyatı < liste toplamı" iş kuralı olarak
   **zorlanmaz** (yalnızca `savings` hesaplanıp gösterilir); zorlanan tek kural,
   paketin en az bir **var olan/aktif** teklif içermesi ve aynı teklifin
   pakette tekrarlanmamasıdır.

**Satış (Product):** `Product`, satılan teklifin son hâlidir; opsiyonel
`campaign_id` ile hangi paketten geldiğini taşır (`price_to_be_paid` = ödenen
nihai fiyat). Product Sale Saga'sında **başlatıcı**, Cart Item Saga'sında
**doğrulayıcı** roldedir (bkz. §7.2, §7.3).

### 8.4. cart-service (port 8085, DB `cartdb`)

`Carts` / `CartItems` ER modelini karşılar; şablonu §6'daki ortak desenle
aynıdır (yalnızca cache/Kafka/DTO/rules deseni değil, **saga başlatıcılığı**
da dahil).

- **Model.** `Cart` (`carts`): `customerId` + `accountId` **ham referans**
  (per-service DB gereği FK değil). `CartItem` (`cart_items`): `itemType`
  (OFFER | CAMPAIGN) + `status` (PENDING | ACTIVE | CANCELLED);
  `productOfferId` **ya da** `campaignId` dolu olur. `name` + `unitPrice`
  **Saga doğrulamasıyla** yazılan snapshot'tır (PENDING iken boştur). CAMPAIGN
  satırının paket içeriği `CartItemLine` (`cart_item_lines`) snapshot
  satırlarında tutulur. Silme soft-delete; sepet toplamı yalnızca **ACTIVE**
  satırların Σ(`unitPrice × quantity`)'idir.

- **Sepete ekleme iki yol (FR-014).** (a) **addOffer** — katalogdan doğrudan
  tek teklif (`POST /carts/{id}/items/offers`). (b) **addCampaign** — içinde
  birden çok teklif olan kampanya, **tek paket fiyatıyla bir bütün olarak** tek
  satır hâlinde (`POST /carts/{id}/items/campaigns`); aynı kampanya iki kez
  eklenemez (sepet-lokal kural — `CartItemBusinessRules`). Kampanya satırı
  yanıtta paket içeriğiyle gösterilir.

- **Cross-service doğrulama = Cart Item Saga (bkz. §7.3).** Ekleme **asenkron**
  tamamlanır (endpoint hemen döner, satır kısa süre `PENDING` görünür).
- **Checkout = Order Checkout Saga (bkz. §7.4).** Sepetin siparişe dönüşü artık
  order-service ile ele alınır; cart-service bu saga'da **doğrulayıcı** roldedir
  (`OrderCheckoutParticipantManager`): sepetin var/aktif ve **en az bir `ACTIVE`
  satırı** olduğunu otoriter kontrol edip satır/toplam/sahiplik snapshot'ını yayınlar.

### 8.5. order-service (port 8086, DB `orderdb`)

FR-016 "Siparişin Tamamlanması" (Submit Order) akışını karşılar; şablonu §6'daki
ortak desenle aynıdır (**saga başlatıcılığı** dahil).

- **Model.** `Order` (`orders`): `orderNumber` (sistem üretimi benzersiz sipariş
  kimliği, `ORD-XXXXXXXX`), `cartId`, `customerId`/`accountId` (**ham referans**,
  saga ile dolar), `serviceAddressId` + `serviceAddress` (FR-015'te seçilen servis
  adresi metin snapshot'ı), `status` (PENDING | CONFIRMED | CANCELLED), `totalAmount`
  (saga ile dolar). `OrderItem` (`order_items`): sipariş anındaki sepet kaleminin
  snapshot'ı (`itemType`, `productOfferId`/`campaignId`, `name`, `unitPrice`,
  `quantity`) — sepet sonradan değişse bile sipariş kaydı sabit kalır. Silme
  soft-delete; sipariş toplamı otoriter olarak saga sonucundan (`totalAmount`) gelir.

- **Submit Order (FR-016).** `POST /orders` gövdesi
  `{cartId, serviceAddressId?, serviceAddress}`. order-service siparişi `PENDING`
  açıp benzersiz `orderNumber` üretir ve cart-service'ten doğrulama ister; ekleme
  **asenkron** tamamlanır (endpoint hemen döner, sipariş kısa süre `PENDING` görünür).
  Onayda sipariş `CONFIRMED` olur (kalemler + toplam snapshot'lanır), sepet yoksa/boşsa
  telafi ile `CANCELLED`. Aynı sepet için hâlâ süren bir sipariş varsa yeni submit
  iş kuralıyla engellenir (`OrderBusinessRules.checkIfCartNotAlreadyOrdered`).

- **Cross-service doğrulama = Order Checkout Saga (bkz. §7.4).** order-service
  cart-service'e senkron çağrı yapmaz / yerel projeksiyon tutmaz.

### 8.6. search-service (port 8087, DB `searchdb`)

FR-002 "Müşteri Arama ve Görüntüleme" ekranını karşılayan bir **CQRS read-model**'dir.
Yazma tarafına (müşteri/hesap CRUD mantığına) dokunmaz; mevcut Kafka olay akışlarını
tüketip tek bir denormalize **arama indeksi** tutar ve tek bir uçtan sorgular. Şablonu
§6'daki ortak n-katmanlı desenle aynıdır (BaseEntity, Inbox, Redis, DTO+MapStruct,
`GlobalExceptionHandler`, `PagedResponse`) ama **kendi outbox'ı/Debezium connector'ı
yoktur** (yalnızca tüketici) ve **saga'ya katılmaz**.

- **Model.** `CustomerSearchIndex` (`customer_search_index`): tek satır = bir müşteri;
  `customerId` (unique iş anahtarı), `firstName`, `secondName`, `lastName`,
  `nationalityId` (TCKN), `gsmNumber`, `role` (`CustomerRole` = B2C | B2B; şimdilik hep
  B2C). Bir müşterinin birden çok fatura hesabı olabildiğinden `accountNumbers` ve
  `orderNumbers` ayrı koleksiyon tablolarında (`search_account_numbers`,
  `search_order_numbers`, `@ElementCollection`) tutulur. Arama sütunları indekslidir.

- **İki consumer (Inbox ile idempotent).** (1) `customerEventConsumer` →
  `crm.Customer.events`: create/update satırı `customerId`'ye göre **null-safe upsert**
  eder (adres-only snapshot'ta boş gelen alanlar mevcut değeri korur), delete satırı
  kaldırır. (2) `billingAccountEventConsumer` → `crm.Account.events`: hesap `ACTIVE`
  olduğunda account/order numaralarını ilgili müşteri satırına ekler, `CANCELLED`/
  `PASSIVE` olduğunda çıkarır. **Sıra bağımsızdır:** account olayı customer satırı
  oluşmadan gelirse `customerId` ile bir **stub satır** oluşturulur; customer olayı
  sonradan gelince isim/TCKN/GSM doldurulur.

- **Sorgu (FR-002).** `GET /api/v1/search/customers` — dinamik **JPA Specification**
  (Criteria API): `customerId`/`idNumber`(TCKN)/`accountNumber`/`gsm`/`orderNumber`
  **tam eşleşme** (ACC-14), `firstName`/`lastName` **starts-with case-insensitive**
  (ACC-17), isim bloğu içinde **AND** (ACC-15), isim bloğu ile diğer kriterler arasında
  **OR** (ACC-16). Koleksiyon aramaları OR-güvenli EXISTS alt sorgusuyla yapılır. İlk
  sayfa 50 kayıt (ACC-19), varsayılan sıralama `customerId` artan (ACC-20), sonuç
  kolonları Customer ID / First / Second / Last Name / Role / ID Number (ACC-21).
  Backend format doğrulaması (ACC-04..10) `business/rules` + `GlobalExceptionHandler`
  ile 400 döner. Liste sorgusu Redis'te kısa süreli (~2 dk) cache'lenir; indeks
  değişince cache boşaltılır.

- **Ön koşul (event genişletmesi).** Search index'in ihtiyaç duyduğu alanlar için iki
  olay sözleşmesi genişletildi: `CustomerEventPayload`'a `secondName`, `nationalityId`,
  `gsmNumber` (müşterinin birincil/ilk aktif iletişim bilgisindeki GSM), `role` (sabit
  "B2C"); `BillingAccountEventPayload`'a `accountNumber`, `orderNumber`. Geriye
  uyumludur (yeni alan ekleme).

### 8.7. REST API Haritası (gateway üzerinden)

Tüm dış erişim `gateway-server` (`localhost:8080`) üzerindendir; route'lar
`configs/gateway-server/application.yml`'de açık (explicit) tanımlıdır
(`StripPrefix=1`). Her servisin Swagger UI'si de ayağa kalktığında
`/<servis>/swagger-ui.html` altından erişilebilir.

| Servis   | Base path                        | Uçlar (özet)                                                                 |
|----------|----------------------------------|------------------------------------------------------------------------------|
| customer | `/api/v1/individual-customers`   | CRUD (POST, GET/{id}, GET list, PUT/{id}, DELETE/{id})                        |
| customer | `/api/v1/addresses`              | CRUD                                                                          |
| customer | `/api/v1/contact-infos`          | CRUD                                                                          |
| account  | `/api/v1/billing-accounts`       | CRUD (create + adres-update **asenkron** saga ile tamamlanır)                 |
| product  | `/api/v1/product-specs`          | CRUD                                                                          |
| product  | `/api/v1/catalogs`               | CRUD                                                                          |
| product  | `/api/v1/product-offers`         | CRUD + `?catalogId=` ile kategori araması                                     |
| product  | `/api/v1/campaigns`              | CRUD; `POST` gövdesi `{name, campaignPrice, offerIds[]}`                      |
| product  | `/api/v1/products`               | create (**asenkron** saga), GET/{id}, GET list, `GET /details`, DELETE       |
| cart     | `/api/v1/carts`                  | `POST` (create), `GET/{id}`, `GET` list, `GET /customer/{customerId}`, `DELETE/{id}` |
| cart     | `/api/v1/carts/{id}/items/offers`    | `POST` — katalogdan teklif ekle (**asenkron** saga)                      |
| cart     | `/api/v1/carts/{id}/items/campaigns` | `POST` — kampanya paketi ekle (**asenkron** saga)                       |
| cart     | `/api/v1/carts/{cartId}/items/{itemId}` | `DELETE` — satırı çıkar                                                |
| order    | `/api/v1/orders`                 | `POST` (submit, **asenkron** saga), `GET/{id}`, `GET` list, `GET /customer/{customerId}`, `DELETE/{id}` |
| search   | `/api/v1/search/customers`       | `GET` — FR-002 müşteri arama (dinamik Specification, sayfalı; olay tüketicili read-model) |

### 8.8. Altyapı Hızlı Referansı (infra)

Kök `infra/docker-compose.yml` ile ayağa kalkan bileşenler (ayrıntı:
`infra/README.md`):

| Bileşen        | Adres                    | Not                                              |
|----------------|--------------------------|--------------------------------------------------|
| PostgreSQL     | `localhost:5432`         | `customerdb`, `accountdb`, `productdb`, `cartdb`, `orderdb`, `searchdb` |
| Redis          | `localhost:6379`         | cache                                            |
| RedisInsight   | http://localhost:5540    | Redis host olarak `redis:6379` ekle              |
| Kafka          | `localhost:9092` (host)  | konteyner içi: `kafka:29092` (KRaft, tek node)   |
| Kafka UI       | http://localhost:8090    | topic/mesaj/consumer group izleme                |
| Kafka Connect  | http://localhost:8083    | Debezium REST API                                |

**Debezium connector'ları** (`infra/debezium/register-<service>-connector.json`,
her servis kendi outbox'ı için ilk açılışta bir kez kaydedilir):
`register-outbox-connector.json` (customerdb), `register-account-connector.json`
(accountdb), `register-product-connector.json` (productdb),
`register-cart-connector.json` (cartdb), `register-order-connector.json` (orderdb).
Saga kanalları (`crm.*Saga.events`) bu mevcut connector'lar üzerinden
`aggregate_type`'a göre yönlenir; ayrı connector gerekmez (bkz. §7).
**`search-service` için connector yoktur** (kendi outbox'ı olmayan salt tüketici);
mevcut customer + account connector'larının yaydığı `crm.Customer.events` /
`crm.Account.events` topic'lerini dinler.

**cartdb / searchdb notu:** Postgres init betiği (`infra/postgres/init/01-create-databases.sql`)
yalnızca **boş volume'de ilk açılışta** çalışır. Var olan bir kurulumda yeni DB
elle oluşturulur: `docker exec -it crm-postgres psql -U postgres -c "CREATE
DATABASE searchdb;"`.

## 9. Test Seed Verisi

İki yol var (ayrıntı ve senaryo tablosu: `infra/seed/README.md`):

1. **Otomatik seed (dev profili).** Her serviste `src/main/resources/data.sql`
   vardır; servis `dev` profilinde açıldığında Hibernate şemayı kurduktan sonra
   Spring bu betiği çalıştırıp DB'yi **idempotent** (`ON CONFLICT (id) DO
   NOTHING`) tohumlar. Aktifleştiren ayarlar servisin yerel `application.yml`'
   indeki dev dökümanında: `spring.sql.init.mode=always` +
   `spring.jpa.defer-datasource-initialization=true` (**prod/test almaz**).
   Böylece repoyu pull edip dev'de çalıştıran herkeste veri kendiliğinden oluşur.
2. **Manuel tam sıfırlama.** `infra/seed/*.sql` (+ `run-seed.sh`/`run-seed.ps1`)
   `TRUNCATE ... RESTART IDENTITY CASCADE` ile DB'yi baştan temiz seed'e
   döndürür. (Not: cart-service henüz bu manuel seed setine dahil değil —
   sepet satırları saga ile API üzerinden oluşturulduğundan yalnızca boş bir
   örnek sepet `data.sql` ile tohumlanır.)

**Not (şema sıfırlama tuzağı):** DBeaver'da veriyi silmek için `public` şemasını
DROP ETME; Hibernate tabloları `public`'e kurduğundan şema yoksa `ddl-auto`
sessizce hiçbir tablo oluşturmaz (uygulama yine "UP" görünür). Sıfırlamak için
tabloları `TRUNCATE`/`DROP` et ya da şemayı silersen `CREATE SCHEMA public;`
ile geri aç ve servisi yeniden başlat.

**Not (hermetik test profili tuzağı):** `application-test.yml`'de
`defer-datasource-initialization` yalnızca `dev` profilinde açık olduğundan,
eğer bir serviste `spring.sql.init.mode` test profilinde de etkinse `data.sql`
Hibernate şemayı kurmadan önce çalışıp "tablo yok" hatası verebilir. cart-service
bunun için `application-test.yml`'de `spring.sql.init.mode: never` ile bunu
açıkça kapatır.

## 10. Açık Sorular / Yapılacaklar

- [x] Servis keşfi: **Netflix Eureka** (`eureka-server`) + API Gateway
  (`gateway-server`, Spring Cloud Gateway).
- [x] Merkezi konfigürasyon: **Spring Cloud Config** (`config-server`), Git
  backend + kök `configs/` klasörü. Bkz. §5.
- [x] Veritabanı teknolojisi: **PostgreSQL, per-service DB**.
- [x] İş servisleri: `customer-service`, `account-service`, `product-service`,
  `cart-service`, `order-service` (n-layered). Bkz. §6, §8.
- [x] Choreography Saga: Billing Account, Product Sale, Cart Item, Order Checkout.
  Bkz. §7.
- [x] Sepetten siparişe geçiş (checkout) — `order-service` (FR-016) eklendi;
  cart-service doğrulayıcı roldedir. Bkz. §7.4, §8.5.
- [ ] Observability (Actuator, Micrometer, tracing) standardı.
- [ ] `Gender`/`Nationality` lookup tabloları (şimdilik `gender_id`/
  `nationality_id` ham referans; ilgili servis/tablo eklenince FK'ye
  bağlanacak).

## 11. Değişiklik Günlüğü

- **2026-07-23:** **Docker ortamı artık boş veriyle başlıyor.** `docker compose up` ile
  ayağa kalkan kurulumda yalnızca **referans/iskelet** tablolar tohumlanır; iş verisi
  tablolarının tamamı boştur ve API üzerinden doldurulur. Her serviste
  `src/main/resources/data-reference.sql` eklendi ve `configs/<service>/application-docker.yml`
  içinde `spring.sql.init.data-locations: classpath:data-reference.sql` ile varsayılan
  `data.sql` yerine bu betik okunuyor. **Dev profili değişmedi** — tam seed'i (iş verisiyle)
  `data.sql`'den okumaya devam eder.
  Tohumlanan tablolar: customer-service → `general_status`, `general_type`,
  `party_role_types`, `parties`, `party_roles`; account/product/cart/order → yalnızca kendi
  `general_status` dilimleri. (search-service'in seed'i zaten yoktur.) Referans satırları
  olmadan servisler açılır ama ilk yazma işleminde `ReferenceDataService` "referans veri
  bulunamadı" hatası verirdi; bu yüzden bu dosyalar docker'da zorunludur.
  **İki uyarı:** (1) `configs/**` config-server tarafından **uzak git deposundan** okunur —
  değişiklik commit + push edilmeden konteynerler görmez. (2) Betikler yalnızca tabloları
  doldurur, **var olan veriyi silmez**; dolu bir `postgres-data` volume'ünde boş başlangıç
  için `docker compose down -v` gerekir.
- **2026-07-23:** **Sol menüdeki "Çıkış Yap" çalışır hâle getirildi.** Satır `link="/login"`
  ile düz gezinme yapıyordu: oturum temizlenmediği için `guestGuard` kullanıcıyı anında
  `/customers`'a geri gönderiyordu (yani hiçbir şey olmuyordu). Çıkış artık bir **eylem**:
  `SidebarItem`'a `action` çıktısı eklendi (bağlantısız satırların düğmesi bunu yayınlar),
  `Sidebar` bunu `logout` olarak yukarı taşır, `MainLayout` mevcut `logout()` metoduyla
  `AuthService.logout()` çağırır — token/kullanıcı/refresh token temizlenir, Keycloak
  oturumu sonlandırılır ve `/login`'e gidilir. Üst bardaki kullanıcı menüsü zaten aynı
  metodu kullanıyordu; iki giriş noktası tek akışta birleşti.
- **2026-07-23:** **Ad alanlarına harf doğrulaması eklendi (frontend + backend).**
  Kabul edilen küme: Türkçe dâhil harfler + boşluk + kesme işareti (`'` ve `’`) + tire;
  rakam ve diğer özel karakterler reddedilir. Küme **üç yerde birebir aynı** tanımlıdır ve
  biri değişirse diğerleri de değişmelidir: frontend `NAME_CHARACTERS`
  (`shared/directives/character-mask.ts`), customer-service `ValidationPatterns.NAME_PATTERN`,
  search-service `CustomerSearchBusinessRules.NAME_PATTERN`.
  **Backend:** customer-service Create/Update isteklerinde `firstName`, `secondName`,
  `lastName`, `fatherName`, `motherName` alanlarına `@Pattern`; search-service'in FR-002
  filtresine ad/soyad desen kuralı (yeni mesaj `search.namePattern.invalid`).
  **Frontend:** `CharacterMask` temel direktifi + `LettersOnly` / `DigitsOnly`; **üç ekranda
  da** uygulandı — müşteri arama (Ad, Soyad, TC Kimlik No), müşteri oluşturma ve müşteri
  detayındaki **güncelleme** formu (Ad, İkinci Ad, Soyad, Anne Adı, Baba Adı, TC Kimlik No).
  Maske değeri sentetik bir `input` olayıyla yayınladığından hem **reactive forms** (arama)
  hem **signal forms** (oluşturma/güncelleme) ile çalışır.
  Oluşturma ve güncelleme formları aynı alanları taşıdığından tip ve kurallar tek yerde
  toplandı: `features/customers/customer-demographic.schema.ts` (`CustomerDraft` +
  `customerDemographicSchema`). Önceden iki ayrı kopya vardı ve güncelleme formundaki kopya
  geride kalmıştı; kurallar artık ayrışamaz.
  *Not:* Signal Forms `maxlength`'i şablondan kabul etmez; uzunluk sınırları şemadaki
  `maxLength()` ile verilir ve backend `@Size` değerleriyle hizalıdır.
- **2026-07-23:** **Sipariş numarası formatı değişti: `ORD-XXXXXXXX` → 8 hane, yalnızca
  rakam.** `OrderManager.generateOrderNumber()` artık UUID kırpmak yerine `SecureRandom`
  ile 8 haneli bir sayı üretir (`%08d` — baştaki sıfırlar korunur, bu yüzden alan metin
  olarak saklanır). Çakışmaya karşı üretim öncesi `existsByOrderNumber` ile kontrol edilir
  (5 deneme), son güvence `order_number` üzerindeki mevcut unique kısıttır. Kolon
  `length` 40 → 8. account-service'teki `orderNumber` alanı (`length` 20 → 8) ve
  Create/Update doğrulaması (`@Pattern("^\\d{8}$")`), search-service'in ACC-08 filtre
  kuralı ve tüm seed'ler (`ORD0000000X` → `1000000X`) aynı kurala hizalandı.
- **2026-07-23:** **Arama panelinde rakam-dışı giriş engellendi.** Yeni `DigitsOnly`
  direktifi (`shared/directives/digits-only.ts`) iki katmanlı çalışır: `beforeinput` ile
  klavyeden gelen harf **hiç girilmez** (sonradan silinmez), `input` ile yapıştırma/otomatik
  doldurma değeri temizlenir (tamamı reddedilmez) ve reactive form kontrolüne yazılır.
  Hesap Numarası (`maxlength=10`) ve Sipariş Numarası (`maxlength=8`) alanlarına uygulandı.
- **2026-07-23:** **Hesap numarası kuralı sıkılaştırıldı.** `account_number` artık
  **yalnızca rakam, tam 10 hane ve benzersiz**: entity'de `length=10` + `unique=true`,
  Create/Update isteklerinde `@Pattern("^\\d{10}$")` (alan opsiyonel kalır — `null`
  geçilebilir, boş string geçilemez). Benzersizlik kontrolü artık **soft-delete edilmiş
  kayıtları da kapsar** (`existsByAccountNumber`); hesap numarası kalıcı bir iş kimliğidir,
  iptal edilmiş bir hesabınki yeniden verilemez — bu kapsam DB'deki unique kısıtla birebir
  aynıdır, böylece kullanıcı ham DB hatası yerine iş hatası alır. `search-service`'in
  FR-002 filtre doğrulaması (ACC-07) da aynı kurala hizalandı (eskiden alfanümerik ≤30);
  arama zaten tam eşleşme yaptığından kısmi numarayla arama desteklenmez. Seed'lerdeki
  `ACC00000000X` numaraları `100000000X` olarak güncellendi (hem `data.sql` hem
  `infra/seed/02_account_seed.sql`). Frontend arama panelindeki yer tutucular yeni kurala
  göre yazıldı.
  *Yan düzeltme:* account-service'in `application-test.yml`'ine `spring.sql.init.mode:
  never` eklendi — §9'daki "hermetik test profili tuzağı" bu serviste de açıktı ve
  `contextLoads` testi zaten kırıktı (aynı düzeltme customer-service ve cart-service'te
  mevcut).
  *Migration notu:* `ddl-auto: update` kolon **daraltmaz** ve unique kısıtı güvenilir
  şekilde eklemez. Var olan `accountdb` için elle: `ALTER TABLE billing_accounts ALTER
  COLUMN account_number TYPE varchar(10); ALTER TABLE billing_accounts ADD CONSTRAINT
  uk_billing_accounts_account_number UNIQUE (account_number);` (önce eski formattaki
  numaralar güncellenmeli).

- **2026-07-23:** **Keycloak kullanıcısı Party modeline bağlandı.** `party_role_types`'a
  `USER` rol tipi ve `general_status`'a `SYS_USER` dilimi eklendi; yeni `SystemUser`
  entity'si (`party_role_id` 1-1 + `keycloak_user_id` unique + `username`) Keycloak
  kullanıcısına domain kimliği verir — **kopya değil referans**: parola/rol/oturumun tek
  otoritesi Keycloak'tır. Zincir (`Party → PartyRole(USER) → SystemUser`) kullanıcının ilk
  kimliği doğrulanmış isteğinde `SystemUserProvisioningFilter` ile kurulur; yaratma
  `SystemUserProvisioner`'da **tek transaction**, tekrar isteklerde JVM içi küme sayesinde
  DB'ye hiç gidilmez. Yarış durumunda unique kısıt devreye girer (idempotent).
  `PartyRoleManager` artık müşteri/kullanıcı rollerini ortak bir yardımcıdan üretir ve
  **çok-rol güvenli**: party yalnızca son aktif rolü de düştüğünde pasifleşir (önceden
  müşteri silinince aynı party'nin kullanıcı rolü de kopardı). Bkz. §8.1.
  *Yan düzeltme:* customer-service'in `application-test.yml`'ine `spring.sql.init.mode:
  never` eklendi — §9'da belgelenen "hermetik test profili tuzağı" bu serviste açıktı ve
  `contextLoads` testi bu yüzden zaten kırıktı (cart-service'te aynı düzeltme mevcuttu).

- **2026-07-10:** **search-service** (müşteri arama, n-layered, port 8087, searchdb)
  eklendi: FR-002 "Müşteri Arama ve Görüntüleme". Cross-service arama için bir **CQRS
  read-model**'dir — tek denormalize `CustomerSearchIndex` tablosu (+ `search_account_numbers`
  / `search_order_numbers` koleksiyonları), tek uç `GET /api/v1/search/customers` (dinamik
  JPA Specification: tam-eşleşme + starts-with, ACC-15 AND / ACC-16 OR, sayfalı, Redis
  cache). İki consumer **Inbox** ile idempotent: `crm.Customer.events` (upsert/remove) ve
  `crm.Account.events` (account/order numarası ekle/çıkar); account olayı customer'dan önce
  gelirse **stub satır** ile sıra-bağımsız çalışır. Servisin kendi outbox'ı/Debezium
  connector'ı **yoktur** (yalnızca tüketici), saga'ya katılmaz. **Ön iş:** search'in
  ihtiyaç duyduğu alanlar için iki olay sözleşmesi geriye-uyumlu genişletildi —
  `CustomerEventPayload` (+`secondName`, `nationalityId`, `gsmNumber`, `role`),
  `BillingAccountEventPayload` (+`accountNumber`, `orderNumber`). Modül sayısı 8→9.
  Bkz. §8.6.
- **2026-07-10:** **order-service** (sipariş, n-layered, port 8086, orderdb)
  eklendi: FR-016 "Siparişin Tamamlanması" (Submit Order). `Order`/`OrderItem`
  modeli (benzersiz `orderNumber`, servis adresi snapshot'ı, kalem/toplam
  snapshot'ı), CRUD, Redis cache. Sepetten siparişe geçiş **choreography Saga** ile
  yapılır (`crm.OrderCheckoutSaga.events`): order-service siparişi PENDING açıp
  doğrulama ister, cart-service (**doğrulayıcı**) sepeti otoriter kontrol edip
  sahiplik/satır/toplam snapshot'ıyla sonuç yayınlar, order-service siparişi
  CONFIRMED yapar ya da telafi ile CANCELLED eder. Inbox ile idempotent; order
  outbox'ı için yeni Debezium connector (`register-order-connector.json`), saga
  sonuç kayıtları mevcut cart connector'ı üzerinden yönlenir. cart-service artık
  iki consumer taşır (`cartSagaConsumer` + `orderCheckoutRequestConsumer`).
  Bkz. §7.4, §8.5.
- **2026-07-09:** **cart-service** (sepet, n-layered, port 8085, cartdb)
  eklendi: Cart/CartItem modeli, iki ekleme yolu (katalogdan teklif / kampanya
  paketi), CRUD, Redis cache. Cross-service doğrulama **choreography Saga** ile
  yapılır (`crm.CartSaga.events`): cart-service satırı PENDING açıp doğrulama
  ister, product-service (doğrulayıcı) teklifi/kampanyayı otoriter doğrulayıp
  ad/fiyat/içerik ile sonuç yayınlar, cart-service satırı ACTIVE yapar ya da
  telafi ile CANCELLED eder. Inbox ile idempotent; yeni Debezium connector
  gerekmez. **Not:** ilk taslakta denenen "product-service offer/campaign
  event'i yayınlar + cart yerel projeksiyon tutar" yaklaşımı saga modeline
  uygun olmadığından kaldırıldı. Bkz. §7.3, §8.4.
- **2026-07-06:** Proje başlatıldı. Parent POM (`etiya.com:crm-lite`) ve bu
  project-brain dökümanı oluşturuldu.
- **2026-07-06:** `eureka-server` ve `gateway-server` eklendi (4 profil:
  dev/test/prod/docker), Dockerfile'lar oluşturuldu.
- **2026-07-06:** `config-server` (Spring Cloud Config) eklendi. Tüm servisler
  `spring.config.import` ile merkezi config'e bağlandı; ortam bazlı ayarlar
  kök `configs/<service>/` klasörüne taşındı (`test` profili yerel kaldı).
  Bkz. §5.
- **2026-07-06:** `customer-service` (ilk iş servisi, n-layered) eklendi:
  PostgreSQL, Redis (+RedisInsight), Kafka + Transactional Outbox/Debezium,
  Inbox Pattern, JOINED kalıtım (Customer/IndividualCustomer), BaseEntity,
  merkezi hata yönetimi, DTO+MapStruct, iş kuralları. Altyapı `infra/`.
  Bkz. §8.1.
- **2026-07-08:** **Saga Pattern (choreography)** eklendi — Fatura Hesabı
  Oluşturma akışı: account-service `PENDING` başlatır, customer-service
  otoriter doğrular (validated/failed), account-service onaylar (`ACTIVE`) veya
  telafi eder (`CANCELLED`). Tek saga kanalı `crm.BillingAccountSaga.events`
  (yeni connector yok). Bkz. §7.1.
- **2026-07-08:** Saga **update akışına** genişletildi ve **CQRS projeksiyonu
  kaldırıldı**. Fatura hesabı adres değişikliği artık `pendingAddressId` +
  `...AddressChangeRequested` ile Saga üzerinden otoriter doğrulanır (onayda
  uygulanır, redde eski adres korunur). account-service'teki müşteri/adres
  projeksiyonu (read-model) ve `customerEventConsumer` silindi; tüm
  cross-service doğrulama Saga'dan geçer. Bkz. §7.1.
- **2026-07-08/09 (product-service):** `product-service` eklendi: Katalog
  (zorunlu kategori) / Kampanya (opsiyonel paket) modeli, `Product` satışı
  choreography Saga ile (`crm.ProductSaga.events`, account-service'i
  doğrulayıcı olarak kullanır). Bkz. §7.2, §8.3.
