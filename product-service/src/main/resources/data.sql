-- =============================================================================
-- product-service OTOMATIK SEED (yalnızca 'dev' profilinde çalışır)
-- -----------------------------------------------------------------------------
-- Katalog (zorunlu kategori) -> ProductSpec -> ProductOffer -> Campaign (paket)
-- -> CampaignOffer -> Product (satılmış ürün). Idempotent (ON CONFLICT DO NOTHING).
-- Şema Hibernate ile kurulur; bu betik yalnızca veriyi basar.
--
-- product.account_id / address_id account & customer servislerine referanstır.
-- Kampanya paket fiyatı (campaign_price) içindeki tekliflerin liste toplamından
-- düşüktür (indirim); bu kural kodda zorlanmaz, burada da öyle kurgulandı.
-- =============================================================================

-- =============================================================================
-- REFERANS VERİ (Bounded Context Ownership)
-- -----------------------------------------------------------------------------
-- Legacy'de GNL_ST/GNL_TP kurum genelinde TEK tablodur; satırlar ENT_CODE_NAME
-- ile bölümlenir. Mikroserviste bu tabloyu paylaşmak "shared database" anti
-- pattern'i olurdu. Bunun yerine HER SERVİS KENDİ DİLİMİNE SAHİPTİR:
--
--   PROD / PROD_SPEC / PROD_CHAR_VAL /
--   PROD_SPEC_SRVC_SPEC / RSRC_SPEC   -> product-service   (bu dosya)
--   PARTY / PARTY_ROLE / IND          -> customer-service
--   CUST_ORD                          -> order-service
--   CUST_ACCT / CUST_ACCT_PROD_INVL   -> account-service
--
-- Dilimler ayrık olduğundan legacy id'ler ÇAKIŞMADAN birebir korunabilir; bu da
-- ileride legacy DB'den ETL/migrasyonu kolaylaştırır.
--
-- DURUM MODELİ: Entity'ler artık ayrı bir durum kolonu (is_active / status enum'u)
-- TAŞIMAZ; her kaydın durumu general_status tablosuna general_status_id FK'si ile
-- bağlanır. Bu nedenle FK verecek her entity dilimi için en az ACTV (Aktif) ve
-- DEL (Silinmiş) satırları burada tohumlanır. general_status'un KENDİSİ bir durum
-- tablosu olduğundan durum FK'si taşımaz.
-- =============================================================================

