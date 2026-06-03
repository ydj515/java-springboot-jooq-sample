# 개발 및 실행 가이드

## 개요

이 문서는 `java-springboot-jooq-sample`을 로컬에서 실행하고 검증하는 기본 절차를 정리합니다. 이 저장소는 `Flyway migration -> MySQL schema -> jOOQ codegen -> Spring Boot runtime/test` 흐름을 기준으로 동작합니다.

## 기술 스택

- Java 21
- Spring Boot 3.5.14
- Gradle 8.14.4
- jOOQ 3.19.32
- Flyway 12.0.1
- MySQL 8
- Testcontainers MySQL

## 핵심 원칙

- 스키마 원본은 `src/main/resources/db/migration` 하나만 사용합니다.
- generated source는 `build/generated-src/jooq/main`에 생성되며 Git에 커밋하지 않습니다.
- 로컬 실행, codegen, 테스트는 모두 MySQL dialect 기준으로 정렬합니다.
- `mise.toml`과 Gradle task 이름은 `jOOQ` 기준으로 통일합니다.

## 자주 쓰는 명령

### mise 사용

- `mise run`: Spring Boot 애플리케이션 실행
- `mise run build`: 전체 빌드 실행
- `mise run jooq-codegen`: 생성 코드 갱신
- `mise run test`: 빠른 테스트 실행
- `mise run web-layer-test`: 웹 레이어 테스트 실행
- `mise run jooq-repository-test`: jOOQ 저장소 테스트 실행
- `mise run integration-test`: MySQL Testcontainers 통합 테스트 실행
- `mise run verify`: codegen부터 전체 검증까지 실행
- `mise run clean`: 빌드 산출물 정리

### Gradle Wrapper 사용

- `./gradlew bootRun`
- `./gradlew jooqCodegen`
- `./gradlew test`
- `./gradlew webLayerTest`
- `./gradlew jooqRepositoryTest`
- `./gradlew integrationTest`
- `./gradlew clean jooqCodegen test webLayerTest jooqRepositoryTest integrationTest`

## 로컬 MySQL 실행

```bash
docker compose --env-file mysql-8/.env -f mysql-8/docker-compose.yml up -d
```

- 기본 로컬 연결값은 `mydatabase / myuser / mypassword` 입니다.
- 애플리케이션 런타임은 `mydatabase / myuser / mypassword`를 사용합니다.
- `jooqCodegen`은 기본적으로 별도 schema `jooq_codegen`을 만들고 `root/root` 계정으로 정리 후 재생성합니다.
- 필요하면 `JOOQ_CODEGEN_DB_HOST`, `JOOQ_CODEGEN_DB_PORT`, `JOOQ_CODEGEN_DB_DATABASE`, `JOOQ_CODEGEN_DB_USER`, `JOOQ_CODEGEN_DB_PASSWORD`로 codegen 연결을 덮어쓸 수 있습니다.

## codegen 흐름

1. `mysql-8` Docker Compose로 로컬 MySQL을 띄웁니다.
2. `./gradlew jooqCodegen`을 실행합니다.
3. Gradle은 `prepareCodegenDatabase -> jooqCodegen` 순서로 동작합니다.
4. 생성된 jOOQ 타입은 `build/generated-src/jooq/main`에 생기고, 컴파일 시 main source set에 포함됩니다.

## 테스트 전략

- `test`
  - `integration-test`, `jooq-repository` 태그를 제외한 빠른 테스트 묶음입니다.
- `webLayerTest`
  - `@WebMvcTest` 기반 API 슬라이스 검증입니다.
- `jooqRepositoryTest`
  - `@JooqTest + MySQL Testcontainers` 기반 저장소 어댑터 검증입니다.
- `integrationTest`
  - 실제 HTTP + Spring Boot + MySQL Testcontainers 흐름 검증입니다.
  - schema는 Flyway로 준비하고, 각 테스트 메서드 전에는 공통 reset + seed SQL을 적용합니다.
  - 테스트 전용 `db/mysql/*-schema.sql` DDL은 유지하지 않습니다. 스키마 변경은 `src/main/resources/db/migration`에 추가하고 테스트도 같은 migration을 사용합니다.

## 관련 문서

- [프로젝트 구조 가이드](./project-structure.md)
- [jOOQ 샘플 개요](./jooq-sample-overview.md)
- [jOOQ Codegen / Flyway 가이드](./jooq-codegen-and-flyway.md)
