# jOOQ Codegen / Flyway 가이드

## 핵심 원칙

- 스키마 원본은 `src/main/resources/db/migration`만 사용합니다.
- 테스트도 `src/test/resources/db/mysql/*-schema.sql` 같은 별도 DDL을 유지하지 않고, Spring Boot/Flyway가 main migration을 적용한 스키마를 재사용합니다.
- generated source는 `build/generated-src/jooq/main`에 생성하고 Git에 커밋하지 않습니다.
- 로컬 개발, codegen, 런타임, 테스트를 모두 MySQL dialect 기준으로 맞춥니다.

## 흐름

1. `mysql-8/docker-compose.yml`로 로컬 MySQL을 띄웁니다.
2. `./gradlew jooqCodegen`을 실행합니다.
3. Gradle은 `flywayClean -> flywayMigrate -> jooqCodegen` 순서로 동작합니다.
4. 생성된 타입은 컴파일 시 main source set에 포함됩니다.

## 왜 MySQL만 사용하는가

- codegen과 런타임 SQL의 dialect를 하나로 고정할 수 있습니다.
- `Flyway -> MySQL schema -> jOOQ codegen -> Testcontainers MySQL` 흐름이 단순하고 재현성이 높습니다.
- H2 호환 스키마를 따로 유지하지 않아도 되어 샘플의 설명력이 선명합니다.

## 운영 규칙

- migration을 바꾸면 `jooqCodegen`을 다시 실행합니다.
- generated type을 직접 수정하지 않습니다.
- codegen 실패는 로컬 MySQL 기동 상태와 migration 정합성을 먼저 확인합니다.

## 관련 문서

- [개발 및 실행 가이드](./development-guide.md)
- [jOOQ 샘플 개요](./jooq-sample-overview.md)
