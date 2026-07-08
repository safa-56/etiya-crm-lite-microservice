# Etiya CRM Lite - Project Brain

> Bu dosya projenin canlı hafızasıdır. Mimari kararlar, standartlar ve ilerleme
> burada tutulur. Projede ilerledikçe güncellenir.

_Son güncelleme: 2026-07-06_

---

## 1. Proje Özeti

Etiya CRM Lite, mikroservis mimarisiyle geliştirilen bir CRM uygulamasıdır.
Her servis bağımsız olarak paketlenir (`jar`) ve çalıştırılır; ortak yapı ve
bağımlılık yönetimi merkezi bir **parent POM** üzerinden sağlanır.

## 2. Teknoloji Yığını (Tech Stack)

| Katman            | Teknoloji            | Versiyon        |
|-------------------|----------------------|-----------------|
| Dil               | Java                 | 25              |
| Framework         | Spring Boot          | 4.1.0           |
| Build aracı       | Maven                | 3.9+            |
| Konfigürasyon     | YAML (`application.yml`) | -           |
| Paketleme         | JAR (alt servisler)  | -               |

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
   Kullanıcı "jar olacak" dedi; ancak Maven kuralı gereği parent olarak
   kullanılan bir artifact `pom` paketlenmek zorundadır. `jar` paketlemesi
   **alt servisler** için geçerlidir. Bu bilinçli ve zorunlu bir tercihtir.

3. **Konfigürasyon YAML üzerinden.**
   Servisler `application.yml` kullanır. `spring-boot-configuration-processor`
   parent'a eklendi (IDE'de yaml auto-complete/metadata desteği için).

4. **Merkezi versiyon yönetimi.**
   BOM dışındaki bağımlılıklar (`springdoc`, `mapstruct`) `properties` +
   `dependencyManagement` ile merkezileştirildi. Alt POM'lar versiyon yazmaz.

5. **Ortak bağımlılıklar (tüm servislerde):**
   - `spring-boot-configuration-processor` (optional)
   - `lombok` (optional)
   - `spring-boot-starter-test` (test)

6. **Annotation processor sırası:** Lombok → MapStruct (compiler plugin'de
   `annotationProcessorPaths` ile sabitlendi).

## 4.1. Merkezi Konfigürasyon (Spring Cloud Config)

`config-server` modülü, tüm servislerin ortam bazlı konfigürasyonunu tek bir
kaynaktan sunar (`@EnableConfigServer`).

**Mimari kararlar ve gerekçeleri:**

1. **Git backend + tek repo.** Config server, bu reponun kendisini Git backend
   olarak kullanır (`https://github.com/safa-56/etiya-crm-lite-microservice.git`).
   Konfigürasyonlar kök `configs/` klasöründe, servis adına göre klasörlenir:
   `configs/<service>/application-<profile>.yml`.

2. **`search-paths: configs/{application}`.** `{application}` placeholder'ı,
   istek yapan servisin `spring.application.name` değerine göre çözülür; config
   server yalnızca o servisin klasörüne bakar. Port: **8888**.

3. **Client bağlantısı `spring.config.import` ile.** Bootstrap context yerine
   (Spring Cloud 2020+ standardı) her servisin yerel `application.yml`'inde:
   `import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}`.
   `optional:` öneki, config server erişilemezse servisin yereldeki ayarlarla
   açılmasına izin verir (dayanıklılık).

4. **Yerel vs. merkezi ayrımı.** Servislerin yerel `application.yml` dosyaları
   yalnızca **bootstrap** bilgisi tutar (isim, aktif profil, config importu).
   `dev/docker/prod` ayarları merkezi `configs/` altına taşındı. **`test`
   profili** ise hermetik (ağdan bağımsız) testler için her serviste **yerel**
   kalır.

5. **`config-server` kendi konfigürasyonunu yereldan yükler** (yumurta-tavuk
   problemi); başka bir config server'dan beslenmez.

6. **Servis açılış sırası:** config-server → eureka-server → gateway-server.
   Bu nedenle `config-server`, kök POM `<modules>` listesinde ilk sıradadır.

**ÖNEMLİ:** Git backend uzak depoyu klonlar; `configs/` değişikliklerinin config
server tarafından görülmesi için **commit + push** gerekir. Private repo için
`CONFIG_GIT_USERNAME` / `CONFIG_GIT_PASSWORD` ortam değişkenleri kullanılır.

## 5. Modül Yapısı

Tanımlı servisler: **config-server** (8888), **eureka-server** (8761),
**gateway-server** (8080). Yeni servis eklenirken:

1. Servis dizini oluşturulur (örn. `customer-service/`).
2. Servis POM'unda parent olarak `etiya.com:crm-lite:1.0.0-SNAPSHOT` gösterilir.
3. Kök `pom.xml` içindeki `<modules>` bloğuna eklenir.

