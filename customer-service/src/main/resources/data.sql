-- =============================================================================
-- customer-service OTOMATIK SEED (yalnızca 'dev' profilinde çalışır)
-- -----------------------------------------------------------------------------
-- Spring Boot, Hibernate şemayı oluşturduktan sonra bu betiği çalıştırır
-- (spring.jpa.defer-datasource-initialization=true, spring.sql.init.mode=always).
-- Idempotenttir: ON CONFLICT (id) DO NOTHING sayesinde boş DB'de doldurur,
-- dolu DB'de tekrar basmaz (her restart güvenli). Kimlikler sabittir; account/
-- product seed'leri bu id'lere referans verir.
--
-- SIRALAMA ÖNEMLİ: referans veri -> parties -> party_roles -> customers, çünkü
-- customers.party_role_id bu zincire FK verir.
-- =============================================================================

-- =============================================================================
-- REFERANS VERİ (Bounded Context Ownership)
-- -----------------------------------------------------------------------------
-- Legacy'de GNL_ST/GNL_TP kurum genelinde TEK tablodur; satırlar ENT_CODE_NAME
-- ile bölümlenir. Mikroserviste bu tabloyu paylaşmak "shared database" anti
-- pattern'i olurdu. Bunun yerine HER SERVİS KENDİ DİLİMİNE SAHİPTİR:
--
--   PARTY / PARTY_ROLE / IND  -> customer-service   (bu dosya)
--   PROD / PROD_SPEC / ...    -> product-service
--   CUST_ORD                  -> order-service
--   CUST_ACCT / ...           -> account-service
--
-- Dilimler ayrık olduğundan legacy id'ler ÇAKIŞMADAN birebir korunabilir; bu da
-- ileride legacy DB'den ETL/migrasyonu kolaylaştırır.
-- =============================================================================

-- --- GNL_ST dilimi: PARTY (16-18), PARTY_ROLE (79-81), IND (156-158) ---------
-- id'ler legacy GNL_ST tablosundan birebir alınmıştır.
INSERT INTO general_status
    (id, created_date, is_active, name, description, short_code, entity_code_name, entity_name)
VALUES
    ( 16, now(), true, 'Silinmis', 'Silinmis', 'DEL',  'PARTY',      'PARTY'),
    ( 17, now(), true, 'Aktif',    'Aktif',    'ACTV', 'PARTY',      'PARTY'),
    ( 18, now(), true, 'Pasif',    'Pasif',    'PASS', 'PARTY',      'PARTY'),
    ( 79, now(), true, 'Silinmis', 'Silinmis', 'DEL',  'PARTY_ROLE', 'PARTY_ROLE'),
    ( 80, now(), true, 'Aktif',    'Aktif',    'ACTV', 'PARTY_ROLE', 'PARTY_ROLE'),
    ( 81, now(), true, 'Pasif',    'Pasif',    'PASS', 'PARTY_ROLE', 'PARTY_ROLE'),
    -- IND dilimi: bu servisin sahipliğindedir. IndividualCustomer'a henüz durum
    -- alanı eklenmedi; satırlar dilim bütünlüğü ve ETL hizası için tutulur.
    (156, now(), true, 'Silinmis', 'Silinmis', 'DEL',  'IND',        'INDIVIDUAL'),
    (157, now(), true, 'Aktif',    'Aktif',    'ACTV', 'IND',        'INDIVIDUAL'),
    (158, now(), true, 'Pasif',    'Pasif',    'PASS', 'IND',        'INDIVIDUAL')
ON CONFLICT (id) DO NOTHING;

