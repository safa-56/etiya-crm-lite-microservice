-- =============================================================================
-- product-service SEED  (veritabanı: productdb)
-- -----------------------------------------------------------------------------
-- Katalog (zorunlu kategori) → ProductSpec → ProductOffer → Campaign (paket) →
-- CampaignOffer (paket içeriği) → Product (satılmış ürün) zincirini kurar.
--
-- Model kuralları:
--   * Her ProductOffer TAM OLARAK bir kataloğa aittir (catalog_id NOT NULL).
--   * Campaign, birden çok teklifi tek "campaign_price" ile paketler; içindeki
--     tekliflerin liste fiyatları toplamından düşük olması beklenir (indirim),
--     bu kural KODDA zorlanmaz — burada da öyle kurgulandı.
--   * product.account_id / address_id account & customer servislerine referanstır.
--
-- Çalıştırma:
--   docker exec -i crm-postgres psql -U postgres -d productdb < infra/seed/03_product_seed.sql
-- =============================================================================

BEGIN;

-- --- Şema uyumu (eski model -> yeni model). Idempotent; yeni şemada no-op. -----
-- Uygulama yeni kodla yeniden başlatılmadan bu seed'i çalıştırabilmek için, yeni
-- kolonları (yoksa) ekler. product_offers.catalog_id CASCADE ile eski catalog_offers
-- join tablosunu da temizler (aşağıdaki TRUNCATE FK üzerinden).
ALTER TABLE product_offers ADD COLUMN IF NOT EXISTS catalog_id bigint;
ALTER TABLE campaigns      ADD COLUMN IF NOT EXISTS campaign_price numeric(19,2);

TRUNCATE TABLE products, campaign_offers, campaigns, product_offers, product_specs, catalogs
    RESTART IDENTITY CASCADE;

-- --- catalogs (kategoriler) -------------------------------------------------
INSERT INTO catalogs (id, created_date, is_active, name, description) VALUES
    (1, now(), true, 'Ev İnterneti', 'Fiber ve ADSL ev interneti teklifleri'),
    (2, now(), true, 'Mobil',        'Cep telefonu ses + data paketleri'),
    (3, now(), true, 'Superbox',     'Kablosuz ev interneti (Superbox) teklifleri'),
    (4, now(), true, 'TV',           'Dijital TV paketleri'),
    (5, now(), true, 'Sabit Hat',    'Ev/iş sabit telefon hattı teklifleri');

-- --- product_specs (teknik özellikler) --------------------------------------
INSERT INTO product_specs (id, created_date, is_active, name, description) VALUES
    (1, now(), true, 'Fiber 100 Mbps',      '100 Mbps indirme / 20 Mbps yükleme, limitsiz'),
    (2, now(), true, 'Fiber 1000 Mbps',     '1000 Mbps indirme / 100 Mbps yükleme, limitsiz'),
    (3, now(), true, 'Mobil 20GB',          '20GB data + 1000 dk + 1000 SMS'),
    (4, now(), true, 'Mobil 50GB',          '50GB data + sınırsız dk + 2000 SMS'),
    (5, now(), true, 'Superbox 50GB',       'Aylık 50GB kotalı kablosuz internet'),
    (6, now(), true, 'Superbox Limitsiz',   'Adil kullanımlı limitsiz kablosuz internet'),
    (7, now(), true, 'TV Başlangıç',        '80+ kanal, HD yayın'),
    (8, now(), true, 'TV Premium',          '200+ kanal, spor + sinema paketleri'),
    (9, now(), true, 'Sabit Hat Sınırsız',  'Sınırsız şehir içi/şehirlerarası konuşma');

-- --- product_offers (fiyatlandırılmış teklifler; her biri bir kataloğa bağlı)
INSERT INTO product_offers
    (id, created_date, is_active, name, catalog_id, product_spec_id, start_date, end_date, price)