Örnek alt servis POM `<parent>` bloğu:

```xml
<parent>
    <groupId>etiya.com</groupId>
    <artifactId>crm-lite</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</parent>
<artifactId>customer-service</artifactId>
<packaging>jar</packaging>
```

## 6. Standartlar & Konvansiyonlar

- Konfigürasyon: `properties` değil **`yaml`** kullanılır.
- Bağımlılık versiyonları: Mümkünse Spring Boot BOM'a bırakılır; değilse
  parent'ın `dependencyManagement`'ında yönetilir. Alt POM'lara versiyon yazılmaz.
- Encoding: UTF-8.

## 4.2. customer-service (ilk iş servisi)

`customer-service` (port **8081**), n-katmanlı (n-layered) ilk somut iş servisidir.

**Katmanlar (paketler):** `entities`, `dataAccess`, `business`
(+ `business/rules`, `business/mappers`, `business/constants`, `business/dtos`),
`apiController`, ayrıca cross-cutting için `core`.

**Alınan kararlar ve gerekçeleri:**

1. **Veritabanı: PostgreSQL (per-service DB).** Servis kendi şemasını yönetir.
   Debezium CDC için `wal_level=logical` gerekir (bkz. `infra/`).

2. **Kalıtım = JPA `JOINED`.** Görseldeki ER modeli birebir uygulandı:
   `Customer` (kök, `customers`) ← `IndividualCustomer` (`individual_customers`,
   `@PrimaryKeyJoinColumn(customer_id)`; 1-1 paylaşılan PK). `Address` ve
   `CustomerContactInfo` müşteriyle N-1 (`customer_id` FK).

3. **`BaseEntity` (@MappedSuperclass).** Ortak alanlar `id, created_date,
   updated_date, deleted_date, is_active` burada; tüm entity'ler miras alır.
   Soft-delete `is_active` + `deleted_date` ile yapılır (görseldeki `is_deleted`
   bunlarla karşılandı). Zaman damgaları `@PrePersist/@PreUpdate` ile otomatik.

