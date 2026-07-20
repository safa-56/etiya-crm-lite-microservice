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
-- DURUM MODELİ: Entity'ler ayrı bir durum kolonu (is_active / status enum'u)
-- TAŞIMAZ; her kaydın durumu general_status tablosuna general_status_id FK'si ile
-- bağlanır. Bu seed, FK verdiği general_status dilimlerini de (idempotent) tohumlar.
--
-- Çalıştırma:
--   docker exec -i crm-postgres psql -U postgres -d productdb < infra/seed/03_product_seed.sql
-- =============================================================================

BEGIN;

-- --- Şema uyumu (eski model -> yeni model). Idempotent; yeni şemada no-op. -----
-- Uygulama yeni kodla yeniden başlatılmadan bu seed'i çalıştırabilmek için, yeni
-- kolonları (yoksa) ekler. general_status_id, iş tablolarının durum FK'sidir.
ALTER TABLE product_offers  ADD COLUMN IF NOT EXISTS catalog_id bigint;
ALTER TABLE campaigns       ADD COLUMN IF NOT EXISTS campaign_price numeric(19,2);
ALTER TABLE catalogs        ADD COLUMN IF NOT EXISTS general_status_id bigint;
ALTER TABLE product_specs   ADD COLUMN IF NOT EXISTS general_status_id bigint;
ALTER TABLE product_offers  ADD COLUMN IF NOT EXISTS general_status_id bigint;
ALTER TABLE campaigns       ADD COLUMN IF NOT EXISTS general_status_id bigint;
ALTER TABLE campaign_offers ADD COLUMN IF NOT EXISTS general_status_id bigint;
ALTER TABLE products        ADD COLUMN IF NOT EXISTS general_status_id bigint;

TRUNCATE TABLE products, campaign_offers, campaigns, product_offers, product_specs, catalogs
    RESTART IDENTITY CASCADE;

-- --- general_status dilimleri (durum FK hedefleri; idempotent) ---------------
-- general_status TRUNCATE EDİLMEZ; yalnızca eksik satırlar eklenir. id'ler
-- product-service data.sql ile birebir aynıdır (aynı FK'lere referans verilir).
INSERT INTO general_status
    (id, created_date, name, description, short_code, entity_code_name, entity_name)
VALUES
    (   115, now(), 'Silinmis',      'Silinmis',      'DEL',       'PROD',           'PROD'),
    (   116, now(), 'Aktif',         'Aktif',         'ACTV',      'PROD',           'PROD'),
    (  1500, now(), 'Beklemede',     'Beklemede',     'PNDG',      'PROD',           'PROD'),
    ( 75690, now(), 'Siparis Iptal', 'Siparis Iptal', 'QUOTE_DEL', 'PROD',           'PROD'),
    (    10, now(), 'Aktif',         'Aktif',         'ACTV',      'PROD_SPEC',      'PROD_SPEC'),
    (200013, now(), 'Silinmis',      'Silinmis',      'DEL',       'PROD_SPEC',      'PROD_SPEC'),
    (200101, now(), 'Aktif',         'Aktif',         'ACTV',      'CATALOG',        'CATALOG'),
    (200102, now(), 'Silinmis',      'Silinmis',      'DEL',       'CATALOG',        'CATALOG'),
    (200201, now(), 'Aktif',         'Aktif',         'ACTV',      'PROD_OFFER',     'PROD_OFFER'),
    (200202, now(), 'Silinmis',      'Silinmis',      'DEL',       'PROD_OFFER',     'PROD_OFFER'),
    (200301, now(), 'Aktif',         'Aktif',         'ACTV',      'CAMPAIGN',       'CAMPAIGN'),
    (200302, now(), 'Silinmis',      'Silinmis',      'DEL',       'CAMPAIGN',       'CAMPAIGN'),
    (200401, now(), 'Aktif',         'Aktif',         'ACTV',      'CAMPAIGN_OFFER', 'CAMPAIGN_OFFER'),
    (200402, now(), 'Silinmis',      'Silinmis',      'DEL',       'CAMPAIGN_OFFER', 'CAMPAIGN_OFFER')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('general_status','id'), (SELECT MAX(id) FROM general_status), true);

