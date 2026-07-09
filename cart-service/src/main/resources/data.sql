-- =============================================================================
-- cart-service DEV seed (yalnızca 'dev' profilinde, Hibernate şemayı kurduktan
-- SONRA çalışır). Idempotent'tir: ON CONFLICT (id) DO NOTHING.
--
-- Sepet, teklif/kampanya bilgisini yerel tutmaz; ekleme bir Saga ile product-service
-- tarafından doğrulanır. Bu nedenle burada yalnızca boş bir örnek sepet tohumlanır;
-- satırlar API'den (POST /carts/{id}/items/...) eklendikçe saga ile kesinleşir.
-- =============================================================================

-- --- örnek sepet (customer 1, account 1) ------------------------------------
INSERT INTO carts (id, created_date, is_active, customer_id, account_id) VALUES
    (1, now(), true, 1, 1)
ON CONFLICT (id) DO NOTHING;

-- --- identity sekansını en büyük id'ye çek ----------------------------------
SELECT setval(pg_get_serial_sequence('carts','id'), (SELECT MAX(id) FROM carts), true);
