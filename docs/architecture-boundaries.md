# 아키텍처 경계 원칙

## 개요

이 저장소는 `presentation -> application -> domain <- infrastructure` 방향을 유지합니다. 원본 샘플의 학습 포인트는 그대로 살리되, XML mapper 중심 구현 대신 `jOOQ DSL + adapter + reducer/factory` 조합으로 영속성을 구현합니다.

## 핵심 원칙

### 1. `application`은 흐름과 트랜잭션만 조합합니다

- `UseCase`는 입력 command를 받아 도메인 흐름을 시작합니다.
- 여러 도메인 서비스와 저장소 포트를 조합합니다.
- 트랜잭션 경계를 선언하고 결과를 result DTO로 변환합니다.
- 상태 전이 규칙, 상세 검증, SQL 세부 구현은 직접 품지 않습니다.

### 2. 비즈니스 규칙은 `domain`에 둡니다

- 상태 전이 검증은 `policy`
- 저장소 조회가 필요한 규칙은 `domain service`
- 엔티티 자신의 불변조건은 엔티티 메서드

### 3. 저장소 포트는 `domain`, 구현은 `infrastructure`

- `OrderRepository`, `PaymentRepository`, `OutboxEventRepository` 같은 포트는 도메인에 둡니다.
- `OrderJooqRepositoryAdapter`, `PaymentJooqRepositoryAdapter` 같은 구현은 인프라에 둡니다.
- generated jOOQ 타입은 adapter 내부에 가두고, 도메인에는 순수 도메인 객체만 전달합니다.

### 4. aggregate 조회는 인프라에서 명시적으로 조립합니다

- join 기반 aggregate 조회는 `reducer`가 row를 fold 합니다.
- 상태 기반 subtype 분기는 `factory`가 담당합니다.
- follow-up query 기반 조회는 repository 메서드가 후속 쿼리를 직접 호출합니다.

### 5. `presentation`은 HTTP 입출력만 담당합니다

- request를 command로 변환해 `UseCase`를 호출합니다.
- result를 response DTO로 변환합니다.
- 비즈니스 규칙을 직접 구현하지 않습니다.

### 6. 트랜잭션은 `application`에서 엽니다

- 일반 읽기/쓰기 경계는 `UseCase`에서 선언합니다.
- `OrderBatchUseCase`처럼 기술적으로 중요한 흐름도 애플리케이션 계층에서 열고 닫습니다.
- jOOQ batch는 Spring 트랜잭션 안에서 실행해 전체 요청을 원자적으로 처리합니다.

## 계층별 책임 표

| 계층 | 책임 | 두면 좋은 것 | 두지 말아야 할 것 |
| --- | --- | --- | --- |
| `presentation` | HTTP 입출력 | controller, request/response DTO, advice | 비즈니스 정책, 저장소 조합 |
| `application` | 유스케이스 흐름, 트랜잭션 | `UseCase`, command/result | SQL 상세 구현, row 매핑 |
| `domain` | 비즈니스 의미와 규칙 | entity, value object, policy, service, repository port | HTTP, jOOQ API, generated record |
| `infrastructure` | 외부 기술 구현 | jOOQ adapter, reducer, listener, scheduler | 유스케이스 흐름 결정 |

## 의존 방향

허용 방향:

- `presentation -> application`
- `application -> domain`
- `infrastructure -> domain`
- `infrastructure -> framework`

피해야 하는 방향:

- `domain -> application`
- `domain -> presentation`
- `domain -> infrastructure`
- `application -> presentation`

## 테스트 원칙

- `web-layer`
  - `@WebMvcTest`와 mock 기반 API 검증
- `jooq-repository`
  - `@JooqTest + MySQL Testcontainers` 기반 저장소 검증
- `integration-test`
  - 실제 HTTP + Spring + MySQL Testcontainers 검증

## 관련 문서

- [프로젝트 구조 가이드](./project-structure.md)
- [jOOQ 패턴 선택 가이드](./jooq-pattern-selection-guide.md)
- [새 기능 추가 체크리스트](./feature-addition-checklist.md)
