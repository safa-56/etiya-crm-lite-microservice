# =============================================================================
# Etiya CRM Lite - Customer Service
# Multi-stage Dockerfile
#
# Build context olarak REPO KÖKÜ kullanılmalıdır (parent pom.xml gerekli):
#   docker build -f customer-service/Dockerfile -t etiya/customer-service:1.0.0 .
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Build
# -----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

# Önce POM'ları kopyalayıp bağımlılıkları indiriyoruz (katman cache'i için).
COPY pom.xml ./
COPY customer-service/pom.xml customer-service/pom.xml
RUN mvn -B -pl customer-service -am dependency:go-offline

# Kaynak kodu kopyala ve yalnızca customer-service modülünü paketle.
COPY customer-service/src customer-service/src
RUN mvn -B -pl customer-service -am clean package -DskipTests

# -----------------------------------------------------------------------------
# Stage 2: Runtime
# -----------------------------------------------------------------------------
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

# Güvenlik: root olmayan kullanıcı ile çalıştır.
RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=build /workspace/customer-service/target/customer-service-*.jar app.jar
RUN chown spring:spring app.jar
USER spring:spring

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE="docker"
ENV CONFIG_SERVER_URL="http://config-server:8888"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
