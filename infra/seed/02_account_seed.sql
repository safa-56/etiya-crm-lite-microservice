-- =============================================================================
-- account-service SEED  (veritabanı: accountdb)
-- -----------------------------------------------------------------------------
-- customer_id / address_id alanları customer-service kimliklerine referanstır
-- (servisler arası, yerel FK değil) → 01_customer_seed.sql ile hizalıdır.
-- active_product_count değerleri 03_product_seed.sql'deki AKTİF ürünlerle uyumludur.
--
-- DURUM MODELİ: billing_accounts is_active / account_status TAŞIMAZ; durum
-- general_status'a general_status_id FK'si ile bağlanır. Bu seed, FK verdiği
-- general_status dilimlerini (idempotent) tohumlar.
-- Senaryolar: ACTV / PNDG (saga sürüyor) / CNCL (telafi) / DEL (soft-delete).
--
-- Çalıştırma:
--   docker exec -i crm-postgres psql -U postgres -d accountdb < infra/seed/02_account_seed.sql
-- =============================================================================

BEGIN;

-- --- Şema uyumu (eski model -> yeni model). Idempotent; yeni şemada no-op. -----
ALTER TABLE billing_accounts ADD COLUMN IF NOT EXISTS general_status_id bigint;

TRUNCATE TABLE billing_accounts RESTART IDENTITY CASCADE;

-- --- general_status dilimleri (durum FK hedefleri; idempotent) ---------------
-- id'ler account-service data.sql ile birebir aynıdır.
INSERT INTO general_status
    (id, created_date, name, description, short_code, entity_code_name, entity_name)
VALUES
    (   164, now(), 'Aktif',         'Aktif',         'ACTV', 'CUST_ACCT', 'CUSTOMER_ACCOUNT'),
    (400101, now(), 'Beklemede',     'Beklemede',     'PNDG', 'CUST_ACCT', 'CUSTOMER_ACCOUNT'),
    (400102, now(), 'Iptal edilmis', 'Iptal edilmis', 'CNCL', 'CUST_ACCT', 'CUSTOMER_ACCOUNT'),
    (400103, now(), 'Silinmis',      'Silinmis',      'DEL',  'CUST_ACCT', 'CUSTOMER_ACCOUNT')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('general_status','id'), (SELECT MAX(id) FROM general_status), true);

-- general_status_id: 164=ACTV, 400101=PNDG, 400102=CNCL, 400103=DEL.
INSERT INTO billing_accounts
    (id, created_date, deleted_date, general_status_id, customer_id, account_name, account_description,
     address_id, pending_address_id, address, account_number, order_number,
     account_type, active_product_count, status_reason)
VALUES
    -- acc1: müşteri 1, ACTV, 2 aktif ürün (Ev+TV kampanyası)
    (1, now(), NULL,  164,    1, 'Ahmet Ev Hattı',    'Birincil fatura hesabı',
     1, NULL, 'İstanbul / Bağdat Caddesi No:12 D:4', 'ACC0000000001', 'ORD00000001',
     'BILLING_ACCOUNT', 2, NULL),

    -- acc2: müşteri 1 (ikinci hesap), ACTV, 1 aktif ürün
    (2, now(), NULL,  164,    1, 'Ahmet İş Hattı',    'İkincil fatura hesabı (iş adresi)',
     2, NULL, 'Ankara / Atatürk Bulvarı No:88', 'ACC0000000002', 'ORD00000002',
     'BILLING_ACCOUNT', 1, NULL),

    -- acc3: müşteri 2, ACTV, 1 aktif ürün
    (3, now(), NULL,  164,    2, 'Ayşe Ev Hattı',     'Birincil fatura hesabı',
     3, NULL, 'İzmir / Kordon No:5 D:2', 'ACC0000000003', 'ORD00000003',
     'BILLING_ACCOUNT', 1, NULL),

    -- acc4: müşteri 3, ACTV, 0 ürün → "ürünü yok, silinebilir" senaryosu
    (4, now(), NULL,  164,    3, 'Mehmet Ev Hattı',   'Aktif ama ürünsüz hesap',
     4, NULL, 'Bursa / Nilüfer Caddesi No:34', 'ACC0000000004', 'ORD00000004',
     'BILLING_ACCOUNT', 0, NULL),

    -- acc5: müşteri 4, PNDG → saga doğrulaması sürüyor (adres snapshot boş)
    (5, now(), NULL,  400101, 4, 'Fatma Yeni Hat',    'Oluşturma saga''sı beklemede',
     6, NULL, '', 'ACC0000000005', 'ORD00000005',
     'BILLING_ACCOUNT', 0, NULL),

    -- acc6: müşteri 5, DEL → soft-delete edilmiş hesap
    (6, now(), now(), 400103, 5, 'Can Eski Hat',      'Pasifleştirilmiş (silinmiş) hesap',
     7, NULL, 'Ankara / Tunalı Hilmi No:45 D:3', 'ACC0000000006', 'ORD00000006',
     'BILLING_ACCOUNT', 0, NULL),

    -- acc7: müşteri 2 (ikinci hesap), CNCL → saga telafisi (doğrulama başarısız)
    (7, now(), now(), 400102, 2, 'Ayşe İptal Hat',    'Saga telafisiyle iptal edilen hesap',
     NULL, NULL, '', 'ACC0000000007', 'ORD00000007',
     'BILLING_ACCOUNT', 0, 'Adres doğrulaması başarısız (müşteriye ait değil).');

SELECT setval(pg_get_serial_sequence('billing_accounts','id'), (SELECT MAX(id) FROM billing_accounts), true);

COMMIT;
