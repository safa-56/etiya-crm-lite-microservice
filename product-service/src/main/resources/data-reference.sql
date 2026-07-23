-- =============================================================================
-- product-service REFERANS SEED — 'docker' profilinde çalışır.
--
-- docker-compose ile ayağa kalkan ortam BOŞ veriyle başlar: yalnızca uygulamanın
-- çalışması için zorunlu olan referans satırları (general_status dilimleri)
-- tohumlanır. İş verisi tohumlanmaz; API üzerinden oluşturulur.
--
-- Referans satırları olmadan servis açılır ama ilk yazma işleminde
-- ReferenceDataService "referans veri bulunamadı" hatası verir; bu yüzden bu
-- dosya docker'da zorunludur.
--
-- Dev profili tam seed'i (iş verisiyle birlikte) data.sql'den okur; iki dosya
-- referans bölümünde AYNI id'leri kullanır, biri değişirse diğeri de değişmelidir.
-- =============================================================================

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
