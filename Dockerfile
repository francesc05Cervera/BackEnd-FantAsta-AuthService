FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src src

RUN mvn dependency:go-offline -B
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 7070

ENTRYPOINT ["java", "-jar", "app.jar"]