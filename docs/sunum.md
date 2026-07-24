# Etiya CRM Lite — Mikroservis Mimarili CRM Uygulaması

### Akademi Bitirme Projesi · Sunum Dosyası

> Bu doküman hem **konuşma metni (speaker notes)** hem de **sunum akışı** olarak
> hazırlanmıştır. Slaytların sırası, her slaytta ne anlatılacağı ve hocadan
> gelebilecek sorulara hazırlık en sonda yer alır.

---

## 0. Sunumun Altın Kuralları (ekip için, sunulmaz)

Hocaya "bu öğrenciler projeye gerçekten hakim, kendileri kodlamış" hissini vermek için:

1. **Karar + gerekçe birlikte anlatılır.** "Kafka kullandık" değil → "Servisler
   arası tutarlılığı senkron çağrıyla sağlamak birbirine bağımlı (coupling) bir
   sistem üretiyordu; bu yüzden **asenkron, olay-tabanlı** iletişimi seçtik."
2. **Somut dosya/sınıf adı verilir.** `GlobalExceptionHandler`, `OrderSagaManager`,
   `CustomerSearchBusinessRules`… İsim vermek hakimiyeti kanıtlar.
3. **Terimi açıklayarak kullan.** "Choreography Saga — yani merkezi bir orkestratör
   olmadan, her servisin olayları dinleyip kendi adımını attığı dağıtık işlem deseni."
4. **Alternatifi de bildiğini göster.** "Orchestration Saga da vardı; biz merkezi
   bağımlılık istemediğimiz için choreography'yi tercih ettik."
