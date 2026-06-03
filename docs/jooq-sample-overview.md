# jOOQ 샘플 개요

## 목적

이 저장소는 원본 대응 저장소의 짝(pair) 저장소입니다. 같은 API와 같은 도메인 시나리오를 유지하면서, 영속성 구현만 `jOOQ + Flyway + MySQL`로 다시 표현합니다.

## 이 저장소에서 보여주는 학습 포인트

- join 기반 aggregate 조회와 row reducer 조립
- 단순 1:N 조회에서 `MULTISET`과 join reducer 비교. 실제 MySQL SQL과 trade-off는 [jOOQ 패턴 선택 가이드](./jooq-pattern-selection-guide.md)를 참고합니다.
- follow-up query 기반 조회와 query count 비교
- 상태 기반 subtype mapping
- optimistic locking update
- jOOQ batch API 기반 다건 insert
- outbox publisher / compensation worker / processed event 멱등 처리
- generated type 비노출 원칙

## 주요 대응 관계

| 원본 샘플 포인트 | jOOQ 저장소에서의 표현 |
| --- | --- |
| `resultMap` aggregate 매핑 | `Record -> Domain` 명시적 매핑 + reducer |
| `nested select` | repository 내부 follow-up query |
| `discriminator` | subtype factory |
| `TypeHandler` | codegen forced type + adapter 매핑 |
| `ExecutorType.BATCH` | `DSLContext.batch(...)` |
| 기존 query count interceptor | jOOQ `ExecuteListener` 기반 query count |

## 추천 탐색 순서

1. `OrderJooqRepositoryAdapter`
2. `OrderAggregateRowReducer`
3. `PaymentJooqRepositoryAdapter`
4. `OutboxEventJooqRepositoryAdapter`
5. `CompensationTaskJooqRepositoryAdapter`

## 관련 문서

- [jOOQ 패턴 선택 가이드](./jooq-pattern-selection-guide.md)
- [jOOQ Codegen / Flyway 가이드](./jooq-codegen-and-flyway.md)
