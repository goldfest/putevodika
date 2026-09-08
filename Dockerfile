FROM maven:3.9.16-eclipse-temurin-26 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src

RUN mvn -B -DskipTests clean package \
    && cp target/putevodika-backend-0.0.1-SNAPSHOT.jar /workspace/app.jar


FROM eclipse-temurin:26-jre-noble

WORKDIR /app

COPY --from=build /workspace/app.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
