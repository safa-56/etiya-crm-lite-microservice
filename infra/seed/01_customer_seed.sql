-- =============================================================================
-- customer-service SEED  (veritabanı: customerdb)
-- -----------------------------------------------------------------------------
-- Mevcut domain verisini temizler ve test senaryoları için deterministik veri
-- yükler. Outbox/Inbox tablolarına dokunulmaz. Kimlikler (id) sabittir; böylece
-- account/product seed'leri bu id'lere güvenle referans verebilir.
--
-- DURUM MODELİ: Entity'ler is_active kolonu TAŞIMAZ; durum general_status'a
-- general_status_id FK'si ile bağlanır. Bu seed, FK verdiği general_status
-- dilimlerini de (idempotent) tohumlar.
--
-- Çalıştırma (infra çalışırken):
--   docker exec -i crm-postgres psql -U postgres -d customerdb < infra/seed/01_customer_seed.sql
-- =============================================================================

BEGIN;

-- --- Şema uyumu (eski model -> yeni model). Idempotent; yeni şemada no-op. -----
ALTER TABLE customers            ADD COLUMN IF NOT EXISTS general_status_id bigint;
ALTER TABLE addresses            ADD COLUMN IF NOT EXISTS general_status_id bigint;
ALTER TABLE customer_contact_info ADD COLUMN IF NOT EXISTS general_status_id bigint;

TRUNCATE TABLE customer_contact_info, addresses, individual_customers, customers
    RESTART IDENTITY CASCADE;

-- --- general_status dilimleri (durum FK hedefleri; idempotent) ---------------
-- general_status TRUNCATE EDİLMEZ; yalnızca eksik satırlar eklenir. id'ler
-- customer-service data.sql ile birebir aynıdır.
INSERT INTO general_status
    (id, created_date, name, description, short_code, entity_code_name, entity_name)
VALUES
    (   157, now(), 'Aktif',    'Aktif',    'ACTV', 'IND',          'INDIVIDUAL'),
    (   156, now(), 'Silinmis', 'Silinmis', 'DEL',  'IND',          'INDIVIDUAL'),
    (300101, now(), 'Aktif',    'Aktif',    'ACTV', 'ADDRESS',      'ADDRESS'),
    (300102, now(), 'Silinmis', 'Silinmis', 'DEL',  'ADDRESS',      'ADDRESS'),
    (300201, now(), 'Aktif',    'Aktif',    'ACTV', 'CUST_CONTACT', 'CUST_CONTACT'),
    (300202, now(), 'Silinmis', 'Silinmis', 'DEL',  'CUST_CONTACT', 'CUST_CONTACT')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('general_status','id'), (SELECT MAX(id) FROM general_status), true);

-- --- customers (JOINED kök tablo: audit alanları burada) --------------------
-- general_status_id = 157 (IND/ACTV)
INSERT INTO customers (id, created_date, general_status_id) VALUES
    (1, now(), 157),
    (2, now(), 157),
    (3, now(), 157),
    (4, now(), 157),
    (5, now(), 157),
    (6, now(), 157);

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
-- general_status_id = 300101 (ADDRESS/ACTV)
INSERT INTO addresses
    (id, created_date, general_status_id, customer_id, city, street, house_number, address_description, is_primary)
VALUES
    (1, now(), 300101, 1, 'İstanbul', 'Bağdat Caddesi',      'No:12 D:4',  'Kadıköy - ev adresi',       true),
    (2, now(), 300101, 1, 'Ankara',   'Atatürk Bulvarı',     'No:88',      'Çankaya - iş adresi',        false),
    (3, now(), 300101, 2, 'İzmir',    'Kordon',              'No:5 D:2',   'Konak - ev adresi',          true),
    (4, now(), 300101, 3, 'Bursa',    'Nilüfer Caddesi',     'No:34',      'Nilüfer - ev adresi',        true),
    (5, now(), 300101, 3, 'Antalya',  'Lara Yolu',           'No:120',     'Muratpaşa - yazlık',         false),
    (6, now(), 300101, 4, 'İstanbul', 'İstiklal Caddesi',    'No:200 D:9', 'Beyoğlu - ev adresi',        true),
    (7, now(), 300101, 5, 'Ankara',   'Tunalı Hilmi',        'No:45 D:3',  'Kavaklıdere - ev adresi',    true),
    (8, now(), 300101, 6, 'Konya',    'Mevlana Caddesi',     'No:7',       'Selçuklu - ev adresi',       true);

-- --- customer_contact_info --------------------------------------------------
-- general_status_id = 300201 (CUST_CONTACT/ACTV)
INSERT INTO customer_contact_info
    (id, created_date, general_status_id, customer_id, email, home_phone, mobil_phone, fax)
VALUES
    (1, now(), 300201, 1, 'ahmet.yilmaz@example.com',  '02161112233', '05321112233', NULL),
    (2, now(), 300201, 2, 'ayse.demir@example.com',    NULL,          '05334445566', NULL),
    (3, now(), 300201, 3, 'mehmet.kaya@example.com',   '02247778899', '05357778899', '02247778800'),
    (4, now(), 300201, 4, 'fatma.sahin@example.com',   NULL,          '05369990011', NULL),
    (5, now(), 300201, 5, 'can.ozturk@example.com',    NULL,          '05312223344', NULL),
    (6, now(), 300201, 6, 'zeynep.aydin@example.com',  '03325556677', '05345556677', NULL);

-- --- identity sekanslarını en büyük id'ye çek (uygulama insert'leri çakışmasın)
SELECT setval(pg_get_serial_sequence('customers','id'),           (SELECT MAX(id) FROM customers), true);
SELECT setval(pg_get_serial_sequence('addresses','id'),           (SELECT MAX(id) FROM addresses), true);
SELECT setval(pg_get_serial_sequence('customer_contact_info','id'),(SELECT MAX(id) FROM customer_contact_info), true);

COMMIT;
