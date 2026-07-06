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

## 7. Açık Sorular / Yapılacaklar

- [x] Servis keşfi: **Netflix Eureka** (`eureka-server`) + API Gateway
  (`gateway-server`, Spring Cloud Gateway).
- [x] Merkezi konfigürasyon: **Spring Cloud Config** (`config-server`), Git
  backend + kök `configs/` klasörü. Bkz. §4.1.
- [ ] Veritabanı teknolojisi (per-service DB?) belirlenecek.
- [ ] İlk somut servisin (örn. `customer-service`) oluşturulması.
- [ ] Observability (Actuator, Micrometer, tracing) standardı.

## 8. Değişiklik Günlüğü

- **2026-07-06:** Proje başlatıldı. Parent POM (`etiya.com:crm-lite`) ve bu
  project-brain dökümanı oluşturuldu.
- **2026-07-06:** `eureka-server` ve `gateway-server` eklendi (4 profil:
  dev/test/prod/docker), Dockerfile'lar oluşturuldu.
- **2026-07-06:** `config-server` (Spring Cloud Config) eklendi. Tüm servisler
  `spring.config.import` ile merkezi config'e bağlandı; ortam bazlı ayarlar kök
  `configs/<service>/` klasörüne taşındı (`test` profili yerel kaldı). Bkz. §4.1.
