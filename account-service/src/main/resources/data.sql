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

-- general_status_id: 164=ACTV, 400101=PNDG, 400102=CNCL, 400103=DEL.
-- CNCL/DEL kayıtları soft-delete olduğundan deleted_date de doldurulur.
INSERT INTO billing_accounts
    (id, created_date, deleted_date, general_status_id, customer_id, account_name, account_description,
     address_id, pending_address_id, address, account_number, order_number,
     account_type, active_product_count, status_reason)
VALUES
    (1, now(), NULL,  164,    1, 'Ahmet Ev Hattı',  'Birincil fatura hesabı',
     1, NULL, 'İstanbul / Bağdat Caddesi No:12 D:4', '1000000001', '10000001',
     'BILLING_ACCOUNT', 2, NULL),
    (2, now(), NULL,  164,    1, 'Ahmet İş Hattı',  'İkincil fatura hesabı (iş adresi)',
     2, NULL, 'Ankara / Atatürk Bulvarı No:88', '1000000002', '10000002',
     'BILLING_ACCOUNT', 1, NULL),
    (3, now(), NULL,  164,    2, 'Ayşe Ev Hattı',   'Birincil fatura hesabı',
     3, NULL, 'İzmir / Kordon No:5 D:2', '1000000003', '10000003',
     'BILLING_ACCOUNT', 1, NULL),
    (4, now(), NULL,  164,    3, 'Mehmet Ev Hattı', 'Aktif ama ürünsüz hesap (silinebilir)',
     4, NULL, 'Bursa / Nilüfer Caddesi No:34', '1000000004', '10000004',
     'BILLING_ACCOUNT', 0, NULL),
    (5, now(), NULL,  400101, 4, 'Fatma Yeni Hat',  'Oluşturma sagası beklemede',
     6, NULL, '', '1000000005', '10000005',
     'BILLING_ACCOUNT', 0, NULL),
    (6, now(), now(), 400103, 5, 'Can Eski Hat',    'Pasifleştirilmiş (silinmiş) hesap',
     7, NULL, 'Ankara / Tunalı Hilmi No:45 D:3', '1000000006', '10000006',
     'BILLING_ACCOUNT', 0, NULL),
    (7, now(), now(), 400102, 2, 'Ayşe İptal Hat',  'Saga telafisiyle iptal edilen hesap',
     NULL, NULL, '', '1000000007', '10000007',
     'BILLING_ACCOUNT', 0, 'Adres doğrulaması başarısız (müşteriye ait değil).')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('billing_accounts','id'), (SELECT MAX(id) FROM billing_accounts), true);
