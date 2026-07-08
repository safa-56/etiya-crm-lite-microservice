-- =============================================================================
-- PostgreSQL başlangıç betiği (yalnızca boş veri hacminde İLK açılışta çalışır).
--
-- POSTGRES_DB=customerdb yalnızca customer-service veritabanını oluşturur.
-- account-service kendi veritabanına (accountdb) ihtiyaç duyduğundan burada
-- oluşturulur. Her servis kendi şemasına/Debezium slot'una sahiptir.
--
-- NOT: Bu betik container /docker-entrypoint-initdb.d/ üzerinden çalıştırılır ve
-- yalnızca postgres-data hacmi boşken devreye girer. Var olan bir kurulumda
-- accountdb'yi elle oluşturmak için: CREATE DATABASE accountdb;
-- =============================================================================

SELECT 'CREATE DATABASE accountdb'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'accountdb')\gexec
