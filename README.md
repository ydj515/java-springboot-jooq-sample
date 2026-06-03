# java-springboot-jooq-sample

Spring Boot와 jOOQ를 사용하는 샘플 애플리케이션입니다. Java 21, Spring Boot 3.x, Flyway 기반 스키마 관리, MySQL 중심 테스트 흐름, 예제 중심 프로젝트 구조를 포함합니다.

이 저장소는 원본 대응 저장소와 같은 도메인과 API를 유지하면서, 영속성 구현만 `jOOQ + Flyway + MySQL`로 다시 표현합니다.

## 빠른 시작

```bash
docker compose --env-file mysql-8/.env -f mysql-8/docker-compose.yml up -d
./gradlew jooqCodegen
mise run
```

## 자주 사용하는 명령어

- `mise run`: 애플리케이션 실행
- `mise run build`: 프로젝트 빌드
- `mise run jooq-codegen`: Flyway 스키마 기준 jOOQ 생성 코드 갱신
- `mise run test`: 빠른 테스트 실행
- `mise run web-layer-test`: 웹 레이어 슬라이스 테스트 실행
- `mise run jooq-repository-test`: `jooqRepositoryTest` 실행
- `mise run integration-test`: MySQL Testcontainers 통합 테스트 실행
- `mise run verify`: codegen부터 전체 검증까지 순차 실행
- `./gradlew bootRun`: Gradle Wrapper로 애플리케이션 실행
- `./gradlew jooqCodegen`: Flyway 스키마 기준 jOOQ 생성 코드 갱신
- `./gradlew webLayerTest`: `web-layer` 태그 테스트만 실행
- `./gradlew jooqRepositoryTest`: `jooq-repository` 태그 테스트만 실행
- `./gradlew integrationTest`: `integration-test` 태그 테스트만 실행
- `./gradlew clean jooqCodegen test webLayerTest jooqRepositoryTest integrationTest`: 전체 검증 실행

## 문서 안내

저장소를 처음 보는 경우에는 문서 인덱스부터 읽고, 아래 주제별 문서로 이어서 확인하는 것을 권장합니다.

- [문서 인덱스](./docs/README.md): 권장 읽기 순서를 포함한 문서 진입점
- [개발 및 실행 가이드](./docs/development-guide.md): 로컬 환경 구성, 실행 명령, 테스트 흐름
- [프로젝트 구조 가이드](./docs/project-structure.md): 패키지 구성, 계층 구조, 네이밍 규칙
- [아키텍처 경계 원칙](./docs/architecture-boundaries.md): 의존성 방향과 책임 분리 기준
- [새 기능 추가 체크리스트](./docs/feature-addition-checklist.md): 기능 추가 시 확인할 안전한 작업 순서
- [jOOQ 샘플 개요](./docs/jooq-sample-overview.md): 포함된 jOOQ 예제와 학습 포인트 개요
- [jOOQ 패턴 선택 가이드](./docs/jooq-pattern-selection-guide.md): 상황별 jOOQ 접근 방식 선택 기준
- [jOOQ Codegen/Flyway 가이드](./docs/jooq-codegen-and-flyway.md): 스키마 원본, 생성 코드, MySQL-only 전략
- [JPA 패턴 선택 가이드](./docs/jpa-pattern-selection-guide.md): 상태 전이, Projection, N+1, bulk update의 JPA/jOOQ 비교
- [로깅 및 추적 가이드](./docs/logging-and-tracing.md): MDC 전파, 추적 흐름, SQL 로깅 규칙
- [에러 처리 가이드](./docs/error-handling.md): 예외 설계와 API 오류 응답 규칙
- [Swagger/OpenAPI 명세](./docs/swagger.yml): 현재 컨트롤러 기준 HTTP API 스펙