-- --- GNL_TP dilimi: CAM_PARTY_TYPE (party tipi) ------------------------------
-- id'ler ve kodlar legacy GNL_TP tablosundan birebir alınmıştır.
--
-- Dikkat edilecek üç nokta:
--   1) Tipler GNL_ST'deki 'PARTY' diliminde DEĞİL, 'CAM_PARTY_TYPE' dilimindedir.
--      (Durum -> PARTY, tip -> CAM_PARTY_TYPE; ayrı isim alanları.)
--   2) Bireysel'in kısa kodu 'INDV'dir ('IND' değil; 'IND' GNL_ST'de birey
--      DURUMLARININ dilim adıdır).
--   3) ENT_NAME legacy'de NULL'dır; ayrım ENT_CODE_NAME ile yapılır.
--
-- NOT: GNL_TP id 164 ile GNL_ST id 164 farklı TABLOLARDADIR; çakışma değildir.
INSERT INTO general_type
    (id, created_date, is_active, name, description, short_code, entity_code_name, entity_name)
VALUES
    (164, now(), true, 'Bireysel', 'Bireysel', 'INDV', 'CAM_PARTY_TYPE', NULL),
    (163, now(), true, 'Kurumsal', 'Kurumsal', 'ORG',  'CAM_PARTY_TYPE', NULL)
ON CONFLICT (id) DO NOTHING;

-- --- PARTY_ROLE_TP: party rol tipleri ---------------------------------------
INSERT INTO party_role_types
    (id, created_date, is_active, name, description, short_code)
VALUES
    (1, now(), true, 'Müşteri', 'Müşteri rolü', 'CUST')
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- PARTY ZİNCİRİ: parties -> party_roles -> customers
-- -----------------------------------------------------------------------------
-- Mevcut 6 müşterinin her biri için bir bireysel party ve onun üzerinde bir
-- CUST rolü oluşturulur. Hizalama kasıtlıdır: customer N <-> party_role N <-> party N.
-- =============================================================================

-- party_type_id = 164 (CAM_PARTY_TYPE/INDV), status_id = 17 (PARTY/ACTV)
INSERT INTO parties (id, created_date, is_active, party_type_id, status_id) VALUES
    (1, now(), true, 164, 17), (2, now(), true, 164, 17), (3, now(), true, 164, 17),
    (4, now(), true, 164, 17), (5, now(), true, 164, 17), (6, now(), true, 164, 17)
ON CONFLICT (id) DO NOTHING;

-- party_role_type_id = 1 (CUST), status_id = 80 (PARTY_ROLE/ACTV)
INSERT INTO party_roles (id, created_date, is_active, party_id, party_role_type_id, status_id) VALUES
    (1, now(), true, 1, 1, 80), (2, now(), true, 2, 1, 80), (3, now(), true, 3, 1, 80),
    (4, now(), true, 4, 1, 80), (5, now(), true, 5, 1, 80), (6, now(), true, 6, 1, 80)
ON CONFLICT (id) DO NOTHING;

INSERT INTO customers (id, created_date, is_active, party_role_id) VALUES
    (1, now(), true, 1), (2, now(), true, 2), (3, now(), true, 3),
    (4, now(), true, 4), (5, now(), true, 5), (6, now(), true, 6)
ON CONFLICT (id) DO NOTHING;

