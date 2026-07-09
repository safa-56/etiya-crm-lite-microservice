-- =============================================================================
-- account-service SEED  (veritabanı: accountdb)
-- -----------------------------------------------------------------------------
-- customer_id / address_id alanları customer-service kimliklerine referanstır
-- (servisler arası, yerel FK değil) → 01_customer_seed.sql ile hizalıdır.
-- active_product_count değerleri 03_product_seed.sql'deki AKTİF ürünlerle uyumludur.
--
-- Senaryolar: ACTIVE / PENDING (saga sürüyor) / CANCELLED (saga telafisi) /
-- PASSIVE (soft-delete). Bir müşterinin birden çok hesabı olabilir.
--
-- Çalıştırma:
--   docker exec -i crm-postgres psql -U postgres -d accountdb < infra/seed/02_account_seed.sql
-- =============================================================================

BEGIN;

TRUNCATE TABLE billing_accounts RESTART IDENTITY CASCADE;

INSERT INTO billing_accounts
    (id, created_date, is_active, customer_id, account_name, account_description,
     address_id, pending_address_id, address, account_number, order_number,
     account_type, account_status, active_product_count, status_reason)
VALUES
    -- acc1: müşteri 1, ACTIVE, 2 aktif ürün (Ev+TV kampanyası)
    (1, now(), true,  1, 'Ahmet Ev Hattı',    'Birincil fatura hesabı',
     1, NULL, 'İstanbul / Bağdat Caddesi No:12 D:4', 'ACC0000000001', 'ORD00000001',
     'BILLING_ACCOUNT', 'ACTIVE', 2, NULL),

    -- acc2: müşteri 1 (ikinci hesap), ACTIVE, 1 aktif ürün
    (2, now(), true,  1, 'Ahmet İş Hattı',    'İkincil fatura hesabı (iş adresi)',
     2, NULL, 'Ankara / Atatürk Bulvarı No:88', 'ACC0000000002', 'ORD00000002',
     'BILLING_ACCOUNT', 'ACTIVE', 1, NULL),

    -- acc3: müşteri 2, ACTIVE, 1 aktif ürün
    (3, now(), true,  2, 'Ayşe Ev Hattı',     'Birincil fatura hesabı',
     3, NULL, 'İzmir / Kordon No:5 D:2', 'ACC0000000003', 'ORD00000003',
     'BILLING_ACCOUNT', 'ACTIVE', 1, NULL),

    -- acc4: müşteri 3, ACTIVE, 0 ürün → "ürünü yok, silinebilir" senaryosu
    (4, now(), true,  3, 'Mehmet Ev Hattı',   'Aktif ama ürünsüz hesap',
     4, NULL, 'Bursa / Nilüfer Caddesi No:34', 'ACC0000000004', 'ORD00000004',
     'BILLING_ACCOUNT', 'ACTIVE', 0, NULL),

    -- acc5: müşteri 4, PENDING → saga doğrulaması sürüyor (adres snapshot boş)
    (5, now(), true,  4, 'Fatma Yeni Hat',    'Oluşturma saga''sı beklemede',
     6, NULL, '', 'ACC0000000005', 'ORD00000005',
     'BILLING_ACCOUNT', 'PENDING', 0, NULL),

    -- acc6: müşteri 5, PASSIVE → soft-delete edilmiş hesap
    (6, now(), false, 5, 'Can Eski Hat',      'Pasifleştirilmiş hesap',
     7, NULL, 'Ankara / Tunalı Hilmi No:45 D:3', 'ACC0000000006', 'ORD00000006',
     'BILLING_ACCOUNT', 'PASSIVE', 0, NULL),

    -- acc7: müşteri 2 (ikinci hesap), CANCELLED → saga telafisi (doğrulama başarısız)
    (7, now(), false, 2, 'Ayşe İptal Hat',    'Saga telafisiyle iptal edilen hesap',
     NULL, NULL, '', 'ACC0000000007', 'ORD00000007',
     'BILLING_ACCOUNT', 'CANCELLED', 0, 'Adres doğrulaması başarısız (müşteriye ait değil).');

SELECT setval(pg_get_serial_sequence('billing_accounts','id'), (SELECT MAX(id) FROM billing_accounts), true);

COMMIT;
