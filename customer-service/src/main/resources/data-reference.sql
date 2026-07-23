-- =============================================================================
-- customer-service REFERANS SEED — 'docker' profilinde çalışır.
--
-- docker-compose ile ayağa kalkan ortam BOŞ veriyle başlar: yalnızca uygulamanın
-- çalışması için zorunlu olan referans/iskelet tablolar tohumlanır
-- (general_status, general_type, party_role_types, parties, party_roles).
-- İş verisi (customers, individual_customers, addresses, customer_contact_info)
-- tohumlanmaz; API üzerinden oluşturulur.
--
-- Referans satırları olmadan servis açılır ama ilk yazma işleminde
-- ReferenceDataService "referans veri bulunamadı" hatası verir; bu yüzden bu
-- dosya docker'da zorunludur.
--
-- Dev profili tam seed'i (iş verisiyle birlikte) data.sql'den okur; iki dosya
-- referans bölümünde AYNI id'leri kullanır, biri değişirse diğeri de değişmelidir.
-- =============================================================================

-- =============================================================================
-- (aşağısı data.sql'in referans bölümüyle birebir aynıdır)
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
--
-- DURUM MODELİ: Entity'ler ayrı bir durum kolonu (is_active / status_id) TAŞIMAZ;
-- her kaydın durumu general_status tablosuna general_status_id FK'si ile bağlanır.
-- Referans tabloları (general_status, general_type, party_role_types) durum taşımaz.
-- =============================================================================

-- =============================================================================
-- REFERANS VERİ (Bounded Context Ownership)
-- -----------------------------------------------------------------------------
-- Legacy'de GNL_ST/GNL_TP kurum genelinde TEK tablodur; satırlar ENT_CODE_NAME
-- ile bölümlenir. Mikroserviste HER SERVİS KENDİ DİLİMİNE SAHİPTİR:
--   PARTY / PARTY_ROLE / IND  -> customer-service   (bu dosya)
--   PROD / PROD_SPEC / ...    -> product-service
--   CUST_ORD                  -> order-service
--   CUST_ACCT / ...           -> account-service
-- =============================================================================

-- --- GNL_ST dilimi: PARTY (16-18), PARTY_ROLE (79-81), IND (156-158) ---------
-- id'ler legacy GNL_ST tablosundan birebir alınmıştır.
INSERT INTO general_status
    (id, created_date, name, description, short_code, entity_code_name, entity_name)
VALUES
    ( 16, now(), 'Silinmis', 'Silinmis', 'DEL',  'PARTY',      'PARTY'),
    ( 17, now(), 'Aktif',    'Aktif',    'ACTV', 'PARTY',      'PARTY'),
    ( 18, now(), 'Pasif',    'Pasif',    'PASS', 'PARTY',      'PARTY'),
    ( 79, now(), 'Silinmis', 'Silinmis', 'DEL',  'PARTY_ROLE', 'PARTY_ROLE'),
    ( 80, now(), 'Aktif',    'Aktif',    'ACTV', 'PARTY_ROLE', 'PARTY_ROLE'),
    ( 81, now(), 'Pasif',    'Pasif',    'PASS', 'PARTY_ROLE', 'PARTY_ROLE'),
    (156, now(), 'Silinmis', 'Silinmis', 'DEL',  'IND',        'INDIVIDUAL'),
    (157, now(), 'Aktif',    'Aktif',    'ACTV', 'IND',        'INDIVIDUAL'),
    (158, now(), 'Pasif',    'Pasif',    'PASS', 'IND',        'INDIVIDUAL')
ON CONFLICT (id) DO NOTHING;

-- --- Yeni dilimler: durum FK'si veren entity'ler için (legacy dökümü yok) -----
-- id'ler legacy GNL_ST aralığıyla çakışmayacak biçimde yüksek bloktan verildi.
INSERT INTO general_status
    (id, created_date, name, description, short_code, entity_code_name, entity_name)
VALUES
    (300101, now(), 'Aktif',    'Aktif',    'ACTV', 'ADDRESS',      'ADDRESS'),
    (300102, now(), 'Silinmis', 'Silinmis', 'DEL',  'ADDRESS',      'ADDRESS'),
    (300201, now(), 'Aktif',    'Aktif',    'ACTV', 'CUST_CONTACT', 'CUST_CONTACT'),
    (300202, now(), 'Silinmis', 'Silinmis', 'DEL',  'CUST_CONTACT', 'CUST_CONTACT'),
    (300301, now(), 'Aktif',    'Aktif',    'ACTV', 'SYS_USER',     'SYS_USER'),
    (300302, now(), 'Pasif',    'Pasif',    'PASS', 'SYS_USER',     'SYS_USER'),
    (300303, now(), 'Silinmis', 'Silinmis', 'DEL',  'SYS_USER',     'SYS_USER')
ON CONFLICT (id) DO NOTHING;

-- --- GNL_TP dilimi: CAM_PARTY_TYPE (party tipi) ------------------------------
-- id'ler ve kodlar legacy GNL_TP tablosundan birebir alınmıştır.
INSERT INTO general_type
    (id, created_date, name, description, short_code, entity_code_name, entity_name)
VALUES
    (164, now(), 'Bireysel', 'Bireysel', 'INDV', 'CAM_PARTY_TYPE', NULL),
    (163, now(), 'Kurumsal', 'Kurumsal', 'ORG',  'CAM_PARTY_TYPE', NULL)
ON CONFLICT (id) DO NOTHING;

-- --- PARTY_ROLE_TP: party rol tipleri ---------------------------------------
-- Bir party birden çok rol taşıyabilir: aynı kişi hem müşterimiz (CUST) hem de
-- sisteme giriş yapan kullanıcımız (USER) olabilir. USER rolü Keycloak'taki bir
-- kullanıcının domain karşılığıdır ve ilk girişte otomatik oluşturulur
-- (bkz. SystemUserProvisioningFilter).
INSERT INTO party_role_types
    (id, created_date, name, description, short_code)
VALUES
    (1, now(), 'Müşteri',  'Müşteri rolü',          'CUST'),
    (2, now(), 'Kullanıcı', 'Sistem kullanıcısı rolü', 'USER')
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- PARTY ZİNCİRİ: parties -> party_roles -> customers
-- -----------------------------------------------------------------------------
-- Hizalama kasıtlıdır: customer N <-> party_role N <-> party N.
-- =============================================================================

-- party_type_id = 164 (CAM_PARTY_TYPE/INDV), general_status_id = 17 (PARTY/ACTV)
INSERT INTO parties (id, created_date, party_type_id, general_status_id) VALUES
    (1, now(), 164, 17), (2, now(), 164, 17), (3, now(), 164, 17),
    (4, now(), 164, 17), (5, now(), 164, 17), (6, now(), 164, 17)
ON CONFLICT (id) DO NOTHING;

-- party_role_type_id = 1 (CUST), general_status_id = 80 (PARTY_ROLE/ACTV)
INSERT INTO party_roles (id, created_date, party_id, party_role_type_id, general_status_id) VALUES
    (1, now(), 1, 1, 80), (2, now(), 2, 1, 80), (3, now(), 3, 1, 80),
    (4, now(), 4, 1, 80), (5, now(), 5, 1, 80), (6, now(), 6, 1, 80)
ON CONFLICT (id) DO NOTHING;

-- identity sekanslarını en büyük id'ye çek (uygulamanın sonraki insert'leri çakışmasın)
SELECT setval(pg_get_serial_sequence('general_status','id'),   (SELECT MAX(id) FROM general_status), true);
SELECT setval(pg_get_serial_sequence('general_type','id'),     (SELECT MAX(id) FROM general_type), true);
SELECT setval(pg_get_serial_sequence('party_role_types','id'), (SELECT MAX(id) FROM party_role_types), true);
SELECT setval(pg_get_serial_sequence('parties','id'),          (SELECT MAX(id) FROM parties), true);
SELECT setval(pg_get_serial_sequence('party_roles','id'),      (SELECT MAX(id) FROM party_roles), true);
