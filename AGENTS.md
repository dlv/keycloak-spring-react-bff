# Repository instructions

## Layout and architecture

- There is no root Maven reactor or root wrapper. `nsa2-gateway/` and `nsa2-resource-server/` are independent Spring Boot applications; run Maven from the application directory.
- `nsa2-gateway` uses Spring Boot 4.1.0, Spring Cloud 2025.1.2, and **Spring Cloud Gateway Server WebMVC** (`spring-boot-starter-webmvc`), not WebFlux. It listens on port 8080 and enables virtual threads.
- `nsa2-resource-server` uses Spring Boot 4.1.1 and Java 21, listens on port 8082, and deliberately exposes the synchronous `GET /resource-server/blocking/{sleepInSecond}` endpoint in `BlockingController`.
- The gateway routes `/resource-server/**` to `${NSA2_RESOURCE_SERVER_URI:http://localhost:8082}` without stripping the prefix, so downstream paths must remain `/resource-server/...`.
- The gateway includes a Netflix Eureka client, but this repository has no Eureka server and the route is a direct URI rather than `lb://`. Standalone runs can show `localhost:8761` connection-refused warnings; the Compose stack disables the unused client.
- The README describes a Keycloak/BFF study, but this checkout currently has no Keycloak configuration/dependency and no React application.
- `nsa2-gateway/GEMINI.md` is stale where it describes WebFlux and summarizes the Java setup; the module `pom.xml` and `application.yaml` are authoritative.
- Preserve the existing package/artifact spellings: the gateway uses `br.com.pradolabs.nsa2_gateway`/`nsa2_gateway`, while the resource server uses `br.com.pradolabs.resourceserver`.

## Build and run

- Use each module's Maven wrapper. From PowerShell use `.\mvnw.cmd`; on Unix use `./mvnw`.
- The gateway POM targets Java 17, but run it on JDK 21 or newer because virtual threads and its Dockerfile require Java 21. The resource-server POM targets Java 21.
- From either module directory, use `clean verify` for the normal build-and-test check, `test` for tests only, and `spring-boot:run` to launch the app. On Windows: `.\mvnw.cmd clean verify`, `.\mvnw.cmd test`, or `.\mvnw.cmd spring-boot:run`.
- Focused test examples: `.\mvnw.cmd "-Dtest=Nsa2GatewayApplicationTests#contextLoads" test` in `nsa2-gateway`, or the equivalent `Nsa2ResourceServerApplicationTests#contextLoads` in `nsa2-resource-server`.
- There are no root-level lint, typecheck, formatter, codegen, or CI commands; Maven `verify` is the available repository check.

## Runtime and test notes

- For a manual proxy check, start `nsa2-resource-server` first, then `nsa2-gateway`, and request `http://localhost:8080/resource-server/blocking/1`.
- Both current tests are only `@SpringBootTest` `contextLoads` smoke tests; they do not start the other application or assert proxy behavior. No Keycloak, database, or container is required for them.
- The gateway configuration intentionally enables TRACE/DEBUG logging, so gateway test/build output is substantially noisier than the resource-server output.
- Root `docker-compose.yml` builds both applications with a root build context; only gateway port `8080:8080` is published. The resource server is reachable internally as `nsa2-resource-server:8082`; use `docker compose up --build` for the full stack (or `wsl.exe -e docker compose up --build` from Windows when Docker is WSL-only).
- Canonical Dockerfiles are `nsa2-gateway/Dockerfile` and `nsa2-resource-server/Dockerfile`; build them from the repository root with `docker build -f <module>/Dockerfile -t <image> .`. An older duplicate gateway Dockerfile remains under `nsa2-gateway/src/test/java/br/com/pradolabs/nsa2_gateway/Dockerfile` and is not the canonical entrypoint.
