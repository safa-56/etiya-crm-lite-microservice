-- =============================================================================
-- order-service REFERANS SEED — 'docker' profilinde çalışır.
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
-- order-service DEV seed (yalnızca 'dev' profilinde, Hibernate şemayı kurduktan
-- SONRA çalışır). Idempotent'tir.
--
-- Siparişler API/saga üzerinden oluşur: kullanıcı bir sepeti "Submit Order" ile
-- onaylar (POST /orders), order-service siparişi CUST_ORD/MIDLWARE açar ve
-- cart-service doğrulamasıyla FINISHED olur. Bu nedenle burada sipariş verisi
-- tohumlanmaz; yalnızca bu servisin sahip olduğu referans veri dilimi basılır.
--
-- DURUM MODELİ: orders/order_items ayrı bir durum kolonu (is_active / status enum'u)
-- TAŞIMAZ; durum general_status'a general_status_id FK'si ile bağlanır.
-- Saga eşlemesi: PENDING→MIDLWARE, CONFIRMED→FINISHED, CANCELLED→REJECTED,
-- soft-delete→DEL. Sipariş satırları CUST_ORD_ITEM dilimini kullanır.
-- =============================================================================

-- =============================================================================
-- REFERANS VERİ (Bounded Context Ownership)
-- -----------------------------------------------------------------------------
--   CUST_ORD                          -> order-service     (bu dosya)
--   PARTY / PARTY_ROLE / IND          -> customer-service
--   PROD / PROD_SPEC / ...            -> product-service
--   CUST_ACCT / CUST_ACCT_PROD_INVL   -> account-service
-- =============================================================================

-- --- GNL_ST dilimi: CUST_ORD (id'ler legacy GNL_ST tablosundan birebir) -----
INSERT INTO general_status
    (id, created_date, name, description, short_code, entity_code_name, entity_name)
VALUES
    (52, now(), 'Sipariş Alindi İşleniyor', 'Sipariş Alindi İşleniyor', 'MIDLWARE', 'CUST_ORD', 'CUST_ORD'),
    (53, now(), 'Tamamlandi',               'Tamamlandi',               'FINISHED', 'CUST_ORD', 'CUST_ORD'),
    (54, now(), 'Rededildi',                'Rededildi',                'REJECTED', 'CUST_ORD', 'CUST_ORD')
ON CONFLICT (id) DO NOTHING;

-- --- Yeni dilimler: soft-delete + sipariş satırı durumları (legacy dökümü yok) -
INSERT INTO general_status
    (id, created_date, name, description, short_code, entity_code_name, entity_name)
VALUES
    (500101, now(), 'Silinmis', 'Silinmis', 'DEL',  'CUST_ORD',      'CUST_ORD'),
    (500201, now(), 'Aktif',    'Aktif',    'ACTV', 'CUST_ORD_ITEM', 'CUST_ORD_ITEM'),
    (500202, now(), 'Silinmis', 'Silinmis', 'DEL',  'CUST_ORD_ITEM', 'CUST_ORD_ITEM')
ON CONFLICT (id) DO NOTHING;

-- --- GNL_TP dilimi ----------------------------------------------------------
-- Legacy GNL_TP dökümü henüz elimizde olmadığından tohumlanmadı.

SELECT setval(pg_get_serial_sequence('general_status','id'), (SELECT MAX(id) FROM general_status), true);