-- --- GNL_ST dilimi (legacy id'ler GNL_ST tablosundan birebir) ---------------
INSERT INTO general_status
    (id, created_date, name, description, short_code, entity_code_name, entity_name)
VALUES
    (   115, now(), 'Silinmis',      'Silinmis',      'DEL',       'PROD',                'PROD'),
    (   116, now(), 'Aktif',         'Aktif',         'ACTV',      'PROD',                'PROD'),
    (  1500, now(), 'Beklemede',     'Beklemede',     'PNDG',      'PROD',                'PROD'),
    ( 10600, now(), 'Askida',        'Askida',        'SPND',      'PROD',                'PROD'),
    ( 75690, now(), 'Siparis Iptal', 'Siparis Iptal', 'QUOTE_DEL', 'PROD',                'PROD'),
    (    10, now(), 'Aktif',         'Aktif',         'ACTV',      'PROD_SPEC',           'PROD_SPEC'),
    (    13, now(), 'Pasif',         'Pasif',         'PASS',      'PROD_SPEC',           'PROD_SPEC'),
    (   123, now(), 'Silinmis',      'Silinmis',      'DEL',       'PROD_CHAR_VAL',       'PROD_CHAR_VAL'),
    (   124, now(), 'Aktif',         'Aktif',         'ACTV',      'PROD_CHAR_VAL',       'PROD_CHAR_VAL'),
    (   239, now(), 'Aktif',         'Aktif',         'ACTV',      'PROD_SPEC_SRVC_SPEC', 'PROD_SPEC_SRVC_SPEC'),
    (   240, now(), 'Silinmis',      'Silinmis',      'DEL',       'PROD_SPEC_SRVC_SPEC', 'PROD_SPEC_SRVC_SPEC'),
    (    14, now(), 'Pasif',         'Pasif',         'PASS',      'RSRC_SPEC',           'RSRC_SPEC')
ON CONFLICT (id) DO NOTHING;

-- --- Yeni dilimler: durum FK'si veren entity'ler için (legacy dökümü yok) -----
-- id'ler legacy GNL_ST aralığıyla çakışmayacak biçimde yüksek bloktan verildi.
INSERT INTO general_status
    (id, created_date, name, description, short_code, entity_code_name, entity_name)
VALUES
    (200013, now(), 'Silinmis', 'Silinmis', 'DEL',  'PROD_SPEC',      'PROD_SPEC'),
    (200101, now(), 'Aktif',    'Aktif',    'ACTV', 'CATALOG',        'CATALOG'),
    (200102, now(), 'Silinmis', 'Silinmis', 'DEL',  'CATALOG',        'CATALOG'),
    (200201, now(), 'Aktif',    'Aktif',    'ACTV', 'PROD_OFFER',     'PROD_OFFER'),
    (200202, now(), 'Silinmis', 'Silinmis', 'DEL',  'PROD_OFFER',     'PROD_OFFER'),
    (200301, now(), 'Aktif',    'Aktif',    'ACTV', 'CAMPAIGN',       'CAMPAIGN'),
    (200302, now(), 'Silinmis', 'Silinmis', 'DEL',  'CAMPAIGN',       'CAMPAIGN'),
    (200401, now(), 'Aktif',    'Aktif',    'ACTV', 'CAMPAIGN_OFFER', 'CAMPAIGN_OFFER'),
    (200402, now(), 'Silinmis', 'Silinmis', 'DEL',  'CAMPAIGN_OFFER', 'CAMPAIGN_OFFER')
ON CONFLICT (id) DO NOTHING;

-- --- GNL_TP dilimi ----------------------------------------------------------
-- Legacy GNL_TP dökümü henüz elimizde olmadığından tohumlanmadı. Döküm geldiğinde
-- bu servisin dilimi kadar ve legacy id'leri korunarak eklenmelidir. Uydurma satır
-- basmak ETL hizasını bozacağından bilinçli olarak boş bırakıldı.

SELECT setval(pg_get_serial_sequence('general_status','id'), (SELECT MAX(id) FROM general_status), true);

INSERT INTO catalogs (id, created_date, general_status_id, name, description) VALUES
    (1, now(), 200101, 'Ev İnterneti', 'Fiber ve ADSL ev interneti teklifleri'),
    (2, now(), 200101, 'Mobil',        'Cep telefonu ses + data paketleri'),
    (3, now(), 200101, 'Superbox',     'Kablosuz ev interneti (Superbox) teklifleri'),
    (4, now(), 200101, 'TV',           'Dijital TV paketleri'),
    (5, now(), 200101, 'Sabit Hat',    'Ev/iş sabit telefon hattı teklifleri')
ON CONFLICT (id) DO NOTHING;

INSERT INTO product_specs (id, created_date, general_status_id, name, description) VALUES
    (1, now(), 10, 'Fiber 100 Mbps',     '100 Mbps indirme / 20 Mbps yükleme, limitsiz'),
    (2, now(), 10, 'Fiber 1000 Mbps',    '1000 Mbps indirme / 100 Mbps yükleme, limitsiz'),
    (3, now(), 10, 'Mobil 20GB',         '20GB data + 1000 dk + 1000 SMS'),
    (4, now(), 10, 'Mobil 50GB',         '50GB data + sınırsız dk + 2000 SMS'),
    (5, now(), 10, 'Superbox 50GB',      'Aylık 50GB kotalı kablosuz internet'),
    (6, now(), 10, 'Superbox Limitsiz',  'Adil kullanımlı limitsiz kablosuz internet'),
    (7, now(), 10, 'TV Başlangıç',       '80+ kanal, HD yayın'),
    (8, now(), 10, 'TV Premium',         '200+ kanal, spor + sinema paketleri'),
    (9, now(), 10, 'Sabit Hat Sınırsız', 'Sınırsız şehir içi/şehirlerarası konuşma')
ON CONFLICT (id) DO NOTHING;

INSERT INTO product_offers
    (id, created_date, general_status_id, name, catalog_id, product_spec_id, start_date, end_date, price)
VALUES
    (1,  now(), 200201, 'Ev İnterneti Fiber 100',            1, 1, DATE '2026-01-01', NULL, 299.90),
    (2,  now(), 200201, 'Ev İnterneti Fiber 1000',           1, 2, DATE '2026-01-01', NULL, 549.90),
    (3,  now(), 200201, 'Mobil 20GB Paket',                  2, 3, DATE '2026-01-01', NULL, 199.90),
    (4,  now(), 200201, 'Mobil 50GB Paket',                  2, 4, DATE '2026-01-01', NULL, 349.90),
    (5,  now(), 200201, 'Superbox 50GB',                     3, 5, DATE '2026-01-01', NULL, 399.90),
    (6,  now(), 200201, 'Superbox Limitsiz',                 3, 6, DATE '2026-01-01', NULL, 699.90),
    (7,  now(), 200201, 'TV Başlangıç Paketi',               4, 7, DATE '2026-01-01', NULL, 149.90),
    (8,  now(), 200201, 'TV Premium Paketi',                 4, 8, DATE '2026-01-01', NULL, 299.90),
    (9,  now(), 200201, 'Sabit Hat Sınırsız',                5, 9, DATE '2026-01-01', NULL,  99.90),
    (10, now(), 200201, 'Ev İnterneti Fiber 100 (Standart)', 1, 1, DATE '2026-01-01', NULL, 319.90)
ON CONFLICT (id) DO NOTHING;

INSERT INTO campaigns (id, created_date, general_status_id, name, campaign_price) VALUES
    (1, now(), 200301, 'Ev + TV Paketi',   399.90),
    (2, now(), 200301, 'Full Ev Eğlence',  749.90),
    (3, now(), 200301, 'Mobil + Superbox', 499.90)
ON CONFLICT (id) DO NOTHING;

INSERT INTO campaign_offers (id, created_date, general_status_id, campaign_id, product_offer_id) VALUES
    (1, now(), 200401, 1, 1),
    (2, now(), 200401, 1, 7),
    (3, now(), 200401, 2, 2),
    (4, now(), 200401, 2, 8),
    (5, now(), 200401, 2, 9),
    (6, now(), 200401, 3, 3),
    (7, now(), 200401, 3, 5)
ON CONFLICT (id) DO NOTHING;

-- products.general_status_id: 116=ACTV, 1500=PNDG, 75690=QUOTE_DEL (iptal).
-- İptal edilen ürün (id=6) soft-delete olduğundan deleted_date de doldurulur.
INSERT INTO products
    (id, created_date, deleted_date, general_status_id, name, product_offer_id, account_id, campaign_id,
     address_id, price_to_be_paid, status_reason)
VALUES
    (1, now(), NULL,   116,   'Ev İnterneti Fiber 100',  1, 1, 1,    1, 249.95, NULL),
    (2, now(), NULL,   116,   'TV Başlangıç Paketi',     7, 1, 1,    1, 149.95, NULL),
    (3, now(), NULL,   116,   'Mobil 20GB Paket',        3, 2, NULL, 2, 199.90, NULL),
    (4, now(), NULL,   116,   'Superbox 50GB',           5, 3, NULL, 3, 399.90, NULL),
    (5, now(), NULL,   1500,  'Sabit Hat Sınırsız',      9, 1, NULL, 1,  99.90, NULL),
    (6, now(), now(),  75690, 'Ev İnterneti Fiber 1000', 2, 3, NULL, 3, 549.90, 'Fatura hesabı doğrulanamadı.')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('catalogs','id'),        (SELECT MAX(id) FROM catalogs), true);
SELECT setval(pg_get_serial_sequence('product_specs','id'),   (SELECT MAX(id) FROM product_specs), true);
SELECT setval(pg_get_serial_sequence('product_offers','id'),  (SELECT MAX(id) FROM product_offers), true);
SELECT setval(pg_get_serial_sequence('campaigns','id'),       (SELECT MAX(id) FROM campaigns), true);
SELECT setval(pg_get_serial_sequence('campaign_offers','id'), (SELECT MAX(id) FROM campaign_offers), true);
SELECT setval(pg_get_serial_sequence('products','id'),        (SELECT MAX(id) FROM products), true);