4. **Asenkron iletişim: Kafka (local container) + Transactional Outbox + Debezium.**
   Manager, iş verisi ile `outbox_events` kaydını **aynı transaction**'da yazar
   (ghost event yok). Debezium (Kafka Connect) outbox tablosunu izleyip
   `EventRouter` SMT ile `crm.<aggregateType>.events` topic'ine yayınlar.
   Broker **local container** (KRaft, Zookeeper'sız, tek node); uygulama tüketici
   tarafında **Spring Cloud Stream (Kafka binder)** fonksiyonel binding kullanır
   (`inboundEventConsumer-in-0`). Kafka Cloud kullanılmaz.

5. **Duplicate consume: Inbox Pattern.** `inbox_messages` tablosu + `InboxService`
   ile `messageId` bazlı idempotent tüketim (örnek: `ExampleInboxConsumer`).

6. **Cacheleme: Redis (Spring Cache).** `RedisCacheConfig` ile JSON serileştirme
   ve cache bazlı TTL. Cache'ler **RedisInsight** ile izlenir.

7. **İş kuralları `business/rules` altında** ayrı sınıfta toplanır ve ilgili
   manager'a inject edilir. **Mesajlar** `business/constants/Messages` sabitleri
   (magic string yok). İstek/yanıt için **DTO** (record) + **MapStruct** mapper.

8. **Hata yönetimi:** `core` altında merkezi `@RestControllerAdvice`
   (`GlobalExceptionHandler`, RFC 7807 `ProblemDetail`) ve iş hataları için
   özel `BusinessException`.

**Altyapı:** Kök `infra/docker-compose.yml` → PostgreSQL, Redis, RedisInsight,
Debezium (Kafka Connect). Debezium outbox connector: `infra/debezium/`. Bkz.
`infra/README.md`.

## 4.3. Saga Pattern (choreography) — Fatura Hesabı

Servisler arası dağıtık işlem **choreography-based Saga** ile yürütülür (merkezi
orchestrator yok; her servis olayları dinleyip kendi adımını yapar, hata olursa
telafi eder). Mevcut **Outbox + Debezium + Inbox** altyapısı üzerine kuruludur.
**CQRS read-model (müşteri projeksiyonu) kaldırıldı**; account-service artık
müşteri/adres doğrulamasını yerel bir kopyadan değil, **Saga ile customer-service'e
otoriter olarak** yaptırır. Böylece tüm cross-service yazma kararları saga'dan geçer.

**Kanal:** Aggregate tipi `BillingAccountSaga` olan tüm outbox kayıtları — hangi
servisin DB'sinden gelirse gelsin — EventRouter ile tek topic'e yönlenir:
`crm.BillingAccountSaga.events`. **Yeni Debezium connector gerekmez** (mevcut iki
connector aggregate_type'a göre dinamik yönlendirir). Her iki servis bu topic'i
dinler ve payload'daki `eventType` ile yalnızca kendini ilgilendiren olayı işler
(self-consume olayları atlanır → döngü yok).

**customer-service** doğrulayıcı (participant) rolündedir: gelen isteklerde
(`...CreationRequested` / `...AddressChangeRequested`) müşteri + adresi **kendi
DB'sinden otoriter** doğrular → `...CustomerValidated` (adres snapshot'ıyla) ya da
`...CustomerValidationFailed` (neden ile) yayınlar. account-service sonucu hesabın
durumuna göre yönlendirir.

**Akış 1 — Create Billing Account:**
1. account-service hesabı `PENDING` açar (adres boş) → `...CreationRequested`.
2. customer-service doğrular → Validated/Failed.
3. account-service: Validated → `ACTIVE` (otoriter adres yazılır); Failed →
   `CANCELLED` + soft-delete (**telafi**).

**Akış 2 — Update Billing Account Address:**
1. account-service adres-dışı alanları senkron günceller; adres değiştiyse yeni
   adresi `pendingAddressId`'de tutar (hesap ACTIVE kalır) → `...AddressChangeRequested`.
2. customer-service (aynı doğrulama) → Validated/Failed.
3. account-service: Validated → yeni adresi uygular (`addressId` + metin), beklemeyi
   temizler; Failed → beklemeyi temizler, **eski adres korunur** (telafi).

**Durumlar:** `AccountStatus` = `PENDING → ACTIVE | CANCELLED` (+ `PASSIVE` silmede).
Create ve adres-update **asenkron** tamamlanır (endpoint hemen döner; sonuç saga ile
gelir). Idempotency: Inbox + sonucu hesap durumuna göre yönlendirme
(PENDING → create; `pendingAddressId` → update; ikisi de yoksa atla).

## 7. Açık Sorular / Yapılacaklar

- [x] Servis keşfi: **Netflix Eureka** (`eureka-server`) + API Gateway
  (`gateway-server`, Spring Cloud Gateway).
- [x] Merkezi konfigürasyon: **Spring Cloud Config** (`config-server`), Git
  backend + kök `configs/` klasörü. Bkz. §4.1.
- [x] Veritabanı teknolojisi: **PostgreSQL, per-service DB** (`customer-service`).
- [x] İlk somut servis: **`customer-service`** (n-layered) oluşturuldu. Bkz. §4.2.
- [ ] Observability (Actuator, Micrometer, tracing) standardı.
- [ ] `Gender`/`Nationality` lookup tabloları (şimdilik `gender_id`/`nationality_id`
  ham referans; ilgili servis/tablo eklenince FK'ye bağlanacak).

## 8. Değişiklik Günlüğü

- **2026-07-06:** Proje başlatıldı. Parent POM (`etiya.com:crm-lite`) ve bu
  project-brain dökümanı oluşturuldu.
- **2026-07-06:** `eureka-server` ve `gateway-server` eklendi (4 profil:
  dev/test/prod/docker), Dockerfile'lar oluşturuldu.
- **2026-07-06:** `config-server` (Spring Cloud Config) eklendi. Tüm servisler
  `spring.config.import` ile merkezi config'e bağlandı; ortam bazlı ayarlar kök
  `configs/<service>/` klasörüne taşındı (`test` profili yerel kaldı). Bkz. §4.1.
- **2026-07-08:** Saga **update akışına** genişletildi ve **CQRS projeksiyonu
  kaldırıldı**. Fatura hesabı adres değişikliği artık `pendingAddressId` +
  `...AddressChangeRequested` ile Saga üzerinden otoriter doğrulanır (onayda uygulanır,
  redde eski adres korunur). account-service'teki müşteri/adres projeksiyonu
  (read-model) ve `customerEventConsumer` silindi; tüm cross-service doğrulama Saga'dan
  geçer. Bkz. §4.3.
- **2026-07-08:** **Saga Pattern (choreography)** eklendi — Fatura Hesabı Oluşturma
  akışı: account-service `PENDING` başlatır, customer-service otoriter doğrular
  (validated/failed), account-service onaylar (`ACTIVE`) veya telafi eder
  (`CANCELLED`). Tek saga kanalı `crm.BillingAccountSaga.events` (yeni connector
  yok). Bkz. §4.3.
- **2026-07-06:** `customer-service` (ilk iş servisi, n-layered) eklendi:
  PostgreSQL, Redis (+RedisInsight), Kafka Cloud + Transactional Outbox/Debezium,
  Inbox Pattern, JOINED kalıtım (Customer/IndividualCustomer), BaseEntity,
  merkezi hata yönetimi, DTO+MapStruct, iş kuralları. Altyapı `infra/`. Bkz. §4.2.