VALUES
    (1,  now(), true, 'Ev İnterneti Fiber 100',        1, 1, DATE '2026-01-01', NULL, 299.90),
    (2,  now(), true, 'Ev İnterneti Fiber 1000',       1, 2, DATE '2026-01-01', NULL, 549.90),
    (3,  now(), true, 'Mobil 20GB Paket',              2, 3, DATE '2026-01-01', NULL, 199.90),
    (4,  now(), true, 'Mobil 50GB Paket',              2, 4, DATE '2026-01-01', NULL, 349.90),
    (5,  now(), true, 'Superbox 50GB',                 3, 5, DATE '2026-01-01', NULL, 399.90),
    (6,  now(), true, 'Superbox Limitsiz',             3, 6, DATE '2026-01-01', NULL, 699.90),
    (7,  now(), true, 'TV Başlangıç Paketi',           4, 7, DATE '2026-01-01', NULL, 149.90),
    (8,  now(), true, 'TV Premium Paketi',             4, 8, DATE '2026-01-01', NULL, 299.90),
    (9,  now(), true, 'Sabit Hat Sınırsız',            5, 9, DATE '2026-01-01', NULL,  99.90),
    -- Kampanyaya bağlı olmayan, yalnızca katalogta görünen alternatif teklif
    (10, now(), true, 'Ev İnterneti Fiber 100 (Standart)', 1, 1, DATE '2026-01-01', NULL, 319.90);

-- --- campaigns (paketler) ---------------------------------------------------
-- campaign_price < Σ(liste) olacak şekilde kurgulandı (indirim gösterimi için).
INSERT INTO campaigns (id, created_date, is_active, name, campaign_price) VALUES
    (1, now(), true, 'Ev + TV Paketi',     399.90),  -- offer 1 (299.90) + 7 (149.90) = 449.80
    (2, now(), true, 'Full Ev Eğlence',    749.90),  -- offer 2 (549.90) + 8 (299.90) + 9 (99.90) = 949.70
    (3, now(), true, 'Mobil + Superbox',   499.90);  -- offer 3 (199.90) + 5 (399.90) = 599.80

-- --- campaign_offers (paket içerikleri; N-N) --------------------------------
INSERT INTO campaign_offers (id, created_date, is_active, campaign_id, product_offer_id) VALUES
    (1, now(), true, 1, 1),
    (2, now(), true, 1, 7),
    (3, now(), true, 2, 2),
    (4, now(), true, 2, 8),
    (5, now(), true, 2, 9),
    (6, now(), true, 3, 3),
    (7, now(), true, 3, 5);

-- --- products (satılmış ürünler) --------------------------------------------
-- status: ACTIVE (satış kesinleşti) / PENDING (saga sürüyor) / CANCELLED (telafi)
-- account_id → accountdb, address_id → customerdb.
INSERT INTO products
    (id, created_date, is_active, name, product_offer_id, account_id, campaign_id,
     address_id, price_to_be_paid, status, status_reason)
VALUES
    -- acc1: Ev + TV kampanyasından 2 ürün (ACTIVE) → active_product_count=2
    (1, now(), true,  'Ev İnterneti Fiber 100', 1, 1, 1,    1, 249.95, 'ACTIVE',    NULL),
    (2, now(), true,  'TV Başlangıç Paketi',    7, 1, 1,    1, 149.95, 'ACTIVE',    NULL),
    -- acc2: tekil mobil teklif (ACTIVE) → active_product_count=1
    (3, now(), true,  'Mobil 20GB Paket',       3, 2, NULL, 2, 199.90, 'ACTIVE',    NULL),
    -- acc3: tekil superbox (ACTIVE) → active_product_count=1
    (4, now(), true,  'Superbox 50GB',          5, 3, NULL, 3, 399.90, 'ACTIVE',    NULL),
    -- acc1: saga süren satış (PENDING) → sayaca dahil DEĞİL
    (5, now(), true,  'Sabit Hat Sınırsız',     9, 1, NULL, 1,  99.90, 'PENDING',   NULL),
    -- acc3: telafi edilen satış (CANCELLED, soft-deleted) → sayaca dahil DEĞİL
    (6, now(), false, 'Ev İnterneti Fiber 1000',2, 3, NULL, 3, 549.90, 'CANCELLED', 'Fatura hesabı doğrulanamadı.');

-- --- identity sekanslarını en büyük id'ye çek ------------------------------
SELECT setval(pg_get_serial_sequence('catalogs','id'),        (SELECT MAX(id) FROM catalogs), true);
SELECT setval(pg_get_serial_sequence('product_specs','id'),   (SELECT MAX(id) FROM product_specs), true);
SELECT setval(pg_get_serial_sequence('product_offers','id'),  (SELECT MAX(id) FROM product_offers), true);
SELECT setval(pg_get_serial_sequence('campaigns','id'),       (SELECT MAX(id) FROM campaigns), true);
SELECT setval(pg_get_serial_sequence('campaign_offers','id'), (SELECT MAX(id) FROM campaign_offers), true);
SELECT setval(pg_get_serial_sequence('products','id'),        (SELECT MAX(id) FROM products), true);

COMMIT;
