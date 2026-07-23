-- =============================================================================
-- account-service REFERANS SEED — 'docker' profilinde çalışır.
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
-- account-service OTOMATIK SEED (yalnızca 'dev' profilinde çalışır)
-- -----------------------------------------------------------------------------
-- customer_id / address_id customer-service kimliklerine referanstır (servisler
-- arası, yerel FK değil). active_product_count değerleri product-service'teki
-- AKTİF ürünlerle uyumludur. Idempotent (ON CONFLICT DO NOTHING).
--
-- DURUM MODELİ: billing_accounts ayrı bir durum kolonu (is_active / account_status
-- enum'u) TAŞIMAZ; durum general_status'a general_status_id FK'si ile bağlanır.
-- Senaryolar: ACTV / PNDG (saga sürüyor) / CNCL (telafi) / DEL (soft-delete).
-- =============================================================================

-- =============================================================================
-- REFERANS VERİ (Bounded Context Ownership)
-- -----------------------------------------------------------------------------
--   CUST_ACCT / CUST_ACCT_PROD_INVL   -> account-service   (bu dosya)
--   PARTY / PARTY_ROLE / IND          -> customer-service
--   PROD / PROD_SPEC / ...            -> product-service
--   CUST_ORD                          -> order-service
-- =============================================================================

-- --- GNL_ST dilimi (id'ler legacy GNL_ST tablosundan birebir) ---------------
INSERT INTO general_status
    (id, created_date, name, description, short_code, entity_code_name, entity_name)
VALUES
    (  164, now(), 'Aktif',         'Aktif',         'ACTV', 'CUST_ACCT',           'CUSTOMER_ACCOUNT'),
    ( 9001, now(), 'Iptal edilmis', 'Iptal edilmis', 'CNCL', 'CUST_ACCT_PROD_INVL', 'CUST_ACCT_PROD_INVL'),
    ( 9004, now(), 'Beklemede',     'Beklemede',     'PNDG', 'CUST_ACCT_PROD_INVL', 'CUST_ACCT_PROD_INVL'),
    ( 9009, now(), 'Silinmis',      'Silinmis',      'DEL',  'CUST_ACCT_PROD_INVL', 'CUST_ACCT_PROD_INVL'),
    ( 9010, now(), 'Aktif',         'Aktif',         'ACTV', 'CUST_ACCT_PROD_INVL', 'CUST_ACCT_PROD_INVL'),
    (10620, now(), 'Askida',        'Askida',        'SPND', 'CUST_ACCT_PROD_INVL', 'CUST_ACCT_PROD_INVL')
ON CONFLICT (id) DO NOTHING;

-- --- CUST_ACCT dilimine durum FK hedefleri (ACTV=164 zaten var) --------------
-- billing_accounts artık bu satırlara FK verir; id'ler legacy aralığıyla
-- çakışmayacak yüksek bloktan verildi.
INSERT INTO general_status
    (id, created_date, name, description, short_code, entity_code_name, entity_name)
VALUES
    (400101, now(), 'Beklemede',     'Beklemede',     'PNDG', 'CUST_ACCT', 'CUSTOMER_ACCOUNT'),
    (400102, now(), 'Iptal edilmis', 'Iptal edilmis', 'CNCL', 'CUST_ACCT', 'CUSTOMER_ACCOUNT'),
    (400103, now(), 'Silinmis',      'Silinmis',      'DEL',  'CUST_ACCT', 'CUSTOMER_ACCOUNT')
ON CONFLICT (id) DO NOTHING;

-- --- GNL_TP dilimi ----------------------------------------------------------
-- Legacy GNL_TP dökümü henüz elimizde olmadığından tohumlanmadı.

SELECT setval(pg_get_serial_sequence('general_status','id'), (SELECT MAX(id) FROM general_status), true);
