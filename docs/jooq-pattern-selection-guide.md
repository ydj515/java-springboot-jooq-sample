# jOOQ 패턴 선택 가이드

## 목적

이 문서는 이 저장소에서 어떤 이유로 특정 jOOQ 패턴을 선택했는지 설명합니다. 핵심 기준은 `타입 안정성`, `명시적 SQL 제어`, `학습 포인트의 비교 가능성`, `도메인 경계 유지`입니다.

## 기본 선택 기준

### 1. aggregate 조회가 필요한가

- 예: 주문 + 고객 + 주문 항목
- 선택: join + reducer
- 이유: 한 번의 SQL로 읽고 query count 비교 실험을 유지할 수 있습니다.

### 2. N+1 비교 시나리오를 의도적으로 남길 것인가

- 예: `findAllWithNestedSelect`, `findByIdWithNestedSelect`
- 선택: follow-up query 방식 유지
- 이유: 원본 샘플의 학습 포인트를 jOOQ에서도 그대로 관찰할 수 있습니다.

### 3. 상태별 subtype이 필요한가

- 예: `PaidOrder`, `ShippedOrder`, `CancelledOrder`
- 선택: factory 분기
- 이유: discriminator와 같은 목적을 기술 독립적으로 표현할 수 있습니다.

### 4. 단건 반복 insert와 JDBC batch를 비교해야 하는가

- 예: 주문 항목 추가
- 선택: 일반 insert + `DSLContext.batch(...)` 두 흐름 모두 유지
- 이유: API는 같아도 실행 전략은 다르게 보여줄 수 있습니다.

### 5. generated type을 어디까지 노출할 것인가

- 선택: 인프라 내부에만 한정
- 이유: 도메인/애플리케이션 계층이 jOOQ generated code에 잠기지 않도록 합니다.

## 실전 규칙

- 조합이 복잡한 조회는 adapter + reducer + factory로 나눕니다.
- 간단한 CRUD 저장은 adapter 내부 private mapper 메서드만으로 끝냅니다.
- `SELECT FOR UPDATE SKIP LOCKED`가 필요한 polling 흐름은 repository port에 그대로 드러냅니다.
- batch, optimistic locking, outbox polling처럼 의미 있는 SQL 특성은 숨기지 않고 코드에 드러냅니다.

## 피하는 선택

- generated record를 도메인 모델처럼 직접 쓰기
- 모든 쿼리를 하나의 giant adapter 메서드에 몰아넣기
- follow-up query가 필요한 시나리오까지 억지로 한 SQL로 합치기
- 문서상 비교 포인트를 없애는 과도한 추상화

## 관련 문서

- [jOOQ 샘플 개요](./jooq-sample-overview.md)
- [아키텍처 경계 원칙](./architecture-boundaries.md)
