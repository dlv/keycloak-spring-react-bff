# syntax=docker/dockerfile:1.7

# Build from the repository root:
# docker build -f nsa2-resource-server/Dockerfile -t nsa2-resource-server:latest .

ARG JAVA_VERSION=21

FROM eclipse-temurin:${JAVA_VERSION}-jdk-jammy AS build
WORKDIR /workspace

# Resolve dependencies in a cacheable layer before copying application sources.
COPY nsa2-resource-server/.mvn/ .mvn/
COPY nsa2-resource-server/pom.xml ./
COPY --chmod=0755 nsa2-resource-server/mvnw ./mvnw

RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    ./mvnw --batch-mode --no-transfer-progress -DskipTests dependency:go-offline

# Test sources are not needed to produce the runtime artifact.
COPY nsa2-resource-server/src/main/ src/main/

RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    ./mvnw --batch-mode --no-transfer-progress -DskipTests package \
    && cp target/nsa2-resource-server-*.jar /workspace/application.jar

# Split the Spring Boot jar so stable dependencies remain cacheable across image builds.
RUN java -Djarmode=tools -jar /workspace/application.jar \
    extract --layers --destination /workspace/layers

FROM eclipse-temurin:${JAVA_VERSION}-jre-jammy AS runtime

ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8"

WORKDIR /app

# Use a dedicated unprivileged account and do not run the resource server as root.
RUN groupadd --gid 10001 app \
    && useradd --uid 10001 --gid app --no-create-home \
        --home-dir /app --shell /usr/sbin/nologin app

# Copy each Spring Boot layer independently to preserve Docker layer caching.
COPY --from=build --chown=app:app /workspace/layers/dependencies/ ./
COPY --from=build --chown=app:app /workspace/layers/spring-boot-loader/ ./
COPY --from=build --chown=app:app /workspace/layers/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /workspace/layers/application/ ./

USER app:app
# Internal service port only; docker-compose.yml does not publish it on the host.
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/application.jar"]