5. Her konuşmacı **kendi bölümünü** anlatır; geçişler net olur ("Backend'i X anlattı,
   şimdi frontend'e Y devam edecek").

---

## 1. Sunum Akışı (17 slayt · ~20-25 dk)

| # | Slayt | Konuşan | Süre |
|---|-------|---------|------|
| 1 | Kapak & Ekip | Herkes | 1 dk |
| 2 | Ajanda | Sunum lideri | 1 dk |
| 3 | Proje Özeti — Etiya CRM Lite ne yapar? | Backend #1 | 1.5 dk |
| 4 | Neden Mikroservis? Mimari Genel Bakış | Backend #1 | 2 dk |
| 5 | Teknoloji Yığını (Backend + Frontend) | Backend #1 | 2 dk |
| 6 | Domain Modeli & Kullanılan Model | Backend #2 | 2 dk |
| 7 | Katmanlı Mimari + Klasör Yapısı | Backend #2 | 2 dk |
| 8 | Olay-Tabanlı Omurga: Outbox + Debezium + Inbox | Backend #2 | 2 dk |
| 9 | Choreography Saga (4 Saga) | Backend #2 | 2 dk |
| 10 | Request/Response Mantığı + DTO/MapStruct | Backend #1 | 1.5 dk |
| 11 | Validasyonlar (uçtan uca) | Backend #1 | 2 dk |
| 12 | Çok Dil Desteği (i18n, uçtan uca) | Frontend | 1.5 dk |
| 13 | Güvenlik (Keycloak / OAuth2) | Backend #1 | 1 dk |
| 14 | Frontend Mimarisi (Angular 22, Signals) | Frontend | 2 dk |
| 15 | **Yapay Zekayı Nasıl Entegre Ettik?** | Herkes | 2.5 dk |
| 16 | Demo | Frontend | 3 dk |
| 17 | Kapanış · Öğrenilenler · Sorular | Herkes | 1 dk |

---

## Slayt 1 — Kapak & Ekip

**Başlık:** Etiya CRM Lite — Mikroservis Mimarili CRM

**Alt başlık:** Spring Boot 4 · Spring Cloud · Kafka · Angular 22

- Ekip üyeleri ve rolleri (Backend / Frontend / DevOps-Infra)
- Bir cümlelik değer önermesi: *"Kurumsal bir CRM'in çekirdek akışlarını
  (müşteri → hesap → katalog → sepet → sipariş) uçtan uca, üretim-benzeri bir
  mikroservis mimarisiyle gerçekledik."*

---

## Slayt 2 — Ajanda

Sunum sırasında değineceğimiz başlıklar:

- Ne yaptık? (Proje özeti + iş akışı)
- Mimari kararlar ve **gerekçeleri**
- Teknoloji hakimiyeti (backend + frontend)
- Kalite: Validasyon, hata yönetimi, çok dil
- **Yapay zekayı geliştirme sürecine nasıl entegre ettik**
- Canlı demo

---

## Slayt 3 — Proje Özeti: Etiya CRM Lite Ne Yapar?

**Konuşma metni:**

Etiya CRM Lite, bir telekom CRM'inin çekirdek satış akışını modelleyen bir
uygulamadır. Uçtan uca iş akışı şöyle:

```
Müşteri Arama/Oluşturma → Fatura Hesabı Açma → Katalog/Kampanya Seçme
        → Sepete Ekleme → Siparişi Tamamlama → Ürün Provizyonu
```

- **10 bağımsız servis**, her biri kendi veritabanını yönetir (per-service DB).
- Gerçek kurumsal desenler: Saga, CQRS, Outbox, Inbox, BFF, API Gateway.
- Angular tabanlı bir kurumsal panel (müşteri arama, oluşturma, detay, satış).

**Vurgu:** "Amacımız sadece çalışan bir CRUD değil; **dağıtık sistemlerin gerçek
problemlerini** (tutarlılık, idempotency, servis keşfi) çözen üretim-benzeri bir
mimariydi."

---

## Slayt 4 — Neden Mikroservis? Mimari Genel Bakış

**Kullanılan mimari: Mikroservis + Olay-Tabanlı (Event-Driven) + Choreography Saga**

10 modül, 3 katmanda:

**Altyapı servisleri (business değil):**
- `config-server` (8888) — Merkezi konfigürasyon (Spring Cloud Config, Git backend)
- `eureka-server` (8761) — Servis keşfi (Netflix Eureka)
- `gateway-server` (8080) — Tek giriş noktası (Spring Cloud Gateway)

**İş servisleri (kendi DB'si + Saga):**
- `customer-service` (8081) — Müşteri, adres, iletişim
- `account-service` (8082) — Fatura hesabı
- `product-service` (8084) — Katalog, teklif, kampanya, satılan ürün
- `cart-service` (8085) — Sepet
- `order-service` (8086) — Sipariş / checkout

**Özel roller:**
- `search-service` (8087) — CQRS read-model (müşteri arama)
- `bff-service` (8099) — Backend for Frontend (veri toplama, DB'siz)

**Neden mikroservis?** *"Her domain bağımsız ölçeklenebilsin, bağımsız
deploy edilebilsin ve bir servisin şeması diğerini kırmasın diye. Bunun bedeli
dağıtık tutarlılıktır — onu da Saga + Outbox/Inbox ile çözdük."*

---

## Slayt 5 — Teknoloji Yığını (Teknoloji Hakimiyeti)

| Katman | Teknoloji | Neden |
|--------|-----------|-------|
| Dil | **Java 25** | Modern dil özellikleri (record, pattern matching) |
| Framework | **Spring Boot 4.0.0** + Spring Cloud 2025.1.0 | Kurumsal standart |
| Build | **Maven** (parent POM) | Merkezi bağımlılık yönetimi |
| Veritabanı | **PostgreSQL** (per-service DB) | `wal_level=logical` → CDC |
| Servis Keşfi | **Netflix Eureka** | İstemci taraflı load-balancing |
| Gateway | **Spring Cloud Gateway** (WebFlux) | Reaktif, tek giriş + CORS |
| Mesajlaşma | **Kafka** (KRaft) + **Spring Cloud Stream** | Asenkron, olay-tabanlı |
| Güvenilir yayın | **Transactional Outbox + Debezium (CDC)** | "Ghost event" yok |
| Idempotent tüketim | **Inbox Pattern** | Duplicate consume yok |
| Cache | **Redis** (Spring Cache) + RedisInsight | TTL bazlı |
| DTO Eşleme | **MapStruct** | Compile-time, reflection yok |
| Dokümantasyon | **springdoc-openapi** (Swagger UI) | Her serviste otomatik |
| Kimlik | **Keycloak** (OAuth2 / JWT) | Merkezi kimlik doğrulama |
| Frontend | **Angular 22** + Signals + Tailwind CSS 4 | Modern, sinyal-tabanlı |

**Konuşma metni:** Her satırda **"neden bu?"** sorusunu cevaplayın. Örn:
*"MapStruct'ı seçtik çünkü DTO ↔ entity dönüşümünü **derleme zamanında** üretiyor;
runtime reflection maliyeti ve gizli hata riski yok, üretilen kodu görebiliyoruz."*

---

## Slayt 6 — Domain Modeli & Kullanılan Model

**Kullanılan model: Party Model (kurumsal SID hizası) + per-service şema**

- **Party → PartyRole → Customer / SystemUser zinciri.** Müşteri ve sistem
  kullanıcısı doğrudan değil, ortak bir `Party` üzerinden modellendi. Aynı kişi
  hem müşterimiz (`CUST`) hem kullanıcımız (`USER`) olabilir — telekom SID
  standardıyla hizalı.
- **JPA `JOINED` kalıtım:** `Customer` (kök) ← `IndividualCustomer` (1-1 paylaşılan PK).
- **`BaseEntity` (@MappedSuperclass):** Tüm entity'ler `id, created_date,
  updated_date, deleted_date, is_active` alanlarını miras alır.
- **Soft-delete deseni:** Fiziksel silme yok; `isActive=false` + `deletedDate=now()`.
- **Katalog vs. Kampanya bilinçli farkı:** Teklif **tam bir** kataloğa (kategori)
  aittir (1-N, zorunlu); kampanya ise çok tekliften oluşan **opsiyonel paket** (N-N).

**Vurgu:** *"Alan modelini rastgele değil, telekom referans veri modeline (SID)
göre kurduk; bu yüzden `Party/PartyRole` gibi bir soyutlama var."*

---

## Slayt 7 — Katmanlı Mimari + Kullanılan Klasör Yapısı

**Her iş servisi aynı n-katmanlı (n-layered) şablonu izler.** İlk `customer-service`
referans alındı, diğerleri birebir aynı desenle kuruldu.

```
service/
├── entities/          → JPA entity'leri (+ enums, outbox, inbox)
├── dataAccess/        → Spring Data JPA repository'leri
├── business/
│   ├── abstracts/     → Servis arayüzleri (interface)
│   ├── concretes/     → Manager implementasyonları
│   ├── rules/         → İş kuralı sınıfları (BusinessRules)
│   ├── mappers/       → MapStruct mapper'ları
│   ├── constants/     → Messages (i18n anahtarları), olay sabitleri
│   ├── dtos/          → requests / responses / events
│   └── messaging/     → Kafka consumer binding'leri
├── apiController/     → REST uçları (ince katman)
└── core/             → Cross-cutting: config, exceptions, constants
```

**Neden bu yapı?**
- **Sorumlulukların ayrılması (SoC):** Controller ince, iş mantığı `business`'ta.
- **Kural katmanı ayrı:** İş kuralları `business/rules` altında ayrı `@Service`
  sınıflarında; manager'a **constructor injection** ile enjekte edilir.
- **Manager, Service'e bağımlıdır — yabancı Repository'ye değil.** (Katmanlar
  arası bağımlılık disiplini.)

---

## Slayt 8 — Olay-Tabanlı Omurga: Outbox + Debezium + Inbox

**Problem:** "DB'ye yazdım ama Kafka'ya event gitmedi" (veya tersi) → tutarsızlık.

**Çözüm — Transactional Outbox:**
1. Manager, iş verisini ve `outbox_events` kaydını **aynı transaction**'da yazar.
2. **Debezium** (CDC), PostgreSQL WAL log'unu izler, outbox tablosuna düşen
   kaydı yakalar ve `EventRouter` ile ilgili Kafka topic'ine yayınlar.
3. Sonuç: Event kaybı ("ghost event") **imkansız** — ya ikisi de commit olur ya hiçbiri.

**Inbox Pattern (idempotency):**
- `inbox_messages` tablosu (PK: `message_id`) + `InboxService.process(...)`.
- Aynı mesaj iki kez gelse bile **bir kez** işlenir → duplicate consume yok.

**Vurgu:** *"Bu iki desen olmadan olay-tabanlı sistemler sessizce bozulur.
Biz bilinçli olarak 'exactly-once benzeri' bir garanti kurduk."*

---

## Slayt 9 — Choreography Saga (Dağıtık İşlem Yönetimi)

**Kullanılan mimari deseni: Choreography-based Saga (merkezi orkestratör YOK)**

Her servis olayı dinler → kendi adımını atar → başarısızsa **telafi (compensation)** eder.

**4 Saga gerçeklendi:**

| Saga | Başlatıcı | Doğrulayıcı | Ne yapar |
|------|-----------|-------------|----------|
| Billing Account | account | customer | Hesap açarken müşteri+adresi otoriter doğrular |
| Product Sale | product | account | Ürün satışında hesabın ACTIVE olduğunu doğrular |
| Cart Item | cart | product | Sepete eklenen teklif/kampanyayı doğrular |
| Order Checkout | order | cart | Siparişte sepet içeriğini otoriter doğrular |

**Ortak desen:**
- **Asenkron tamamlanma:** Endpoint hemen döner, kayıt kısa süre `PENDING` görünür.
- **Telafi:** Doğrulama başarısızsa `CANCELLED` + soft-delete.
- **Durum bazlı idempotency:** Sonuç yalnızca kayıt hâlâ `PENDING` ise uygulanır.

**Neden choreography, orchestration değil?** *"Merkezi bir orkestratör tek nokta
bağımlılığı (single point of coupling) yaratırdı. Choreography'de servisler
gevşek bağlı kalıyor — her biri sadece olayları biliyor."*

---

## Slayt 10 — Neden Request/Response Mantığı? (DTO + MapStruct)

**Karar: Entity'ler asla API'ye sızmaz. Her uç `record` DTO alır ve döner.**

```
İstek  → CreateIndividualCustomerRequest (record)
                 ↓ MapStruct mapper
Entity → IndividualCustomer (JPA)
                 ↓ MapStruct mapper
Yanıt  ← IndividualCustomerResponse (record)
```

**Neden Request/Response ayrımı?**
1. **Güvenlik / kapsülleme:** İstemci hangi alanı yazabilir kontrol altında;
   sistem-türetilen alanlar (`id`, tarihler, `isActive`) request'te yok.
2. **API sözleşmesi stabil kalır:** Entity'yi değiştirmek API'yi kırmaz.
3. **Doğrulama noktası netleşir:** Validasyon anotasyonları request DTO'sunda.
4. **Farklı gösterimler:** Aynı entity için farklı response'lar (özet/detay).
5. **`record` seçimi:** Immutable, boilerplate yok, "value object" semantiği.

**MapStruct:** Dönüşüm **compile-time** üretilir (reflection yok). Sistem-türetilen
alanlar mapper'da `ignore=true` ile atlanır, manager'da set edilir.

---

## Slayt 11 — Validasyonlar (Uçtan Uca)

**Katman katman savunma (defense-in-depth):**

**1. Backend — Bean Validation (`@Valid`):**
```java
public record CreateContactInfoRequest(
    @Email @NotBlank @Size(max = 150) String email,
    @Size(min = 11, max = 15) @NotBlank String mobilePhone,
    ...) {}
```
- `@NotBlank`, `@Size`, `@Email`, `@Pattern` (örn. hesap no `^\d{10}$`, ad harf-maskesi).

**2. Backend — İş Kuralları (`business/rules`):**
- Format ötesi kurallar: *"Aktif ürünü olan hesap silinemez"*, *"Aynı sepet iki
  kez sipariş edilemez"* → `BusinessException` fırlatır.

**3. Merkezi Hata Yönetimi (`GlobalExceptionHandler`):**
- **RFC 7807 `ProblemDetail`** formatı. `@RestControllerAdvice`.
- `BusinessException` → 400, `@Valid` hatası → 400 (alan bazlı), beklenmeyen → 500
  (kök neden **istemciye sızdırılmaz**, sunucuda loglanır).

**4. Frontend — Signal Forms + Directive'ler:**
- `LettersOnly` / `DigitsOnly` direktifleri: harf/rakam maskesi (klavye + yapıştırma).
- Şema bazlı doğrulama (`customerDemographicSchema`), backend `@Size`/`@Pattern` ile hizalı.

**Vurgu:** *"Kurallar üç yerde birebir aynı tanımlı (frontend + 2 backend servisi)
ve biri değişince diğerleri de değişmeli — bunu bilinçli belgeledik."*

---

## Slayt 12 — Çok Dil Desteği (i18n — Uçtan Uca)

**Hem frontend hem backend Türkçe + İngilizce destekler.**

**Frontend — Sinyal tabanlı özel i18n servisi:**
```typescript
readonly t = computed(() => TRANSLATIONS[this.currentLanguage()]);
// Şablonda:  {{ t().login.title }}
```
- `LANGUAGES = ['tr', 'en']`, varsayılan `tr`.
- Dil seçimi `localStorage`'da saklanır (`etiya.language`), `<html lang>` güncellenir.
- Harici kütüphane yerine **sinyal-tabanlı** kendi çözümümüz → sıfır bağımlılık,
  tam tip güvenliği.

**Backend — `MessageSource` + `Accept-Language`:**
- Mesajlar kod içinde değil, **anahtar** olarak: `Messages.CUSTOMER_NOT_FOUND =
  "customer.notFound"`.
- Gerçek metin `messages.properties` (TR) / `messages_en.properties` (EN)
  dosyalarında.
- `GlobalExceptionHandler`, isteğin `Accept-Language` başlığına göre doğru dilde
  mesaj çözer.

**Vurgu:** *"Dil desteği sadece arayüzde değil; API hata mesajları da isteğin
diline göre dönüyor. Uçtan uca i18n."*

---

## Slayt 13 — Güvenlik (Keycloak / OAuth2)

- **Keycloak** (`etiya-crm` realm) — merkezi kimlik doğrulama, tek otorite.
- **OAuth2 Resource Server (JWT):** Gateway doğrular; kritik servisler (BFF)
  token'ı **yeniden doğrular** (defense-in-depth).
- **Token relay:** Gelen `Authorization` başlığı downstream çağrılara aynen
  taşınır → her servis çağıran **kullanıcıyla** doğrular.
- **Lazy provisioning:** Keycloak kullanıcısı, ilk istekte domain'de `SystemUser`
  olarak referanslanır (parola/rol **kopyalanmaz** — otorite Keycloak).
- Gateway CORS yalnızca `localhost:4200`; Bearer token → `allowCredentials=false`.

---

## Slayt 14 — Frontend Mimarisi (Angular 22)

**Modern, sinyal-tabanlı, standalone bileşen mimarisi.**

- **Angular 22** — standalone bileşenler (NgModule yok), **Signals** ile state.
- **Signal Forms** (`@angular/forms/signals`) — tip güvenli, şema bazlı formlar.
- **Feature-based klasör yapısı:**
```
src/app/
├── core/        → auth, config, i18n (singleton servisler)
├── features/    → auth, customers (create/detail/search), new-sale
├── layout/      → main-layout, sidebar, topbar
└── shared/      → ui (button, pagination, stepper…), directives
```
- **Tailwind CSS 4** — utility-first stil.
- Gateway (`localhost:8080`) üzerinden tek base URL şeması ile API'ye bağlanır.
- Erişilebilirlik: WCAG AA hedefi (ARIA, kontrast, focus yönetimi).

**Vurgu:** *"En güncel Angular sürümünü ve sinyal-tabanlı yeni form API'sini
kullandık — RxJS/NgModule ağırlığı olmadan reaktif ve tip güvenli."*

---

## Slayt 15 — Yapay Zekayı Nasıl Entegre Ettik? ⭐ (En Kritik Slayt)

**Hangi yapay zekayı kullandık?**
- **Claude (Anthropic) — Claude Code / Opus.** Bir **geliştirme ajanı** (AI agent)
  olarak, kod editörümüze entegre şekilde.

**Neden Claude?**
1. **Geniş bağlam penceresi** — 10 servisli bir mono-repoyu bir bütün olarak
   "görebiliyor"; mimari kararları tüm servislere tutarlı uygulayabiliyor.
2. **Güçlü mimari muhakeme** — Saga, Outbox, CQRS gibi ileri desenleri doğru
   uygulayabilecek düzeyde.
3. **Referans-şablon disiplini** — "customer-service'i birebir örnek al" gibi
   talimatlara sadık kalıyor; kod tabanı homojen kalıyor.

**Nasıl entegre ettik? (Süreç)**
- AI'yı **kod yazdıran değil, kararları biz verip uygulatan** bir eş-programcı
  (pair programmer) olarak kullandık.
- Her yeni servis için **yapılandırılmış prompt** hazırladık
  (`docs/search-service-prompt.md`): "Önce şunu oku, şu şablona birebir uy,
  şu kuralları çiğneme."
- Ürettiği kodu **biz gözden geçirip** (code review) mimariye uydurduk.

### Agent'ın Yaptıklarımızı Unutmaması İçin Ne Yaptık? ⭐

Bu, projedeki en özgün mühendislik kararlarından biri:

1. **`docs/project-brain.md` — "Projenin Canlı Hafızası".**
   - Tüm mimari kararlar, gerekçeleri, standartlar, tuzaklar ve değişiklik günlüğü
     tek dosyada. Proje ilerledikçe güncellenir.
   - AI her yeni göreve başlarken **önce bunu okur** → önceki kararları "hatırlar",
     tutarsız kod üretmez.
2. **`CLAUDE.md` — kodlama konvansiyonları.** (Frontend için Angular/TS best-practice
   kuralları; AI her oturumda bunlara uyar.)
3. **Kalıcı hafıza (memory) dosyaları** — tekrar eden kurallar (örn. "Manager,
   yabancı Repository'ye değil Service'e bağımlıdır") ayrı hatırlanır.

**Vurgu:** *"AI'nın en büyük zayıflığı oturumlar arası unutkanlıktır. Biz bunu
`project-brain.md` ile çözdük: proje kendi hafızasını taşıyor. Bu sayede 10 servis
de birbirinin aynısı desende, tutarlı çıktı."*

---

## Slayt 16 — Demo

**Canlı demo senaryosu (sıra önemli):**
1. **Giriş** — Keycloak ile login (dil değiştir: TR ↔ EN göster).
2. **Müşteri Arama** — FR-002: TC/ad/hesap no ile arama (validasyonu göster:
   ada rakam girmeyi dene → engellenir).
3. **Müşteri Oluştur** — çok adımlı form (Signal Forms), validasyon hataları.
4. **Müşteri Detay** — BFF ile tek çağrıda müşteri + fatura hesapları.
5. **(Opsiyonel) Swagger UI** — bir servisin otomatik API dokümanı.
6. **(Opsiyonel) Kafka UI / RedisInsight** — olay akışını/cache'i göster.

> **Yedek plan:** Canlı demo riskli olursa önceden **ekran kaydı** hazır bulundurun.

---

## Slayt 17 — Kapanış · Öğrenilenler · Sorular

**Neyi başardık:**
- Üretim-benzeri, 10 servisli, olay-tabanlı bir mikroservis mimarisi.
- Dağıtık tutarlılığı Saga + Outbox/Inbox ile çözdük.
- Uçtan uca kalite: validasyon, çok dil, merkezi hata yönetimi, güvenlik.
- AI'yı disiplinli bir süreçle (project-brain hafızası) entegre ettik.

**Öğrenilenler / Zorluklar:**
- Dağıtık tutarlılık senkron çağrıdan çok daha zor — asenkron düşünmeyi öğrendik.
- "Ghost event" ve "duplicate consume" gibi görünmez hataların farkına vardık.
- Config'in git backend'den okunması → commit+push disiplini gerektirdi.

**Teşekkürler — Sorular?**

---

## EK — Hocadan Gelebilecek Sorulara Hazırlık (Q&A)

> Cevaplar **kısa "kanca cümle" + gerekçe + somut sınıf/dosya adı** biçiminde
> yazıldı. Panikte tek satır kancayı söyleyip gerekçeyle açın; isim vermek
> hakimiyeti kanıtlar. Her cevabın sonundaki *"Somut:"* satırı, hocaya
> gösterebileceğiniz gerçek sınıf/dosyayı işaret eder.

### A) Mimari & Dağıtık Sistemler

**S: Choreography Saga yerine neden orchestration kullanmadınız?**
C: Orchestration merkezi bir **orkestratör servisi** gerektirir — tüm akışları o
bilir, herkes ona bağımlı olur. Bu, tam da mikroservislerde kaçınmak istediğimiz
**tek nokta bağımlılığı (single point of coupling/failure)** demektir. Bizim
akışlarımız 2 servis arası ve görece basit; bu yüzden servisleri gevşek bağlı
tutan **choreography**'yi seçtik: her servis yalnızca olayları bilir, merkez yok.
Trade-off'un farkındayız — akış sayısı ve adım derinliği artsaydı (5-6 servisli,
uzun zincirli işlemler) izlenebilirlik için orchestration'a geçmek mantıklı olurdu.
*Somut:* `OrderSagaManager`, `CartSagaManager`, `ProductSagaManager` — her biri
kendi servisinde, ortak orkestratör yok.

**S: Bir event iki kez işlenirse (duplicate) ne olur?**
C: Hiçbir yan etki olmaz — **iki katmanlı** koruma var. (1) **Inbox Pattern**:
`inbox_messages` tablosunda PK `message_id`; aynı mesaj tekrar gelirse
`InboxService.process(...)` onu atlar. (2) **Durum-bazlı idempotency**: saga sonucu
yalnızca ilgili kayıt hâlâ `PENDING` durumundaysa uygulanır; kayıt zaten
`ACTIVE`/`CANCELLED` olduysa sonuç sessizce yok sayılır. Kafka'nın "at-least-once"
teslimatı gerçeğiyle bilinçli olarak böyle tasarladık.
*Somut:* `InboxService.process`, `message_id` PK'si, manager'daki `applyValidationResult`.

**S: Outbox olmadan neden Kafka'ya direkt yazmadınız?**
C: Buna **"dual-write" problemi** denir: iki ayrı sisteme (DB + Kafka) yazmak
atomik değildir. DB commit olup Kafka publish başarısız olursa "ghost event" kaybı;
tersi olursa hayalet event oluşur — her iki durumda da tutarsızlık. **Transactional
Outbox** ile event'i normal bir DB satırı (`outbox_events`) olarak **aynı
transaction'da** yazıyoruz; sonra **Debezium (CDC)** PostgreSQL WAL log'undan bu
satırı yakalayıp Kafka'ya taşıyor. Böylece "ya ikisi de olur ya hiçbiri" garantisi
DB transaction'ının kendisinden geliyor.
*Somut:* `outbox_events` tablosu, `infra/debezium/register-*-connector.json`,
EventRouter SMT → `crm.<Aggregate>.events`.

**S: Neden Debezium? Uygulama içinden bir scheduler ile outbox'ı okuyup Kafka'ya yazsanız olmaz mıydı (polling publisher)?**
C: Olurdu, o da geçerli bir Outbox varyantıdır ama iki dezavantajı var: (1) periyodik
polling **gecikme** ekler ve boşa DB sorgusu üretir; (2) publish + "işlendi işaretle"
adımını uygulama içinde yeniden atomik yapmak gerekir. Debezium **log-tabanlı CDC**
olduğu için gerçek-zamanlıya yakın, uygulamaya ek kod yükü bindirmeden ve DB'yi
yormadan çalışır. Bedeli bir altyapı bileşeni (Kafka Connect) işletmek — biz bunu
kabul ettik.

**S: Her servisin ayrı DB'si performans/join sorunu yaratmıyor mu?**
C: Cross-service join **bilinçli olarak yapmıyoruz** — zaten mikroservis sınırını
bozardı. İki farklı ihtiyaca iki farklı çözüm koyduk: (1) Bir ekran birden çok
servisin verisini **anlık** birleştirecekse → **BFF** senkron toplar
(`CustomerDetailAggregator`). (2) Sık ve karmaşık **arama** gerekiyorsa → **CQRS
read-model**: olayları dinleyip tek denormalize tabloda tutuyoruz
(`search-service`), böylece okuma yükü yazma servislerini hiç etkilemiyor.
Veri sahipliği tek serviste kaldığı için tutarlılık da net.

**S: search-service neden ayrı bir servis? customer-service içinde arama yapılamaz mıydı?**
C: FR-002 araması tek servisin verisi değil — müşteri (customer) + hesap/sipariş
numaraları (account) birleşiyor ve tek ekranda, çok kriterli (TC, GSM, hesap no…)
sorgulanıyor. Bunu yazma servisinin içine koysak: (1) customer-service'e ait
olmayan veriyi oraya taşımak gerekirdi, (2) ağır arama sorguları CRUD performansını
etkilerdi. **CQRS** ile okuma ve yazmayı ayırdık: `search-service` sadece olay
tüketen, denormalize `CustomerSearchIndex` tutan bir **read-model**; kendi
outbox'ı/Debezium connector'ı bile yok, saga'ya katılmıyor.
*Somut:* `CustomerSearchIndex`, dinamik JPA Specification, `GET /api/v1/search/customers`.

**S: BFF ile Gateway arasındaki fark ne? İkisi de "arada" duruyor.**
C: Gateway **teknik** bir katman ("dumb pipe"): routing, `StripPrefix`, CORS,
JWT doğrulama yapar; iş mantığı bilmez. **BFF** ise **iş odaklı** bir katman:
belirli bir frontend ekranı (müşteri detayı) için birden çok servisten (customer +
account) veri çekip **tek yanıtta** birleştirir. Yani Gateway "isteği doğru
servise ulaştırır", BFF "birden çok servisin cevabını ekranın ihtiyacına göre
harmanlar". BFF'in DB'si/Kafka'sı yoktur, durumsuzdur.
*Somut:* `bff-service` (port 8099), `CustomerDetailAggregator`, `RestClient` + Eureka lb.

### B) Kod Kalitesi & Tasarım Kararları

**S: MapStruct yerine neden ModelMapper (ya da elle mapping) kullanmadınız?**
C: **MapStruct compile-time** çalışır: mapper kodunu derleme sırasında **üretir**,
üretilen `.java` dosyasını açıp görebiliyoruz, runtime reflection yok → hızlı ve
öngörülebilir. En önemlisi: eşleşmeyen bir alan olursa **derlemede** uyarı/hata
alırız, canlıda değil. ModelMapper runtime reflection kullanır — daha yavaş ve
hatalar geç fark edilir. Elle mapping ise boilerplate ve insan hatası demek.
*Somut:* `business/mappers/*Mapper` (`componentModel = "spring"`,
`unmappedTargetPolicy = ReportingPolicy.IGNORE`), türetilen alanlar `ignore = true`.

**S: Neden Request/Response DTO'ları? Entity'yi direkt döndürseniz daha az kod olmaz mıydı?**
C: Kısa vadede evet, ama entity'yi API'ye açmak birçok soruna yol açar: (1) İstemci
`id`, `createdDate`, `isActive` gibi **sistem alanlarını** göndermeye çalışabilir —
DTO'da bu alanlar yok, yüzey daralıyor. (2) Entity'yi değiştirdiğimizde **API
sözleşmesi kırılır**; DTO araya girince entity özgürce evrilebiliyor. (3) JPA lazy
ilişkileri serileştirmede patlayabilir. (4) Validasyon ve API dokümanı için doğru
yer request DTO'su. `record` seçtik çünkü immutable, boilerplate'siz, "value object"
semantiği taşıyor.
*Somut:* `business/dtos/requests` ve `.../responses` altındaki `record`'lar.

**S: İş kurallarını neden Manager'ın içine yazmadınız da ayrı `rules` sınıflarına aldınız?**
C: **Tek sorumluluk** için. Manager akışı yönetir (orkestrasyon); iş kuralı ("aktif
ürünü olan hesap silinemez") ayrı bir `@Service` kural sınıfında durur ve manager'a
**constructor injection** ile girer. Böylece kurallar tek yerde toplanır, test
edilebilir, tekrar kullanılabilir ve manager şişmez. İhlalde `BusinessException`
fırlatılır, mesaj `Messages` sabitlerinden gelir — magic string yok.
*Somut:* `business/rules/*BusinessRules`, `OrderBusinessRules.checkIfCartNotAlreadyOrdered`.

**S: "Manager, Service'e bağımlıdır; yabancı Repository'ye değil" derken ne kastediyorsunuz?**
C: Bir servisin manager'ı, başka bir domain'in verisine ihtiyaç duyduğunda **o
domain'in repository'sini doğrudan çağırmaz**; onun servis (Manager/abstract)
arayüzünü kullanır. Böylece o domain'in iş kuralları/validasyonu atlanmaz ve
katmanlar arası bağımlılık disiplini korunur. Bu bizim tekrar tekrar uyguladığımız
bir konvansiyon ve `project-brain`'de de kayıtlı.

### C) Validasyon, Çok Dil, Hata Yönetimi

**S: Validasyonu hem frontend hem backend'de yapmak tekrar (kod tekrarı) değil mi?**
C: Bilinçli bir tekrar — çünkü ikisi farklı işe yarıyor. **Frontend** validasyonu
**kullanıcı deneyimi** içindir (anında geri bildirim, gereksiz istek atmamak);
ama tarayıcı devre dışı bırakılabilir/atlanabilir, bu yüzden **asla güvenlik
sınırı değildir**. **Backend** validasyonu **otoritedir** — API'ye kim çağrı
yaparsa yapsın orada durur. Kuralı iki yerde tutmanın riskini de yönettik: ad
harf-maskesi gibi kritik kurallar üç yerde **birebir aynı** tanımlı ve
`project-brain`'de "biri değişirse hepsi değişmeli" diye not düştük.
*Somut:* frontend `NAME_CHARACTERS`, customer `ValidationPatterns.NAME_PATTERN`,
search `CustomerSearchBusinessRules.NAME_PATTERN`.

**S: `@Valid` (format) ile iş kuralı validasyonu arasındaki fark ne?**
C: `@Valid`/Bean Validation **alan formatını** doğrular: boş mu, uzunluk, regex,
e-posta (`@NotBlank`, `@Size`, `@Pattern`). Bu, request DTO'suna bakar ve tek
kaydın kendi içinde doğruluğudur. **İş kuralı** ise **domain durumuna/başka
verilere** bakar: "bu hesabın aktif ürünü var mı", "bu sepet zaten sipariş edildi
mi" — bunları veritabanına bakmadan bilemezsiniz, bu yüzden `business/rules`'ta.
İkisi de sonunda 400 döner ama farklı katmanlarda ve farklı gerekçelerle.

**S: Hata yönetimini nasıl kurguladınız? İstemci ham exception görüyor mu?**
C: Hayır. Her serviste merkezi bir `@RestControllerAdvice`
(`GlobalExceptionHandler`) var ve yanıtları **RFC 7807 `ProblemDetail`** standardında
döndürüyor: `BusinessException` → 400, `@Valid` hatası → 400 (alan bazlı detayla),
beklenmeyen `Exception` → 500. Kritik nokta: 500'de **kök neden istemciye
sızdırılmaz** (güvenlik), sunucuda loglanır. Böylece istemci her zaman tutarlı,
öngörülebilir ve dile duyarlı bir hata gövdesi alır.
*Somut:* `core/crosscutting/exceptions/GlobalExceptionHandler`.

**S: Çok dil desteğini nasıl kurdunuz? Neden hazır bir i18n kütüphanesi (ngx-translate) kullanmadınız?**
C: **Uçtan uca** kurduk. Frontend'de Angular'ın yeni **signal** API'siyle kendi
`I18nService`'imizi yazdık: `t = computed(() => TRANSLATIONS[lang()])`, şablonda
`{{ t().login.title }}`. Hazır kütüphane yerine bunu seçtik çünkü: sıfır ekstra
bağımlılık, **tam tip güvenliği** (sözlük TypeScript nesnesi, yanlış anahtar
derlemede yakalanır) ve sinyal-tabanlı reaktivite. Backend'de ise mesajlar kodda
değil, **anahtar**: `messages.properties`/`messages_en.properties`; isteğin
`Accept-Language` başlığına göre `MessageSource` doğru dilde çözer. Yani sadece
arayüz değil, **API hata mesajları da** kullanıcının dilinde dönüyor.
*Somut:* `core/i18n/i18n.service.ts` + `translations.ts`; backend `Messages` sabitleri.

**S: Sayfalama (pagination) nasıl? Spring geçersiz `page`/`size`'ı ne yapıyor?**
C: Liste uçları `Pageable` alıp `PagedResponse<T>` döner (content + sayfa meta
bilgisi). Bir tuzak fark ettik: Spring'in `Pageable` çözücüsü **geçersiz** `page`/
`size` değerlerini sessizce sınırlıyor (hata vermiyor). Kullanıcıya net hata dönmek
için search-service'te ham `page`/`size` parametrelerini `@RequestParam` ile ayrıca
alıp, Spring çözmeden **önce** iş kuralıyla doğruluyoruz (`page<0` veya `size`
[1..2000] dışıysa 400).
*Somut:* `CustomerSearchBusinessRules.validatePagination`, `MAX_PAGE_SIZE = 2000`.

### D) Yapay Zeka & Süreç

**S: Kodu AI yazdıysa siz ne yaptınız? Bu sizin projeniz mi?**
C: Evet, çünkü **mimari kararların hepsi bizim**: servis sınırları, hangi desen
(Saga mı senkron mu), domain modeli (Party/PartyRole), tutarlılık stratejisi.
AI'yı bir **eş-programcı** gibi kullandık — biz "şu şablona birebir uy, şu kuralı
çiğneme" diye yönlendirdik, o üretti, biz **her kodu review edip** mimariye
oturttuk. Asıl mühendislik ürünümüz görünmeyen kısımda: `project-brain.md`
(mimari hafıza) ve yapılandırılmış prompt'lar (`search-service-prompt.md`). Bir
şablonu 10 servise **tutarlı** uygulayabilmek, projeye hakim olmadan mümkün değil.

**S: "Agent'ın unutmaması için ne yaptınız" derken tam olarak ne yaptınız?**
C: AI ajanlarının en büyük zayıflığı **oturumlar arası unutkanlık** — her yeni
oturum sıfırdan başlar ve önceki kararları bilmez, bu da tutarsız kod üretir. Biz
buna **kalıcı, dosya-tabanlı bir hafıza** kurduk: (1) `docs/project-brain.md` —
tüm mimari kararlar, gerekçeler, standartlar, bilinen tuzaklar ve tam bir değişiklik
günlüğü; AI her göreve **önce bunu okuyarak** başlar. (2) `CLAUDE.md` — kodlama
konvansiyonları (örn. Angular/TS kuralları), her oturumda otomatik uygulanır.
(3) Tekrar eden özel kurallar için ayrı memory dosyaları. Sonuç: proje kendi
hafızasını taşıyor, bu yüzden 10 servis de birbirinin aynısı desende çıktı.

**S: Neden Claude'u seçtiniz, başka bir modeli değil?**
C: Üç pratik sebep: (1) **Geniş bağlam penceresi** — 10 servisli mono-repoyu bir
bütün olarak değerlendirip kararı tüm servislere tutarlı uygulayabiliyor. (2)
**Mimari muhakeme** — Saga, Outbox, CQRS gibi ileri desenleri doğru kurgulayacak
seviyede. (3) **Talimat/şablon disiplini** — "customer-service'i birebir örnek al"
gibi kısıtlara sadık kalıyor, kod tabanı homojen kalıyor. Bizim iş akışımız
"referans şablonu tekrar et" olduğu için bu disiplin belirleyiciydi.

### E) Teknoloji Seçimleri

**S: Neden senkron REST yerine bunca yeri Kafka ile asenkron yaptınız? Daha karmaşık değil mi?**
C: Karmaşık, evet — ama bilinçli bir takas. Servisler arası **senkron** çağrı
zinciri kursaydık: (1) servisler birbirine **runtime bağımlı** olurdu (biri düşünce
diğeri de düşer), (2) gecikmeler birikirdi, (3) dağıtık transaction (2PC) gerekir,
o da kırılgandır. Asenkron + Saga ile servisler **birbirini beklemeden** çalışıyor,
biri geçici düşse mesaj Kafka'da bekliyor (dayanıklılık), sonuç geldiğinde
işleniyor. CRUD projesi olsaydı gereksiz olurdu; ama amacımız gerçek dağıtık
sistem problemlerini çözmekti.

**S: Redis cache'i nasıl ve nerede kullanıyorsunuz? Tutarlılığı nasıl koruyorsunuz?**
C: Spring Cache soyutlamasıyla, manager metodlarında `@Cacheable`/`@CacheEvict`/
`@CachePut`. Tekil kayıtlar ~5-10 dk, listeler ~2 dk TTL. **Tutarlılık**: veri
değişince ilgili cache anahtarı `@CacheEvict` ile boşaltılıyor, böylece bayat veri
dönmüyor. JSON serileştirme tip bilgisiyle yapılıyor; RedisInsight ile izliyoruz.
*Somut:* `core/config/RedisCacheConfig`, `CacheNames` sabitleri.

**S: Konfigürasyonu neden merkezileştirdiniz (Config Server)? Her serviste `application.yml` olsa olmaz mıydı?**
C: 10 servisin ortam ayarlarını (dev/docker/prod) tek yerden yönetmek için. Config
Server **Git backend** kullanıyor — konfigürasyonlar da versiyonlanıyor. Servisin
yerel `application.yml`'i yalnızca bootstrap bilgisi (isim, aktif profil, config
importu) taşır; gerisi merkezde. Bir tuzağı da öğrendik: Git backend uzak depoyu
okuduğu için `configs/` değişiklikleri **commit + push** edilmeden servisler
göremiyor. Hermetik testler için `test` profili bilinçli olarak yerelde kaldı.

**S: Neden Java 25 ve Spring Boot 4 gibi çok yeni sürümler? Risk değil mi?**
C: Akademi projesi olduğu için en güncel kurumsal yığını öğrenmek istedik — record'lar,
pattern matching, Spring'in yeni `RestClient`'ı ve Signal-tabanlı Angular gibi
modern araçları gerçek bir mimaride kullanmak asıl kazanımdı. Üretim kritikliği
olmadığından yeni sürüm riskini bilinçli aldık; karşılığında güncel ekosistem
hakimiyeti kazandık.

### F) Zor / Kurcalayıcı Sorular

**S: Kullanıcı bir hesap oluşturdu ama saga henüz tamamlanmadı — ekranda ne görür?**
C: Kayıt kısa süre **`PENDING`** durumunda görünür; başlatan endpoint hemen döner
(asenkron tamamlanma). Saga doğrulaması bittiğinde `ACTIVE` olur ya da başarısızsa
telafi ile `CANCELLED` + soft-delete. Bu, "eventual consistency"nin bilinçli ve
görünür bir sonucu — kullanıcıya sahte bir "anında başarılı" göstermiyoruz.

**S: İki kullanıcı aynı anda aynı hesap numarasını alırsa (race condition)?**
C: Son savunma **veritabanı unique kısıtı**. Uygulama önce `existsByAccountNumber`
ile kontrol eder ama iki istek aynı anda geçebilir; o durumda DB'deki unique
constraint devreye girer ve ikinci yazma reddedilir. Hesap numarası kalıcı iş
kimliği olduğu için kontrol soft-delete edilmiş kayıtları da kapsar (iptal edilmiş
bir numara yeniden verilemez).

**S: Kampanya fiyatı liste toplamından yüksek olabilir mi? Bunu engelliyor musunuz?**
C: Hayır, bilinçli olarak engellemiyoruz — sadece `savings` (indirim) hesaplayıp
gösteriyoruz. Zorladığımız tek kural: paketin en az bir **var olan/aktif** teklif
içermesi ve aynı teklifin pakette tekrarlanmaması. Fiyatlandırma bir iş kararıdır,
teknik kısıt değil; bunu iş analiziyle örtüşecek şekilde açık bıraktık.

**S: Testleri nasıl çalıştırıyorsunuz? Kafka/DB olmadan test geçiyor mu?**
C: Evet — `test` profili **hermetik**: H2 in-memory DB, Kafka/Eureka kapalı
(`app.kafka.enabled=false`), config server'a gitmez. Böylece testler ağdan bağımsız,
tekrarlanabilir çalışır. Bir tuzağı da çözdük: `data.sql`'in test profilinde
Hibernate şemayı kurmadan çalışıp "tablo yok" hatası vermemesi için
`spring.sql.init.mode: never` ekledik.

---

### Son söz (kapanışta söylenebilir)
> "Bu projede asıl öğrendiğimiz şey teknolojilerin adları değil, **her kararın bir
> bedeli olduğu** ve o bedeli bilerek seçim yapmak oldu. Senkron mu asenkron,
> orchestration mı choreography, cache mi tutarlılık — hepsinde takası tartışıp
> gerekçeli karar verdik ve bunu `project-brain` ile belgeledik."
