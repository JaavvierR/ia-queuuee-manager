# Etapa de construcción (queda igual)
FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src /app/src
RUN mvn package -DskipTests

FROM registry.access.redhat.com/ubi8/openjdk-21:latest

COPY --from=builder /app/target/quarkus-app/ /app/

EXPOSE 9090
ENTRYPOINT ["java", "-jar", "/app/quarkus-run.jar"]