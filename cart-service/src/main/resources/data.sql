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

-- --- örnek sepet (customer 1, account 1), general_status_id = 600101 (CART/ACTV)
INSERT INTO carts (id, created_date, general_status_id, customer_id, account_id) VALUES
    (1, now(), 600101, 1, 1)
ON CONFLICT (id) DO NOTHING;

-- --- identity sekansını en büyük id'ye çek ----------------------------------
SELECT setval(pg_get_serial_sequence('carts','id'), (SELECT MAX(id) FROM carts), true);