-- --- catalogs (kategoriler) -------------------------------------------------
INSERT INTO catalogs (id, created_date, general_status_id, name, description) VALUES
    (1, now(), 200101, 'Ev İnterneti', 'Fiber ve ADSL ev interneti teklifleri'),
    (2, now(), 200101, 'Mobil',        'Cep telefonu ses + data paketleri'),
    (3, now(), 200101, 'Superbox',     'Kablosuz ev interneti (Superbox) teklifleri'),
    (4, now(), 200101, 'TV',           'Dijital TV paketleri'),
    (5, now(), 200101, 'Sabit Hat',    'Ev/iş sabit telefon hattı teklifleri');

-- --- product_specs (teknik özellikler) --------------------------------------
INSERT INTO product_specs (id, created_date, general_status_id, name, description) VALUES
    (1, now(), 10, 'Fiber 100 Mbps',      '100 Mbps indirme / 20 Mbps yükleme, limitsiz'),
    (2, now(), 10, 'Fiber 1000 Mbps',     '1000 Mbps indirme / 100 Mbps yükleme, limitsiz'),
    (3, now(), 10, 'Mobil 20GB',          '20GB data + 1000 dk + 1000 SMS'),
    (4, now(), 10, 'Mobil 50GB',          '50GB data + sınırsız dk + 2000 SMS'),
    (5, now(), 10, 'Superbox 50GB',       'Aylık 50GB kotalı kablosuz internet'),
    (6, now(), 10, 'Superbox Limitsiz',   'Adil kullanımlı limitsiz kablosuz internet'),
    (7, now(), 10, 'TV Başlangıç',        '80+ kanal, HD yayın'),
    (8, now(), 10, 'TV Premium',          '200+ kanal, spor + sinema paketleri'),
    (9, now(), 10, 'Sabit Hat Sınırsız',  'Sınırsız şehir içi/şehirlerarası konuşma');

-- --- product_offers (fiyatlandırılmış teklifler; her biri bir kataloğa bağlı)
INSERT INTO product_offers
    (id, created_date, general_status_id, name, catalog_id, product_spec_id, start_date, end_date, price)
VALUES
    (1,  now(), 200201, 'Ev İnterneti Fiber 100',        1, 1, DATE '2026-01-01', NULL, 299.90),
    (2,  now(), 200201, 'Ev İnterneti Fiber 1000',       1, 2, DATE '2026-01-01', NULL, 549.90),
    (3,  now(), 200201, 'Mobil 20GB Paket',              2, 3, DATE '2026-01-01', NULL, 199.90),
    (4,  now(), 200201, 'Mobil 50GB Paket',              2, 4, DATE '2026-01-01', NULL, 349.90),
    (5,  now(), 200201, 'Superbox 50GB',                 3, 5, DATE '2026-01-01', NULL, 399.90),
    (6,  now(), 200201, 'Superbox Limitsiz',             3, 6, DATE '2026-01-01', NULL, 699.90),
    (7,  now(), 200201, 'TV Başlangıç Paketi',           4, 7, DATE '2026-01-01', NULL, 149.90),
    (8,  now(), 200201, 'TV Premium Paketi',             4, 8, DATE '2026-01-01', NULL, 299.90),
    (9,  now(), 200201, 'Sabit Hat Sınırsız',            5, 9, DATE '2026-01-01', NULL,  99.90),
    -- Kampanyaya bağlı olmayan, yalnızca katalogta görünen alternatif teklif
    (10, now(), 200201, 'Ev İnterneti Fiber 100 (Standart)', 1, 1, DATE '2026-01-01', NULL, 319.90);

