-- =============================================================================
-- order-service DEV seed (yalnızca 'dev' profilinde, Hibernate şemayı kurduktan
-- SONRA çalışır). Idempotent'tir.
--
-- Siparişler API/saga üzerinden oluşur: kullanıcı bir sepeti "Submit Order" ile
-- onaylar (POST /orders), order-service siparişi PENDING açar ve cart-service
-- doğrulamasıyla CONFIRMED olur. Bu nedenle burada sipariş verisi tohumlanmaz;
-- yalnızca bu servisin sahip olduğu referans veri dilimi basılır.
-- =============================================================================

-- =============================================================================
-- REFERANS VERİ (Bounded Context Ownership)
-- -----------------------------------------------------------------------------
-- Legacy'de GNL_ST/GNL_TP kurum genelinde TEK tablodur; satırlar ENT_CODE_NAME
-- ile bölümlenir. Mikroserviste bu tabloyu paylaşmak "shared database" anti
-- pattern'i olurdu. Bunun yerine HER SERVİS KENDİ DİLİMİNE SAHİPTİR:
--
--   CUST_ORD                          -> order-service     (bu dosya)
--   PARTY / PARTY_ROLE / IND          -> customer-service
--   PROD / PROD_SPEC / ...            -> product-service
--   CUST_ACCT / CUST_ACCT_PROD_INVL   -> account-service
--
-- Dilimler ayrık olduğundan legacy id'ler ÇAKIŞMADAN birebir korunabilir; bu da
-- ileride legacy DB'den ETL/migrasyonu kolaylaştırır.
--
-- KULLANIM NOTU: Bu servisin çalışan durum alanı (orders.status) bir saga durum
-- makinesidir ve OrderStatus enum'u (PENDING/CONFIRMED/CANCELLED) ile modellenir;
-- bu tabloya FK VERMEZ. Legacy CUST_ORD kodları (MIDLWARE/FINISHED/REJECTED) ile
-- enum değerleri birebir örtüşmez; eşleme kararı verilmemiştir. Satırlar şu an
-- legacy/ETL hizası için tutulur.
-- =============================================================================

-- --- GNL_ST dilimi: CUST_ORD (id'ler legacy GNL_ST tablosundan birebir) -----
INSERT INTO general_status
    (id, created_date, is_active, name, description, short_code, entity_code_name, entity_name)
VALUES
    (52, now(), true, 'Sipariş Alindi İşleniyor', 'Sipariş Alindi İşleniyor', 'MIDLWARE', 'CUST_ORD', 'CUST_ORD'),
    (53, now(), true, 'Tamamlandi',               'Tamamlandi',               'FINISHED', 'CUST_ORD', 'CUST_ORD'),
    (54, now(), true, 'Rededildi',                'Rededildi',                'REJECTED', 'CUST_ORD', 'CUST_ORD')
ON CONFLICT (id) DO NOTHING;

-- --- GNL_TP dilimi ----------------------------------------------------------
-- Legacy GNL_TP dökümü henüz elimizde olmadığından tohumlanmadı. Döküm geldiğinde
-- bu servisin dilimi kadar ve legacy id'leri korunarak eklenmelidir. Uydurma satır
-- basmak ETL hizasını bozacağından bilinçli olarak boş bırakıldı.

SELECT setval(pg_get_serial_sequence('general_status','id'), (SELECT MAX(id) FROM general_status), true);
