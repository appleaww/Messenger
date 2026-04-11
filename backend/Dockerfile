FROM maven:3.9.12-eclipse-temurin-25 AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true --no-transfer-progress

FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app

ARG OTEL_AGENT_VERSION=2.26.0

RUN apk add --no-cache curl && \
    curl -L -f --retry 3 --max-time 30 \
      -o /app/opentelemetry-javaagent.jar \
      "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar" && \
    chmod 644 /app/opentelemetry-javaagent.jar && \
    apk del curl
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
            "-javaagent:/app/opentelemetry-javaagent.jar", \
            "-Dotel.javaagent.debug=false", \
            "-Dotel.instrumentation.micrometer.enabled=true",\
            "-Dotel.metric.export.interval=15000", \
            "-jar", \
            "app.jar"]