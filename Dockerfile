# =============================================================================
# Dockerfile — Loyalty Tier System
# Multi-stage build: Maven builder → lightweight JRE runtime
# =============================================================================

# ---------- Stage 1: Builder ----------
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Leverage Docker layer caching — copy POM first and resolve dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B
RUN cp target/*.jar app.jar

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]