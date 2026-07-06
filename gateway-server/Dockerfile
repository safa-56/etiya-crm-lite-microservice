# =============================================================================
# Etiya CRM Lite - Gateway Server
# Multi-stage Dockerfile
#
# 1. Aşama (build) : Maven ile bağımlılıkları çeker ve fat-jar üretir.
# 2. Aşama (runtime): Yalnızca JRE + üretilen jar; küçük ve güvenli imaj.
#
# Build context olarak REPO KÖKÜ kullanılmalıdır (parent pom.xml gerekli):
#   docker build -f gateway-server/Dockerfile -t etiya/gateway-server:1.0.0 .
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
COPY gateway-server/pom.xml gateway-server/pom.xml
RUN mvn -B -pl gateway-server -am dependency:go-offline

# Şimdi kaynak kodu kopyalayıp yalnızca gateway-server modülünü paketliyoruz.
COPY gateway-server/src gateway-server/src
RUN mvn -B -pl gateway-server -am clean package -DskipTests

# -----------------------------------------------------------------------------
# Stage 2: Runtime
# Sadece JRE içeren, jdk'ya göre çok daha küçük ve saldırı yüzeyi az bir imaj.
# -----------------------------------------------------------------------------
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Güvenlik: uygulamayı root olmayan bir kullanıcı ile çalıştırıyoruz.
RUN groupadd --system spring && useradd --system --gid spring spring

# Build aşamasında üretilen çalıştırılabilir jar'ı kopyalıyoruz.
COPY --from=build /workspace/gateway-server/target/gateway-server-*.jar app.jar

# Sahipliği spring kullanıcısına ver ve ona geç.
RUN chown spring:spring app.jar
USER spring:spring

# Gateway'in dinlediği port (dokümantasyon amaçlı; publish gerekir).
EXPOSE 8080

# Actuator health endpoint'ine dayalı konteyner sağlık kontrolü.
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Konteyner ortamına uygun JVM bayrakları (memory-aware) dışarıdan verilebilir.
ENV JAVA_OPTS=""

# İmaj varsayılan olarak 'docker' profili ile çalışır; compose/k8s'te override edilebilir.
ENV SPRING_PROFILES_ACTIVE="docker"

# exec formu: JVM PID 1 olur, sinyalleri (SIGTERM) doğru alır ve düzgün kapanır.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
