# =============================================================================
# Etiya CRM Lite - Config Server
# Multi-stage Dockerfile
#
# 1. Aşama (build) : Maven ile bağımlılıkları çeker ve fat-jar üretir.
# 2. Aşama (runtime): Yalnızca JRE + üretilen jar; küçük ve güvenli imaj.
#
# Build context olarak REPO KÖKÜ kullanılmalıdır (parent pom.xml gerekli):
#   docker build -f config-server/Dockerfile -t etiya/config-server:1.0.0 .
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Build
# Java 25 kullanıldığı için eclipse-temurin 25 + Maven tabanlı imaj.
# -----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

# Önce yalnızca POM'ları kopyalayıp bağımlılıkları indiriyoruz.
# Bu katman, kaynak kod değişse bile POM'lar değişmedikçe cache'lenir
# ve tekrar tekrar dependency indirilmesini engeller.
COPY pom.xml ./
# Parent POM tüm modülleri listeler; reactor'ın aggregator'ı parse edebilmesi
# için TÜM modül pom.xml'leri kopyalanmalı (aksi halde "Child module ... does
# not exist" hatası). Yalnızca ilgili modülün src'si aşağıda kopyalanır.
COPY config-server/pom.xml config-server/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY gateway-server/pom.xml gateway-server/pom.xml
COPY customer-service/pom.xml customer-service/pom.xml
RUN mvn -B -pl config-server -am dependency:go-offline

# Şimdi kaynak kodu kopyalayıp yalnızca config-server modülünü paketliyoruz.
COPY config-server/src config-server/src
RUN mvn -B -pl config-server -am clean package -DskipTests

# -----------------------------------------------------------------------------
# Stage 2: Runtime
# Sadece JRE içeren, jdk'ya göre çok daha küçük ve saldırı yüzeyi az bir imaj.
# -----------------------------------------------------------------------------
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# HEALTHCHECK için curl kurulur (temurin JRE imajında wget/curl gelmez; bu yüzden
# eski wget tabanlı healthcheck komutu "not found" ile hep başarısız oluyordu).
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Güvenlik: uygulamayı root olmayan bir kullanıcı ile çalıştırıyoruz.
RUN groupadd --system spring && useradd --system --gid spring spring

# Build aşamasında üretilen çalıştırılabilir jar'ı kopyalıyoruz.
COPY --from=build /workspace/config-server/target/config-server-*.jar app.jar

# Sahipliği spring kullanıcısına ver ve ona geç.
RUN chown spring:spring app.jar
USER spring:spring

# Config server'ın dinlediği port (dokümantasyon amaçlı; publish gerekir).
EXPOSE 8888

# Actuator health endpoint'ine dayalı konteyner sağlık kontrolü.
# config-server açılışta git backend'i klonlar (clone-on-start); ilk açılış
# yavaş olabileceğinden start-period yüksek tutulur.
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=10 \
    CMD curl -fsS http://localhost:8888/actuator/health || exit 1

# Konteyner ortamına uygun JVM bayrakları (memory-aware) dışarıdan verilebilir.
ENV JAVA_OPTS=""

# İmaj varsayılan olarak 'docker' profili ile çalışır; compose/k8s'te override edilebilir.
ENV SPRING_PROFILES_ACTIVE="docker"

# Git backend adresi/kimlik bilgileri gerekirse compose/k8s'te override edilir:
#   CONFIG_GIT_URI, CONFIG_GIT_LABEL, CONFIG_GIT_USERNAME, CONFIG_GIT_PASSWORD

# exec formu: JVM PID 1 olur, sinyalleri (SIGTERM) doğru alır ve düzgün kapanır.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
