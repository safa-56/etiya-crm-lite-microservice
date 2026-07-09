-- =============================================================================
-- PostgreSQL başlangıç betiği (yalnızca boş veri hacminde İLK açılışta çalışır).
--
-- POSTGRES_DB=customerdb yalnızca customer-service veritabanını oluşturur.
-- account-service (accountdb), product-service (productdb) ve cart-service (cartdb)
-- kendi veritabanlarına ihtiyaç duyduğundan burada oluşturulur. Her servis kendi
-- şemasına/Debezium slot'una sahiptir.
--
-- NOT: Bu betik container /docker-entrypoint-initdb.d/ üzerinden çalıştırılır ve
-- yalnızca postgres-data hacmi boşken devreye girer. Var olan bir kurulumda
-- veritabanlarını elle oluşturmak için: CREATE DATABASE accountdb; / CREATE DATABASE productdb; / CREATE DATABASE cartdb;
-- =============================================================================

SELECT 'CREATE DATABASE accountdb'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'accountdb')\gexec

SELECT 'CREATE DATABASE productdb'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'productdb')\gexec

SELECT 'CREATE DATABASE cartdb'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cartdb')\gexec
