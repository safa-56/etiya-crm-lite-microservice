# Merkezi Konfigürasyon Deposu (`configs/`)

Bu klasör, tüm mikroservislerin ortam bazlı (dev / test / prod / docker)
konfigürasyonunun **tek doğruluk kaynağıdır**. `config-server`, bu reponun
Git backend'i üzerinden bu klasörü okur ve konfigürasyonu client servislere
`/{application}/{profile}` endpoint'i ile dağıtır.

## Klasör yapısı

```
configs/
  <service-name>/
    application.yml            # servisin profil bağımsız ortak ayarları
    application-dev.yml        # dev profili override'ları
    application-docker.yml     # docker/compose profili override'ları
    application-prod.yml       # prod profili override'ları
```

Örn: `configs/gateway-server/application-dev.yml`.

`config-server` şu ayarla yalnızca ilgili servisin klasörüne bakar:

```yaml
spring.cloud.config.server.git.search-paths: configs/{application}
```

Buradaki `{application}` placeholder'ı, istek yapan servisin
`spring.application.name` değerine göre çözülür.

## Önemli notlar

- **Git backend** uzak depoyu (remote) klonlar. Bu klasördeki değişikliklerin
  config server tarafından görülebilmesi için **commit + push** edilmelidir.
- **`test` profili merkezi değildir.** Testlerin ağdan bağımsız (hermetik)
  çalışması için her servisin `application-test.yml` dosyası kendi
  `src/main/resources/` dizininde yerel olarak tutulur.
- Servislerin yerel `application.yml` dosyaları yalnızca **bootstrap** bilgisini
  (`spring.application.name`, aktif profil, config server importu) içerir.
