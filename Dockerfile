# ─── Stage 1: build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Cache Maven dependencies
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q

# Build the jar (skip tests — tests run in CI, not build image)
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ─── Stage 2: runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
LABEL authors="aiiisana"
WORKDIR /app

# JVM tuning for a constrained container:
#   -XX:+UseContainerSupport   — respect cgroup memory/CPU limits
#   -XX:MaxRAMPercentage=75    — use up to 75% of container RAM for heap
#   -Djava.security.egd=...    — faster startup (avoids /dev/random blocking)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

COPY --from=build /app/target/tulpar-0.0.1-SNAPSHOT.jar tulpar.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar tulpar.jar"]
