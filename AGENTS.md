# Repository Guidelines

## Project Structure & Module Organization
Core code lives in `src/main/java/org/example/javaspringbootjooqsample` and follows layered packaging. Use `presentation` for controllers and request/response DTOs, `application` for `*UseCase`, `*Command`, and `*Result`, `domain` for models, policies, services, and repository ports, and `infrastructure` for jOOQ adapters, reducers, listeners, schedulers, and support classes. Flyway migrations live in `src/main/resources/db/migration`. Tests are under `src/test/java`, with shared reset/data SQL in `src/test/resources/db/common`. Supporting docs are in `docs/`, HTTP examples in `http/`, and local MySQL files in `mysql-8/`.

## Documentation References
Start with [`docs/README.md`](./docs/README.md) for the reading order. Use [`docs/development-guide.md`](./docs/development-guide.md) for local setup and test flow, [`docs/project-structure.md`](./docs/project-structure.md) for package and DTO conventions, and [`docs/architecture-boundaries.md`](./docs/architecture-boundaries.md) for dependency direction. Check [`docs/feature-addition-checklist.md`](./docs/feature-addition-checklist.md) before merging new features. For persistence and operations, refer to [`docs/jooq-sample-overview.md`](./docs/jooq-sample-overview.md), [`docs/jooq-pattern-selection-guide.md`](./docs/jooq-pattern-selection-guide.md), [`docs/jooq-codegen-and-flyway.md`](./docs/jooq-codegen-and-flyway.md), [`docs/logging-and-tracing.md`](./docs/logging-and-tracing.md), and [`docs/error-handling.md`](./docs/error-handling.md).
For JPA versus jOOQ trade-offs around status transitions, projections, N+1, and bulk updates, also review [`docs/jpa-pattern-selection-guide.md`](./docs/jpa-pattern-selection-guide.md).

## Build, Test, and Development Commands
Use `mise` for the default workflow:
- `mise run`: start the Spring Boot app
- `mise run jooq-codegen`: generate jOOQ sources
- `mise run build`: compile and package
- `mise run test`: run fast tests excluding `integration-test`
- `mise run web-layer-test`: run `web-layer` slice tests
- `mise run jooq-repository-test`: run jOOQ repository tests
- `mise run integration-test`: run Testcontainers MySQL integration tests
- `mise run verify`: run codegen and the full verification flow

Gradle Wrapper equivalents are available, such as `./gradlew bootRun` and `./gradlew integrationTest`. Start local MySQL with `docker compose --env-file mysql-8/.env -f mysql-8/docker-compose.yml up -d`.

## Coding Style & Naming Conventions
Follow `.editorconfig`: 4 spaces for `*.java`, `*.gradle`, and `*.kts`; 2 spaces for `*.yml`, `*.json`, and `*.toml`. Keep layer-specific names consistent: `*Controller`, `*Request`, `*Response`, `*UseCase`, `*Command`, `*Result`, `*JooqRepositoryAdapter`, and `*Reducer`. Prefer feature packages such as `account` and `order`. Do not expose domain entities directly from controllers.

## Testing Guidelines
This project uses JUnit 5, Spring Boot Test, jOOQ test support, Rest Assured, Flyway, and Testcontainers. Name test classes with a `Tests` suffix, for example `AccountControllerTests` or `OrderMySqlIntegrationTests`. Add or update tests for every behavior change, and use the narrowest test slice that proves the change before relying on full integration coverage.

## Commit & Pull Request Guidelines
Recent history follows Conventional Commits with lowercase types such as `feat:`, `fix:`, `docs:`, `build:`, `refactor:`, and `chore:`. Keep commits focused on one logical change. Pull requests should include a short summary, affected layers or packages, test evidence (`mise run test`, `./gradlew integrationTest`, and so on), and linked issues when applicable. Include request/response examples when API behavior changes.
