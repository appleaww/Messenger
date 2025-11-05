FROM maven:3.9.11-openjdk-25 AS builder

COPY pom.xml .

COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:25-jdk-slim

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]