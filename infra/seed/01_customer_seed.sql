-- =============================================================================
-- customer-service SEED  (veritabanı: customerdb)
-- -----------------------------------------------------------------------------
-- Mevcut domain verisini temizler ve test senaryoları için deterministik veri
-- yükler. Outbox/Inbox tablolarına dokunulmaz. Kimlikler (id) sabittir; böylece
-- account/product seed'leri bu id'lere güvenle referans verebilir.
--
-- Çalıştırma (infra çalışırken):
--   docker exec -i crm-postgres psql -U postgres -d customerdb < infra/seed/01_customer_seed.sql
-- =============================================================================

BEGIN;

TRUNCATE TABLE customer_contact_info, addresses, individual_customers, customers
    RESTART IDENTITY CASCADE;

-- --- customers (JOINED kök tablo: audit alanları burada) --------------------
INSERT INTO customers (id, created_date, is_active) VALUES
    (1, now(), true),
    (2, now(), true),
    (3, now(), true),
    (4, now(), true),
    (5, now(), true),
    (6, now(), true);

-- --- individual_customers (customers.id ile 1-1) ----------------------------
INSERT INTO individual_customers
    (id, nationality_id, first_name, second_name, last_name, birth_date, father_name, mother_name, gender_type)
VALUES
    (1, '10000000146', 'Ahmet',  NULL,    'Yılmaz',  DATE '1985-03-12', 'Mustafa', 'Hatice',  'MALE'),
    (2, '10000000278', 'Ayşe',   NULL,    'Demir',   DATE '1990-07-24', 'Kemal',   'Sevgi',   'FEMALE'),
    (3, '10000000315', 'Mehmet', 'Ali',   'Kaya',    DATE '1978-11-02', 'Hasan',   'Fadime',  'MALE'),
    (4, '10000000453', 'Fatma',  NULL,    'Şahin',   DATE '1995-01-19', 'İbrahim', 'Emine',   'FEMALE'),
    (5, '10000000591', 'Can',    NULL,    'Öztürk',  DATE '2000-09-30', 'Osman',   'Zehra',   'MALE'),
    (6, '10000000638', 'Zeynep', 'Nur',   'Aydın',   DATE '1988-05-05', 'Ahmet',   'Meryem',  'FEMALE');

-- --- addresses --------------------------------------------------------------
-- cust1: birincil (İstanbul) + ikincil (Ankara); diğerlerinde tekil/çift adres
INSERT INTO addresses
    (id, created_date, is_active, customer_id, city, street, house_number, address_description, is_primary)
VALUES
    (1, now(), true, 1, 'İstanbul', 'Bağdat Caddesi',      'No:12 D:4',  'Kadıköy - ev adresi',       true),
    (2, now(), true, 1, 'Ankara',   'Atatürk Bulvarı',     'No:88',      'Çankaya - iş adresi',        false),
    (3, now(), true, 2, 'İzmir',    'Kordon',              'No:5 D:2',   'Konak - ev adresi',          true),
    (4, now(), true, 3, 'Bursa',    'Nilüfer Caddesi',     'No:34',      'Nilüfer - ev adresi',        true),
    (5, now(), true, 3, 'Antalya',  'Lara Yolu',           'No:120',     'Muratpaşa - yazlık',         false),
    (6, now(), true, 4, 'İstanbul', 'İstiklal Caddesi',    'No:200 D:9', 'Beyoğlu - ev adresi',        true),
    (7, now(), true, 5, 'Ankara',   'Tunalı Hilmi',        'No:45 D:3',  'Kavaklıdere - ev adresi',    true),
    (8, now(), true, 6, 'Konya',    'Mevlana Caddesi',     'No:7',       'Selçuklu - ev adresi',       true);

-- --- customer_contact_info --------------------------------------------------
INSERT INTO customer_contact_info
    (id, created_date, is_active, customer_id, email, home_phone, mobil_phone, fax)
VALUES
    (1, now(), true, 1, 'ahmet.yilmaz@example.com',  '02161112233', '05321112233', NULL),
    (2, now(), true, 2, 'ayse.demir@example.com',    NULL,          '05334445566', NULL),
    (3, now(), true, 3, 'mehmet.kaya@example.com',   '02247778899', '05357778899', '02247778800'),
    (4, now(), true, 4, 'fatma.sahin@example.com',   NULL,          '05369990011', NULL),
    (5, now(), true, 5, 'can.ozturk@example.com',    NULL,          '05312223344', NULL),
    (6, now(), true, 6, 'zeynep.aydin@example.com',  '03325556677', '05345556677', NULL);

-- --- identity sekanslarını en büyük id'ye çek (uygulama insert'leri çakışmasın)
SELECT setval(pg_get_serial_sequence('customers','id'),           (SELECT MAX(id) FROM customers), true);
SELECT setval(pg_get_serial_sequence('addresses','id'),           (SELECT MAX(id) FROM addresses), true);
SELECT setval(pg_get_serial_sequence('customer_contact_info','id'),(SELECT MAX(id) FROM customer_contact_info), true);

COMMIT;
