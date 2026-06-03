# JPA 패턴 선택 가이드

## 개요

이 문서는 `order` 도메인처럼 상태 전이와 aggregate 조회가 많은 기능을 설계할 때 JPA와 jOOQ를 어떤 관점으로 비교하면 좋은지 정리합니다. 이 저장소의 실제 구현은 jOOQ 기준이며, 비교 목적은 선택 기준을 분명히 하기 위함입니다.

## 비교 포인트

| 주제 | JPA | jOOQ |
| --- | --- | --- |
| 상태 변경 | 엔티티 메서드 + dirty checking | 명시적 update SQL |
| 낙관적 락 | `@Version` | `WHERE version = ?` 조건 update |
| 조회 모델 | 엔티티 그래프 / projection | DSL + DTO / domain 조립 |
| aggregate 조회 | fetch join / `EntityGraph` | join + reducer |
| N+1 비교 | 지연 로딩 주의 | follow-up query 주의 |
| 벌크 작업 | JPQL bulk update | 명시적 bulk update SQL |
| SQL 제어력 | ORM 추상화 우선 | SQL 가독성과 제어력 우선 |

## 이 저장소에서 jOOQ를 선택한 이유

- 주문 조회처럼 join 결과를 명시적으로 접어 aggregate를 조립하는 예제를 보여주기 쉽습니다.
- 상태 전이 update와 outbox polling을 SQL 수준에서 직접 드러낼 수 있습니다.
- batch, optimistic locking, `FOR UPDATE SKIP LOCKED`를 학습용 코드로 노출하기 좋습니다.

## JPA가 더 유리한 경우

- 엔티티 그래프와 변경 감지 중심 모델링이 더 자연스러울 때
- 조회보다 aggregate 수정 중심의 CRUD가 많을 때
- 팀이 ORM 기반 생산성에 더 익숙할 때

## jOOQ가 더 유리한 경우

- SQL이 복잡하고 결과 조립 전략을 명시적으로 통제해야 할 때
- 읽기 모델과 쓰기 모델이 분명히 다를 때
- 도메인 규칙은 유지하되 영속성 구현을 세밀하게 보여주고 싶을 때

## 관련 문서

- [jOOQ 패턴 선택 가이드](./jooq-pattern-selection-guide.md)
- [jOOQ 샘플 개요](./jooq-sample-overview.md)
