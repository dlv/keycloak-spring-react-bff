# Project: nsa2-gateway

## Project Overview
`nsa2-gateway` is a Spring Boot application acting as an API Gateway in a microservices architecture. It utilizes **Spring Cloud Gateway** with **WebFlux** for non-blocking, reactive request routing.

- **Technology Stack:** Java 21, Spring Boot, Spring Cloud Gateway (WebFlux).
- **Core Functionality:** Routing requests to downstream services (e.g., resource-server).

## Building and Running
This project uses the Maven Wrapper (`mvnw`).

- **Build the project:**
  ```bash
  ./mvnw clean install
  ```
- **Run the application:**
  ```bash
  ./mvnw spring-boot:run
  ```
- **Run tests:**
  ```bash
  ./mvnw test
  ```

## Development Conventions
- **Codebase Structure:** Follows standard Maven layout (`src/main/java`, `src/test/java`).
- **Configuration:** Managed via `src/main/resources/application.yaml`.
- **Java Version:** 21.
