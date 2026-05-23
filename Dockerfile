# Build Stage
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests -Dcheckstyle.skip=true

# Run Stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy generated jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dserver.port=${PORT:8080}", "app.jar"]