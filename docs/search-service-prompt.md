# Görev: Etiya CRM Lite'a `search-service` (CQRS read-model) ekle + gerekli event genişletmeleri

Bu repo mikroservis tabanlı bir CRM'dir (Spring Boot 4, Spring Cloud, per-service
PostgreSQL, Kafka + Debezium + Transactional Outbox, Inbox Pattern, Redis, Eureka,
Spring Cloud Gateway, choreography Saga). FR-002 "Müşteri Arama ve Görüntüleme"
ekranını karşılayacak yeni bir **`search-service`** ekleyeceksin. Bu servis
cross-service ARAMA için bir **CQRS read-model**'dir: mevcut Kafka event akışlarını
dinleyip tek bir denormalize "customer search index" tablosu tutar ve tek bir
endpoint'ten sorgular. Yazma tarafına (müşteri/hesap CRUD mantığına) dokunmaz.

Bu görev İKİ parçadan oluşur ve İKİSİ de zorunludur:
- **A) Ön iş:** İki mevcut servisin event payload'larını genişletmek (aşağıda; search
  için gereken alanlar şu an event'lerde YOK).
- **B) `search-service`'i baştan yazmak.**

---

## 0) ÖNCE OKU (bağlam ve şablon)

- `docs/project-brain.md` — mimari kararlar, standartlar; özellikle **§4.1 "Yeni servis
  ekleme prosedürü"**, **§6 n-layered şablon**, **§8 servis notları**. BUNLARA BİREBİR UY.
- `customer-service/` — **referans şablon**. Paket yapısı, `BaseEntity`, Outbox/Inbox,
  Spring Cloud Stream consumer binding'i (`business/messaging/*ConsumerConfig`),
  `core/config/RedisCacheConfig`, `core/crosscutting/.../GlobalExceptionHandler`,
  `PagedResponse`, DTO(record)+MapStruct, `business/rules`, `business/constants/Messages`
  desenini AYNEN kopyala. Yeni servis bunların birebir aynısı olmalı.

## Uyulacak proje kuralları (project-brain'den özet)

- Java 25, Spring Boot 4.0.0, Spring Cloud 2025.1.0. Maven parent:
  `etiya.com:crm-lite:1.0.0-SNAPSHOT` (alt POM'da `spring-cloud.version` tanımlı).
- n-layered paketler: `entities`(+`enums`,`outbox`,`inbox`), `dataAccess`,
  `business`(`rules`,`mappers`,`constants`,`dtos`(`requests`/`responses`/`events`),
  `messaging`,`abstracts`,`concretes`), `apiController`, `core`(`config`,`constants`,
  `crosscutting/exceptions`).
- PostgreSQL per-service DB. Bu servis DB'si: **`searchdb`**. Port: **8087**.
- `BaseEntity` (@MappedSuperclass): `id` IDENTITY, `created_date/updated_date/
  deleted_date`, `is_active`, soft-delete. Zaman damgaları `@PrePersist/@PreUpdate`.
- Merkezi config: `configs/search-service/` altına `application.yml` + `application-dev.yml`
  + `application-docker.yml` + `application-prod.yml`. Servisin yerel
  `src/main/resources/application.yml`'i yalnız bootstrap (isim, aktif profil, config
  import: `optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}`) taşır.
  Hermetik testler için yerel `application-test.yml` (H2, `app.kafka.enabled: false`,
  `spring.sql.init.mode: never`).
- Kafka tüketimi: **Spring Cloud Stream (Kafka binder)**, fonksiyonel
  `java.util.function.Consumer<Message<String>>` bean'leri; yalnız `app.kafka.enabled=true`
  iken aktif (test profilinde false).
