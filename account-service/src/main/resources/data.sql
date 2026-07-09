-- =============================================================================
-- account-service OTOMATIK SEED (yalnızca 'dev' profilinde çalışır)
-- -----------------------------------------------------------------------------
-- customer_id / address_id customer-service kimliklerine referanstır (servisler
-- arası, yerel FK değil). active_product_count değerleri product-service'teki
-- AKTİF ürünlerle uyumludur. Idempotent (ON CONFLICT DO NOTHING).
--
-- Senaryolar: ACTIVE / PENDING (saga sürüyor) / CANCELLED (telafi) / PASSIVE.
-- =============================================================================

INSERT INTO billing_accounts
    (id, created_date, is_active, customer_id, account_name, account_description,
     address_id, pending_address_id, address, account_number, order_number,
     account_type, account_status, active_product_count, status_reason)
VALUES
    (1, now(), true,  1, 'Ahmet Ev Hattı',  'Birincil fatura hesabı',
     1, NULL, 'İstanbul / Bağdat Caddesi No:12 D:4', 'ACC0000000001', 'ORD00000001',
     'BILLING_ACCOUNT', 'ACTIVE', 2, NULL),
    (2, now(), true,  1, 'Ahmet İş Hattı',  'İkincil fatura hesabı (iş adresi)',
     2, NULL, 'Ankara / Atatürk Bulvarı No:88', 'ACC0000000002', 'ORD00000002',
     'BILLING_ACCOUNT', 'ACTIVE', 1, NULL),
    (3, now(), true,  2, 'Ayşe Ev Hattı',   'Birincil fatura hesabı',
     3, NULL, 'İzmir / Kordon No:5 D:2', 'ACC0000000003', 'ORD00000003',
     'BILLING_ACCOUNT', 'ACTIVE', 1, NULL),
    (4, now(), true,  3, 'Mehmet Ev Hattı', 'Aktif ama ürünsüz hesap (silinebilir)',
     4, NULL, 'Bursa / Nilüfer Caddesi No:34', 'ACC0000000004', 'ORD00000004',
     'BILLING_ACCOUNT', 'ACTIVE', 0, NULL),
    (5, now(), true,  4, 'Fatma Yeni Hat',  'Oluşturma sagası beklemede',
     6, NULL, '', 'ACC0000000005', 'ORD00000005',
     'BILLING_ACCOUNT', 'PENDING', 0, NULL),
    (6, now(), false, 5, 'Can Eski Hat',    'Pasifleştirilmiş hesap',
     7, NULL, 'Ankara / Tunalı Hilmi No:45 D:3', 'ACC0000000006', 'ORD00000006',
     'BILLING_ACCOUNT', 'PASSIVE', 0, NULL),
    (7, now(), false, 2, 'Ayşe İptal Hat',  'Saga telafisiyle iptal edilen hesap',
     NULL, NULL, '', 'ACC0000000007', 'ORD00000007',
     'BILLING_ACCOUNT', 'CANCELLED', 0, 'Adres doğrulaması başarısız (müşteriye ait değil).')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('billing_accounts','id'), (SELECT MAX(id) FROM billing_accounts), true);
