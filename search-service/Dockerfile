# =============================================================================
# Etiya CRM Lite - Search Service
# Multi-stage Dockerfile
#
# Build context olarak REPO KÖKÜ kullanılmalıdır (parent pom.xml gerekli):
#   docker build -f search-service/Dockerfile -t etiya/search-service:1.0.0 .
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Build
# -----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

# Önce POM'ları kopyalayıp bağımlılıkları indiriyoruz (katman cache'i için).
COPY pom.xml ./
# Parent POM tüm modülleri listeler; reactor'ın aggregator'ı parse edebilmesi
# için TÜM modül pom.xml'leri kopyalanmalı (aksi halde "Child module ... does
# not exist" hatası). Yalnızca ilgili modülün src'si aşağıda kopyalanır.
COPY config-server/pom.xml config-server/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY gateway-server/pom.xml gateway-server/pom.xml
COPY customer-service/pom.xml customer-service/pom.xml
COPY account-service/pom.xml account-service/pom.xml
COPY product-service/pom.xml product-service/pom.xml
COPY cart-service/pom.xml cart-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY search-service/pom.xml search-service/pom.xml
COPY bff-service/pom.xml bff-service/pom.xml
RUN mvn -B -pl search-service -am dependency:go-offline

# Kaynak kodu kopyala ve yalnızca search-service modülünü paketle.
COPY search-service/src search-service/src
RUN mvn -B -pl search-service -am clean package -DskipTests

# -----------------------------------------------------------------------------
# Stage 2: Runtime
# -----------------------------------------------------------------------------
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Güvenlik: root olmayan kullanıcı ile çalıştır.
# HEALTHCHECK için curl kurulur (temurin JRE imajında wget/curl gelmez).
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=build /workspace/search-service/target/search-service-*.jar app.jar
RUN chown spring:spring app.jar
USER spring:spring

EXPOSE 8087

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=10 \
    CMD curl -fsS http://localhost:8087/actuator/health || exit 1

ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE="docker"
ENV CONFIG_SERVER_URL="http://config-server:8888"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