- **Idempotent tüketim: Inbox Pattern** (`inbox_messages` tablosu +
  `InboxService.process(messageId, eventType, handler)` — customer-service'teki aynısı).
- Hata yönetimi: `core` altında `GlobalExceptionHandler` (RFC 7807 `ProblemDetail`).
- Sayfalama: `Pageable` → `PagedResponse<T>` (content + pageNumber/pageSize/
  totalElements/totalPages/last).
- DTO = Java `record`; MapStruct (`componentModel="spring"`,
  `unmappedTargetPolicy=ReportingPolicy.IGNORE`).
- Magic string yok: mesajlar `business/constants/Messages` sabitlerinden.
- **`configs/*` dosyaları commit + push edilmeden config-server (Git backend) göremez.**
- **Yeni Debezium connector GEREKMEZ:** search-service'in kendi outbox'ı yoktur; yalnız
  tüketicidir. Mevcut customer ve account connector'ları event'leri zaten yayınlıyor.

---

## A) ÖN İŞ — iki event payload'ını genişlet (search için zorunlu)

Search index'in ihtiyaç duyduğu alanların bir kısmı ŞU AN event'lerde YOK. Önce bunları ekle.

### A.1 customer-service — `CustomerEventPayload` genişlet
Dosya: `customer-service/.../business/dtos/events/CustomerEventPayload.java`
Şu an yalnızca `customerId, firstName, lastName, eventType, addresses, occurredAt` var.
**Ekle:** `secondName`, `nationalityId` (TCKN), `gsmNumber`, `role` (şimdilik sabit "B2C").
- `gsmNumber` müşterinin `CustomerContactInfo` kayıtlarındadır (event'te yok). Müşterinin
  **birincil/ilk GSM** iletişim bilgisini event'e koy. `CustomerContactInfo` entity'sindeki
  alan adını (telefon/gsm + tip) kontrol et; birincil GSM'i seçecek mantığı ekle.
- Bu event'i yazan yeri bul (`IndividualCustomerManager` — müşteri create/update/delete'te
  `OutboxService.publish(...)` çağrısı) ve yeni alanları doldur. `secondName` zaten
  `IndividualCustomer.secondName` alanında var.
- Var olan tüketicileri (account-service) bozma: yeni alanlar eklemek geriye uyumludur
  (JSON deserialize'de bilinmeyen alanlar zaten ignore ediliyorsa sorun yok; account
  tarafının deserialize ayarını kontrol et, gerekirse `@JsonIgnoreProperties(ignoreUnknown=true)`).

### A.2 account-service — `BillingAccountEventPayload` genişlet
Dosya: `account-service/.../business/dtos/events/BillingAccountEventPayload.java`
Şu an `billingAccountId, customerId, accountName, accountStatus, occurredAt` var.
**Ekle:** `accountNumber`, `orderNumber`.
- Bu iki alan zaten `BillingAccount` entity'sinde var (`account_number`, `order_number`)
  ve create/update'te kullanıcı tarafından set ediliyor.
- Bu event'i yazan yeri bul (`BillingAccountManager` içindeki `OutboxService.publish(...)`)
  ve yeni alanları doldur.

> NOT (tasarım gerekçesi): FR-002'deki hem **Account Number** hem **Order Number**
> arama alanları bu projede **BillingAccount** üzerindedir (account-service). Bu yüzden
> search-service order-service'i DİNLEMEZ; yalnız customer + account event'lerini tüketir.
> (order-service'in kendi `Order.orderNumber`'ı ayrı bir şeydir — `ORD-XXXXXXXX`, 40 hane —
> ve FR-002 aramasının konusu değildir.)

---

## B) `search-service`'i yaz

### B.1 Read-model entity — `CustomerSearchIndex` (tablo: `customer_search_index`)
Denormalize tek satır = bir müşteri. Alanlar:
- `customerId` (unique iş anahtarı, indeksli), `firstName`, `secondName`, `lastName`,
  `nationalityId` (TCKN), `gsmNumber`, `role` (enum: B2C | B2B; şimdilik hep B2C).
- `accountNumbers` ve `orderNumbers`: bir müşterinin **birden çok** fatura hesabı olabilir,
  bu yüzden **ayrı child tablolar** (`search_account_numbers`, `search_order_numbers`) ya da
  `@ElementCollection`. Her ikisi de indeksli (arama alanı).
- Arama sütunları için indeks: `nationalityId`, `gsmNumber`, `firstName`, `lastName`,
  account/order numaraları.
- Bu index event'lerle yönetilir (upsert/remove); saf read-model olduğu için BaseEntity
  soft-delete zorunlu değil ama isim/yapı tutarlılığı için BaseEntity kullanabilirsin.

### B.2 Consumer'lar (2 akış, Inbox ile idempotent)
1. **`crm.Customer.events`** (genişletilmiş `CustomerEventPayload`):
   - create/update → `customer_search_index` satırını `customerId`'ye göre **upsert**
     (firstName, secondName, lastName, nationalityId, gsmNumber, role).
   - delete → satırı kaldır (ya da pasifleştir).
2. **account event akışı** (genişletilmiş `BillingAccountEventPayload`):
   - ilgili `customerId` satırına `accountNumber` + `orderNumber` **ekle**; hesap silin/
     pasifleşince ilgili numaraları çıkar.
   - **Sıra bağımsızlığı:** account event, henüz customer index satırı oluşmadan gelebilir.
     Satır yoksa `customerId` ile bir **stub satır oluştur** ve numaraları ekle; customer
     event geldiğinde ismi/TCKN/GSM'i doldur (upsert).
- Her iki consumer da customer-service'teki `InboxService.process(...)` deseniyle korunur.
  Topic adlarını mevcut Debezium yönlendirmesinden doğrula (`crm.<aggregateType>.events`).

### B.3 Endpoint — `GET /api/v1/search/customers`
Query paramları (hepsi opsiyonel): `segment` (default `B2C`), `idNumber`, `customerId`,
`accountNumber`, `gsm`, `firstName`, `lastName`, `page`, `size` (default 50),
`sort` (default `customerId,asc`). **Not:** `orderNumber` param'ı da al (aynı OR bloğuna girer).
Dönen: `PagedResponse<CustomerSearchResponse{customerId, firstName, secondName, lastName, role, nationalityId}>`.

**Sorgu mantığı — JPA Specification/Criteria API ile dinamik kur (FR-002):**
- **ACC-14 / Adım 9 — TAM eşleşme:** `customerId`, `idNumber`(TCKN), `accountNumber`,
  `gsm`, `orderNumber`.
- **ACC-17 — starts-with, case-insensitive:** `firstName`, `lastName` (`ILIKE 'deger%'`).
- **ACC-15 — AND:** `firstName` + `lastName` birlikte verildiyse AND.
- **ACC-16 (KRİTİK) — OR:** (firstName/lastName bloğu) İLE diğer kriterler
  (`customerId`, `idNumber`, `accountNumber`, `gsm`, `orderNumber`) arasında **OR**. Yani:
  ```
  WHERE (firstName ILIKE ? AND lastName ILIKE ?)
     OR customerId    = ?
     OR nationalityId = ?
     OR ? IN elements(accountNumbers)
     OR gsmNumber     = ?
     OR ? IN elements(orderNumbers)
  ```
  (Yalnız DOLU gelen kriterler bloğa eklenir. accountNumber/orderNumber child koleksiyonlarda
  arandığı için uygun join/subquery kullan.)
- **ACC-19:** ilk sayfa max **50** kayıt, sonrası sayfalama.
- **ACC-20:** varsayılan sıralama **`customerId` artan**.
- **ACC-21:** sonuç kolonları: Customer ID, First Name, Second Name, Last Name, Role, ID Number.
- **ACC-24:** eşleşme yoksa boş liste dön (UI "No customer found / Create Customer" gösterir).
- **segment=B2C** → yalnız bireysel müşteriler (şu an tek tür bu; role=B2C).

**Backend validasyonu (ACC-04..10), geçersizse `GlobalExceptionHandler` ile 400:**
- `idNumber`: yalnız rakam, tam **11** hane.
- `gsm`: yalnız rakam, max **15**.
- `customerId`: yalnız rakam, max **20**.
- `accountNumber`: alfanümerik, max **30**.
- `orderNumber`: alfanümerik, max **20**.
- `firstName`/`lastName`: max **50**; baş/son boşlukları **trim** et (ACC-10).

### B.4 Redis cache
Liste sorgusu için makul TTL (customer-service `CacheNames` + `RedisCacheConfig` deseni;
liste ~2 dk). Cache anahtarı sorgu parametrelerini içermeli.

---

## §4.1 checklist (bitirmeden HEPSİNİ yap)
1. `search-service/` dizini + n-layered paketler + POM (`<parent>` etiya.com:crm-lite,
   `spring-cloud.version` property'si, `<packaging>jar</packaging>`).
2. Kök `pom.xml` `<modules>`'a `search-service` ekle.
3. `configs/search-service/` altına 4 profil dosyası; yerelde bootstrap + `application-test.yml`.
4. `configs/gateway-server/application.yml` route listesine `/api/v1/search/**` ekle
   (mevcut route'larla aynı desen, `StripPrefix=1`).
5. `infra/postgres/init/01-create-databases.sql`'e `searchdb` ekle;
   `infra/docker-compose.yml`'e servis bloğu (diğerleriyle aynı desende, yorumlu olabilir).
   Var olan Postgres volume'ünde init çalışmaz — README'deki gibi `CREATE DATABASE searchdb;`
   elle gerekebileceğini not düş.
6. **Yeni Debezium connector EKLEME** (gerekmez — yalnız tüketici).
7. `configs/*` ve kod değişikliklerinin config-server için commit+push gerektiğini belirt.

## Test
- En az: endpoint + Specification sorgu mantığı için hermetik test (H2, Kafka kapalı):
  ACC-16 OR mantığı, ACC-15 AND, tam-eşleşme vs starts-with, sıralama (customerId asc),
  sayfalama (50). Consumer için upsert + sıra-bağımsız (stub satır) senaryosu.

## Bitince
- Ne kurduğunu ve **A) bölümündeki iki event payload değişikliğini** özetle.
- `docs/project-brain.md`'ye: yeni servis notu (§8'e `search-service`, port 8087, `searchdb`,
  role-model, 2 consumer), REST API haritasına satır (§8.6), ve §11 değişiklik günlüğüne kayıt ekle.
- Modül tablosundaki servis sayısını güncelle (8 → 9).

## Çalışma tarzı
- Önce `customer-service`'i şablon olarak incele, sonra dosyaları oluştur. Mevcut
  isimlendirme/idiom/yorum yoğunluğuna uy. Emin olmadığın event topic adı / alan adı /
  contact-info yapısı gibi noktaları koddan doğrula, uydurma.
