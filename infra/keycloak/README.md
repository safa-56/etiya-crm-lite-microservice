# Keycloak - Kimlik Doğrulama ve Yetkilendirme

Etiya CRM Lite için kimlik doğrulama/yetki sunucusu **Keycloak** ile sağlanır.
Container olarak `infra/docker-compose.yml` içinde çalışır ve açılışta `etiya-crm`
realm'ini otomatik import eder.

## Bileşenler

| Dosya / Dizin | Amaç |
|---|---|
| `import/etiya-crm-realm.json` | Açılışta import edilen realm tanımı (tüm kabul kriterleri burada). |
| `themes/etiya/login/` | Özel login teması: alan bazında `maxlength` ve kullanıcı adı `trim`. |
| `../postgres/init/01-create-databases.sql` | `keycloakdb` veritabanını oluşturur. |
| `../docker-compose.yml` (`keycloak` servisi) | Container tanımı, DB bağlantısı, volume mount'ları. |

## Ayağa Kaldırma

```bash
# infra dizininden
docker compose -f docker-compose.yml up -d keycloak
```

- **Admin konsolu:** http://localhost:8180  (kullanıcı: `admin`, parola: `admin` — `.env` ile değiştirilebilir)
- **Realm issuer:** http://localhost:8180/realms/etiya-crm
- **OpenID keşif:** http://localhost:8180/realms/etiya-crm/.well-known/openid-configuration

### Örnek kullanıcılar (realm import ile gelir)

| Kullanıcı | Parola | Roller |
|---|---|---|
| `demo` | `Password123` | `crm_user` |
| `admin-crm` | `Password123` | `crm_user`, `crm_admin` |

### Test amaçlı token alma (Direct Access Grant)

```bash
curl -s -X POST \
  http://localhost:8180/realms/etiya-crm/protocol/openid-connect/token \
  -d "client_id=crm-app" \
  -d "grant_type=password" \
  -d "username=demo" \
  -d "password=Password123"
```

Dönen `access_token` gateway'e `Authorization: Bearer <token>` header'ı ile gönderilir.

## Kabul Kriterleri Eşlemesi

| # | Kriter | Nerede karşılanır |
|---|---|---|
| **1** | Kullanıcı adı en fazla 50 karakter; fazlası girişe alınmaz | **Sunucu:** realm `components → declarative-user-profile` → `username.validations.length.max = 50`. **Girdi (UI):** `themes/etiya` teması login alanına `maxlength=50` uygular; kayıt/hesap formlarında user profile aynı `maxlength`'i otomatik üretir. |
| **2** | Kullanıcı adının baş/son boşlukları otomatik temizlenir | **UI:** `themes/etiya/login/resources/js/field-limits.js` form gönderiminde `username.value.trim()` uygular. |
| **3** | Kullanıcı adı doğrulaması büyük/küçük harf **duyarsız** | Keycloak kullanıcı adlarını küçük harfe normalize ederek saklar; giriş varsayılan olarak case-insensitive'dir (ör. `Demo`, `DEMO`, `demo` aynı hesaba düşer). |
| **4** | Şifre en fazla 50 karakter; fazlası girişe alınmaz | **Sunucu:** realm `passwordPolicy = "length(8) and maxLength(50)"`. **Girdi (UI):** `etiya` teması şifre alanına `maxlength=50` uygular. |
| **5** | 5 başarısız denemede hesap 15 dk kilitlenir | realm brute-force ayarları: `bruteForceProtected=true`, `failureFactor=5`, `waitIncrementSeconds=900`, `maxFailureWaitSeconds=900`, `permanentLockout=false`. |
| **6** | Kullanıcı 8 saat işlem yapmazsa yeniden giriş gerekir | realm `ssoSessionIdleTimeout = 28800` (8 saat). |

> Not: 1 ve 4'te iki katman birlikte çalışır. Tarayıcı `maxlength`'i UX içindir
> (fazla karakteri fiziksel olarak engeller); sunucu tarafı politika ise kötü niyetli
> istekleri (temayı atlayanları) reddeder.

## Gateway Entegrasyonu (Resource Server)

`gateway-server`, `spring-boot-starter-oauth2-resource-server` ile JWT doğrulaması
yapar (bkz. `gateway-server/.../config/SecurityConfig.java`). issuer-uri:

- **Host (dev):** `http://localhost:8180/realms/etiya-crm` (`configs/gateway-server/application.yml`)
- **Docker profili:** `http://keycloak:8080/realms/etiya-crm` (`configs/gateway-server/application-docker.yml`)

Actuator health/info ve OpenAPI uçları public'tir; diğer tüm `/api/v1/**` istekleri
geçerli bir Bearer token ister. Keycloak realm/client rolleri `ROLE_*` authority'lerine
map'lenir (ileride route/metot bazlı yetkilendirme için).

> **config-server uyarısı:** Gateway konfigürasyonu git backend'li config-server'dan
> çekildiği için `configs/gateway-server/**` değişiklikleri container'ların görebilmesi
> adına **commit + push** edilmelidir.

## Üretim Notları

- `start-dev` yalnızca yerel geliştirme içindir (HTTP, esnek hostname, cache=local).
  Üretimde `start` komutu, HTTPS, sabit `KC_HOSTNAME` ve harici/replike DB kullanın.
- Bootstrap admin parolasını ve `crm-app` istemcisinin redirect/web-origin
  değerlerini üretim için mutlaka sıkılaştırın (`*` yerine gerçek origin'ler).
