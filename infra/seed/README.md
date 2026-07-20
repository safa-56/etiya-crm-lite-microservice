# Test Seed Verisi

3 servisin veritabanını (`customerdb`, `accountdb`, `productdb`) uçtan uca test
senaryoları için tutarlı, deterministik veriyle doldurur.

## İki yol var

1. **Otomatik (önerilen — repoyu pull edip çalıştıran herkeste kendiliğinden gelir).**
   Her serviste `src/main/resources/data.sql` vardır. Servis **`dev` profilinde**
   açıldığında Hibernate şemayı kurar, ardından Spring bu betiği çalıştırıp DB'yi
   **idempotent** (`ON CONFLICT (id) DO NOTHING`) şekilde tohumlar. Yani arkadaşların
   sadece servisleri başlatır; veri otomatik oluşur, ekstra adım yok. Tekrar
   başlatmak zarar vermez (var olanı tekrar basmaz).

   Aktifleştiren ayarlar her servisin yerel `application.yml`'inde (dev dökümanı):
   `spring.sql.init.mode=always` + `spring.jpa.defer-datasource-initialization=true`.
   **prod/test** profilleri bunu almaz.

2. **Manuel (aşağıdaki `.sql` dosyaları) — tam sıfırlama içindir.** `infra/seed/`
   altındaki script'ler `TRUNCATE ... RESTART IDENTITY CASCADE` ile önce temizler,
   sonra yükler. DB'yi baştan temiz bir seed'e döndürmek istediğinde kullan.

## Ön koşullar

1. Altyapı ayakta olmalı: `docker compose -f infra/docker-compose.yml up -d`
2. **Şema oluşmuş olmalı.** Tablolar Hibernate `ddl-auto: update` ile servis
   ilk açıldığında yaratılır. Bu yüzden seed'i çalıştırmadan önce üç servisi
   (customer/account/product) **en az bir kez başlatın** ki tablolar var olsun.
3. Postgres tek container'dır (`crm-postgres`), üç veritabanı da onun içinde.

## Çalıştırma

### Windows (PowerShell)
```powershell
./infra/seed/run-seed.ps1
```

### Linux/macOS/Git Bash
```bash
bash infra/seed/run-seed.sh
```

### Elle (tek tek)
```bash
docker exec -i crm-postgres psql -U postgres -d customerdb < infra/seed/01_customer_seed.sql
docker exec -i crm-postgres psql -U postgres -d accountdb  < infra/seed/02_account_seed.sql
docker exec -i crm-postgres psql -U postgres -d productdb  < infra/seed/03_product_seed.sql
```

> Script'ler idempotenttir: her tabloyu `TRUNCATE ... RESTART IDENTITY CASCADE`
> ile temizler, sabit id'lerle yeniden yükler ve identity sekanslarını en büyük
> id'ye çeker (uygulamanın sonraki insert'leri çakışmaz). Outbox/Inbox tablolarına
> dokunulmaz.

## Kapsanan senaryolar

### customerdb — 6 bireysel müşteri
| id | müşteri | cinsiyet | adres | kontakt |
|----|---------|----------|-------|---------|
| 1 | Ahmet Yılmaz | MALE | 2 adres (İstanbul birincil, Ankara ikincil) | ev+cep |
| 2 | Ayşe Demir | FEMALE | 1 adres (İzmir) | sadece cep |
| 3 | Mehmet Ali Kaya | MALE | 2 adres (Bursa, Antalya) | ev+cep+faks |
| 4 | Fatma Şahin | FEMALE | 1 adres (İstanbul) | sadece cep |
| 5 | Can Öztürk | MALE | 1 adres (Ankara) | sadece cep |
| 6 | Zeynep Nur Aydın | FEMALE | 1 adres (Konya) | ev+cep — **hesabı yok** |

### accountdb — 7 fatura hesabı (tüm durum senaryoları)
| id | müşteri | durum | aktif ürün | not |
|----|---------|-------|-----------|-----|
| 1 | 1 | ACTIVE | 2 | Ev+TV kampanyası satılmış |
| 2 | 1 | ACTIVE | 1 | aynı müşterinin ikinci hesabı |
| 3 | 2 | ACTIVE | 1 | tekil Superbox |
| 4 | 3 | ACTIVE | 0 | ürünsüz → **silinebilir** senaryosu |
| 5 | 4 | PENDING | 0 | oluşturma saga'sı sürüyor |
| 6 | 5 | PASSIVE | 0 | soft-delete edilmiş (is_active=false) |
| 7 | 2 | CANCELLED | 0 | saga telafisi (doğrulama başarısız) |

### productdb — katalog / teklif / kampanya / satılmış ürün
- **5 katalog** (kategori): Ev İnterneti, Mobil, Superbox, TV, Sabit Hat.
- **10 teklif**, her biri **tam olarak bir kataloğa** bağlı (teklif 10 hiçbir
  kampanyada değil; yalnızca katalogtan bulunur).
- **3 kampanya (paket)** — hepsinde `campaign_price < Σ liste` (indirim):
  | kampanya | teklifler | liste toplamı | paket fiyatı | indirim |
  |----------|-----------|---------------|--------------|---------|
  | Ev + TV Paketi | 1, 7 | 449.80 | 399.90 | 49.90 |
  | Full Ev Eğlence | 2, 8, 9 | 949.70 | 749.90 | 199.80 |
  | Mobil + Superbox | 3, 5 | 599.80 | 499.90 | 99.90 |
- **6 satılmış ürün**: durum `general_status` FK'si ile tutulur — `ACTV` (kampanyalı
  ve tekil), `PNDG` (saga sürüyor), `QUOTE_DEL` (telafi/iptal, soft-delete).
  `account_id`/`address_id` account & customer seed'leriyle hizalı.

## Hızlı doğrulama

```bash
# Kampanya detayını paket + indirim ile gör
curl http://localhost:8080/product-service/api/v1/campaigns/1

# Bir kataloğun (kategori) teklifleri
curl "http://localhost:8080/product-service/api/v1/product-offers?catalogId=1"
```
