ARG JAVA_VERSION=21

FROM eclipse-temurin:${JAVA_VERSION}-jdk-jammy AS build
WORKDIR /workspace

# Resolve dependencies in a cacheable layer before copying application sources.
COPY nsa2-gateway/.mvn/ .mvn/
COPY nsa2-gateway/pom.xml ./
COPY --chmod=0755 nsa2-gateway/mvnw ./mvnw

RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    ./mvnw --batch-mode --no-transfer-progress -DskipTests dependency:go-offline

# Test sources are not needed to produce the runtime artifact.
COPY nsa2-gateway/src/main/ src/main/

RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    ./mvnw --batch-mode --no-transfer-progress -DskipTests package \
    && cp target/nsa2_gateway-*.jar /workspace/application.jar

# Split the Spring Boot jar so stable dependencies remain cacheable across image builds.
RUN java -Djarmode=tools -jar /workspace/application.jar \
    extract --layers --destination /workspace/layers

FROM eclipse-temurin:${JAVA_VERSION}-jre-jammy AS runtime

ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8"

WORKDIR /app

# Use a dedicated unprivileged account and do not run the gateway as root.
RUN groupadd --gid 10001 app \
    && useradd --uid 10001 --gid app --no-create-home \
        --home-dir /app --shell /usr/sbin/nologin app

# Copy each Spring Boot layer independently to preserve Docker layer caching.
COPY --from=build --chown=app:app /workspace/layers/dependencies/ ./
COPY --from=build --chown=app:app /workspace/layers/spring-boot-loader/ ./
COPY --from=build --chown=app:app /workspace/layers/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /workspace/layers/application/ ./

USER app:app
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/application.jar"]
