# syntax=docker/dockerfile:1

# Run from the repository root:
# docker build -f nsa2-gateway/src/test/java/br/com/pradolabs/nsa2_gateway/Dockerfile -t nsa2-gateway .

# Java 21 is required because virtual threads are enabled in the application.
ARG JAVA_VERSION=21

FROM eclipse-temurin:${JAVA_VERSION}-jdk-jammy AS build
WORKDIR /workspace

# Resolve dependencies separately so this layer is reused when sources change.
COPY nsa2-gateway/.mvn/ .mvn/
COPY nsa2-gateway/pom.xml ./
COPY --chmod=0755 nsa2-gateway/mvnw ./
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY nsa2-gateway/src/ src/
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    ./mvnw --batch-mode --no-transfer-progress package

FROM eclipse-temurin:${JAVA_VERSION}-jre-jammy AS runtime
WORKDIR /app

# Run with an unprivileged account instead of root.
RUN groupadd --gid 10001 app \
    && useradd --uid 10001 --gid app --no-create-home \
        --home-dir /app --shell /usr/sbin/nologin app

COPY --from=build --chown=app:app \
    /workspace/target/nsa2_gateway-*.jar /app/app.jar

USER 10001:10001
EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