-- Backfill (expand-migrate-contract'ın "migrate" adımı): party_role_id kolonu
-- dolu bir tabloya sonradan eklendiğinden, ON CONFLICT DO NOTHING yüzünden
-- yukarıdaki INSERT'in atlandığı mevcut DB'lerde eski satırlar NULL kalır.
-- Hizalama customer N <-> party_role N olduğu için id eşlemesi yeterlidir.
UPDATE customers SET party_role_id = id
WHERE id BETWEEN 1 AND 6 AND party_role_id IS NULL;

INSERT INTO individual_customers
    (id, nationality_id, first_name, second_name, last_name, birth_date, father_name, mother_name, gender_type)
VALUES
    (1, '10000000146', 'Ahmet',  NULL,  'Yılmaz',  DATE '1985-03-12', 'Mustafa', 'Hatice',  'MALE'),
    (2, '10000000278', 'Ayşe',   NULL,  'Demir',   DATE '1990-07-24', 'Kemal',   'Sevgi',   'FEMALE'),
    (3, '10000000315', 'Mehmet', 'Ali', 'Kaya',    DATE '1978-11-02', 'Hasan',   'Fadime',  'MALE'),
    (4, '10000000453', 'Fatma',  NULL,  'Şahin',   DATE '1995-01-19', 'İbrahim', 'Emine',   'FEMALE'),
    (5, '10000000591', 'Can',    NULL,  'Öztürk',  DATE '2000-09-30', 'Osman',   'Zehra',   'MALE'),
    (6, '10000000638', 'Zeynep', 'Nur', 'Aydın',   DATE '1988-05-05', 'Ahmet',   'Meryem',  'FEMALE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO addresses
    (id, created_date, is_active, customer_id, city, street, house_number, address_description, is_primary)
VALUES
    (1, now(), true, 1, 'İstanbul', 'Bağdat Caddesi',   'No:12 D:4',  'Kadıköy - ev adresi',    true),
    (2, now(), true, 1, 'Ankara',   'Atatürk Bulvarı',  'No:88',      'Çankaya - iş adresi',    false),
    (3, now(), true, 2, 'İzmir',    'Kordon',           'No:5 D:2',   'Konak - ev adresi',      true),
    (4, now(), true, 3, 'Bursa',    'Nilüfer Caddesi',  'No:34',      'Nilüfer - ev adresi',    true),
    (5, now(), true, 3, 'Antalya',  'Lara Yolu',        'No:120',     'Muratpaşa - yazlık',     false),
    (6, now(), true, 4, 'İstanbul', 'İstiklal Caddesi', 'No:200 D:9', 'Beyoğlu - ev adresi',    true),
    (7, now(), true, 5, 'Ankara',   'Tunalı Hilmi',     'No:45 D:3',  'Kavaklıdere - ev adresi',true),
    (8, now(), true, 6, 'Konya',    'Mevlana Caddesi',  'No:7',       'Selçuklu - ev adresi',   true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO customer_contact_info
    (id, created_date, is_active, customer_id, email, home_phone, mobil_phone, fax)
VALUES
    (1, now(), true, 1, 'ahmet.yilmaz@example.com', '02161112233', '05321112233', NULL),
    (2, now(), true, 2, 'ayse.demir@example.com',   NULL,          '05334445566', NULL),
    (3, now(), true, 3, 'mehmet.kaya@example.com',  '02247778899', '05357778899', '02247778800'),
    (4, now(), true, 4, 'fatma.sahin@example.com',  NULL,          '05369990011', NULL),
    (5, now(), true, 5, 'can.ozturk@example.com',   NULL,          '05312223344', NULL),
    (6, now(), true, 6, 'zeynep.aydin@example.com', '03325556677', '05345556677', NULL)
ON CONFLICT (id) DO NOTHING;

-- identity sekanslarını en büyük id'ye çek (uygulamanın sonraki insert'leri çakışmasın)
SELECT setval(pg_get_serial_sequence('general_status','id'),    (SELECT MAX(id) FROM general_status), true);
SELECT setval(pg_get_serial_sequence('general_type','id'),      (SELECT MAX(id) FROM general_type), true);
SELECT setval(pg_get_serial_sequence('party_role_types','id'),  (SELECT MAX(id) FROM party_role_types), true);
SELECT setval(pg_get_serial_sequence('parties','id'),           (SELECT MAX(id) FROM parties), true);
SELECT setval(pg_get_serial_sequence('party_roles','id'),       (SELECT MAX(id) FROM party_roles), true);
SELECT setval(pg_get_serial_sequence('customers','id'),            (SELECT MAX(id) FROM customers), true);
SELECT setval(pg_get_serial_sequence('addresses','id'),            (SELECT MAX(id) FROM addresses), true);
SELECT setval(pg_get_serial_sequence('customer_contact_info','id'),(SELECT MAX(id) FROM customer_contact_info), true);
