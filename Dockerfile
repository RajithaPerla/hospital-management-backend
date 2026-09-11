# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only the POM first so Docker can cache the dependency download
# layer across builds when only source files change.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Render injects PORT at runtime; application.properties already reads
# server.port=${PORT:8080}, so nothing else to configure here.
COPY --from=build /app/target/hospital-management-backend-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
