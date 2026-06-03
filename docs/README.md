# 문서 안내

## 개요

이 디렉토리는 `java-springboot-jooq-sample` 저장소를 빠르게 이해하고 실행하기 위한 문서 모음입니다. 이 저장소는 원본 대응 저장소와 같은 도메인, 같은 API, 같은 학습 포인트를 유지하면서 영속성 구현만 `jOOQ + Flyway + MySQL`로 재구성한 짝 저장소입니다.

## 이 문서를 보면 좋은 경우

- 프로젝트를 처음 실행하거나 로컬 환경을 맞추고 싶을 때
- `presentation -> application -> domain <- infrastructure` 구조를 jOOQ 기준으로 이해하고 싶을 때
- join 기반 aggregate 조회, follow-up query, optimistic locking, batch, outbox/compensation 패턴을 어디서 확인해야 할지 알고 싶을 때

## 권장 읽기 순서

1. [개발 및 실행 가이드](./development-guide.md)
2. [프로젝트 구조 가이드](./project-structure.md)
3. [아키텍처 경계 원칙](./architecture-boundaries.md)
4. [jOOQ 샘플 개요](./jooq-sample-overview.md)
5. [jOOQ 패턴 선택 가이드](./jooq-pattern-selection-guide.md)
6. [jOOQ Codegen / Flyway 가이드](./jooq-codegen-and-flyway.md)
7. [JPA 패턴 선택 가이드](./jpa-pattern-selection-guide.md)
8. [새 기능 추가 체크리스트](./feature-addition-checklist.md)
9. [로깅 및 추적 가이드](./logging-and-tracing.md)
10. [에러 처리 가이드](./error-handling.md)
11. [Swagger/OpenAPI 명세](./swagger.yml)

## 문서 목록

- [개발 및 실행 가이드](./development-guide.md)
- [프로젝트 구조 가이드](./project-structure.md)
- [아키텍처 경계 원칙](./architecture-boundaries.md)
- [jOOQ 샘플 개요](./jooq-sample-overview.md)
- [jOOQ 패턴 선택 가이드](./jooq-pattern-selection-guide.md)
- [jOOQ Codegen / Flyway 가이드](./jooq-codegen-and-flyway.md)
- [JPA 패턴 선택 가이드](./jpa-pattern-selection-guide.md)
- [새 기능 추가 체크리스트](./feature-addition-checklist.md)
- [로깅 및 추적 가이드](./logging-and-tracing.md)
- [에러 처리 가이드](./error-handling.md)
- [Swagger/OpenAPI 명세](./swagger.yml)