-- --- campaigns (paketler) ---------------------------------------------------
-- campaign_price < Σ(liste) olacak şekilde kurgulandı (indirim gösterimi için).
INSERT INTO campaigns (id, created_date, general_status_id, name, campaign_price) VALUES
    (1, now(), 200301, 'Ev + TV Paketi',     399.90),  -- offer 1 (299.90) + 7 (149.90) = 449.80
    (2, now(), 200301, 'Full Ev Eğlence',    749.90),  -- offer 2 (549.90) + 8 (299.90) + 9 (99.90) = 949.70
    (3, now(), 200301, 'Mobil + Superbox',   499.90);  -- offer 3 (199.90) + 5 (399.90) = 599.80

-- --- campaign_offers (paket içerikleri; N-N) --------------------------------
INSERT INTO campaign_offers (id, created_date, general_status_id, campaign_id, product_offer_id) VALUES
    (1, now(), 200401, 1, 1),
    (2, now(), 200401, 1, 7),
    (3, now(), 200401, 2, 2),
    (4, now(), 200401, 2, 8),
    (5, now(), 200401, 2, 9),
    (6, now(), 200401, 3, 3),
    (7, now(), 200401, 3, 5);

-- --- products (satılmış ürünler) --------------------------------------------
-- general_status_id: 116=ACTV (satış kesinleşti) / 1500=PNDG (saga sürüyor) /
-- 75690=QUOTE_DEL (telafi/iptal; soft-delete → deleted_date dolu).
-- account_id → accountdb, address_id → customerdb.
INSERT INTO products
    (id, created_date, deleted_date, general_status_id, name, product_offer_id, account_id, campaign_id,
     address_id, price_to_be_paid, status_reason)
VALUES
    -- acc1: Ev + TV kampanyasından 2 ürün (ACTV) → active_product_count=2
    (1, now(), NULL,  116,   'Ev İnterneti Fiber 100', 1, 1, 1,    1, 249.95, NULL),
    (2, now(), NULL,  116,   'TV Başlangıç Paketi',    7, 1, 1,    1, 149.95, NULL),
    -- acc2: tekil mobil teklif (ACTV) → active_product_count=1
    (3, now(), NULL,  116,   'Mobil 20GB Paket',       3, 2, NULL, 2, 199.90, NULL),
    -- acc3: tekil superbox (ACTV) → active_product_count=1
    (4, now(), NULL,  116,   'Superbox 50GB',          5, 3, NULL, 3, 399.90, NULL),
    -- acc1: saga süren satış (PNDG) → sayaca dahil DEĞİL
    (5, now(), NULL,  1500,  'Sabit Hat Sınırsız',     9, 1, NULL, 1,  99.90, NULL),
    -- acc3: telafi edilen satış (QUOTE_DEL, soft-deleted) → sayaca dahil DEĞİL
    (6, now(), now(), 75690, 'Ev İnterneti Fiber 1000',2, 3, NULL, 3, 549.90, 'Fatura hesabı doğrulanamadı.');

-- --- identity sekanslarını en büyük id'ye çek ------------------------------
SELECT setval(pg_get_serial_sequence('catalogs','id'),        (SELECT MAX(id) FROM catalogs), true);
SELECT setval(pg_get_serial_sequence('product_specs','id'),   (SELECT MAX(id) FROM product_specs), true);
SELECT setval(pg_get_serial_sequence('product_offers','id'),  (SELECT MAX(id) FROM product_offers), true);
SELECT setval(pg_get_serial_sequence('campaigns','id'),       (SELECT MAX(id) FROM campaigns), true);
SELECT setval(pg_get_serial_sequence('campaign_offers','id'), (SELECT MAX(id) FROM campaign_offers), true);
SELECT setval(pg_get_serial_sequence('products','id'),        (SELECT MAX(id) FROM products), true);

COMMIT;
