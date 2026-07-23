-- =============================================================================
-- cart-service REFERANS SEED — 'docker' profilinde çalışır.
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
-- cart-service DEV seed (yalnızca 'dev' profilinde, Hibernate şemayı kurduktan
-- SONRA çalışır). Idempotent'tir: ON CONFLICT (id) DO NOTHING.
--
-- Sepet, teklif/kampanya bilgisini yerel tutmaz; ekleme bir Saga ile product-service
-- tarafından doğrulanır. Bu nedenle burada yalnızca boş bir örnek sepet tohumlanır;
-- satırlar API'den (POST /carts/{id}/items/...) eklendikçe saga ile kesinleşir.
--
-- DURUM MODELİ: carts/cart_items/cart_item_lines ayrı bir durum kolonu (is_active /
-- CartItemStatus enum'u) TAŞIMAZ; durum general_status'a general_status_id FK'si
-- ile bağlanır. Saga eşlemesi: PENDING→PNDG, ACTIVE→ACTV, CANCELLED→CNCL,
-- soft-delete→DEL.
-- =============================================================================

-- --- GNL_ST dilimleri: CART / CART_ITEM / CART_ITEM_LINE ---------------------
-- Bu tablo cart-service'e sonradan eklendi; legacy dökümü olmadığından id'ler
-- yüksek bir bloktan verildi.
INSERT INTO general_status
    (id, created_date, name, description, short_code, entity_code_name, entity_name)
VALUES
    (600101, now(), 'Aktif',    'Aktif',    'ACTV', 'CART',           'CART'),
    (600102, now(), 'Silinmis', 'Silinmis', 'DEL',  'CART',           'CART'),
    (600201, now(), 'Aktif',    'Aktif',    'ACTV', 'CART_ITEM',      'CART_ITEM'),
    (600202, now(), 'Beklemede','Beklemede','PNDG', 'CART_ITEM',      'CART_ITEM'),
    (600203, now(), 'Iptal',    'Iptal',    'CNCL', 'CART_ITEM',      'CART_ITEM'),
    (600204, now(), 'Silinmis', 'Silinmis', 'DEL',  'CART_ITEM',      'CART_ITEM'),
    (600301, now(), 'Aktif',    'Aktif',    'ACTV', 'CART_ITEM_LINE', 'CART_ITEM_LINE'),
    (600302, now(), 'Silinmis', 'Silinmis', 'DEL',  'CART_ITEM_LINE', 'CART_ITEM_LINE')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('general_status','id'), (SELECT MAX(id) FROM general_status), true);
