# 새 기능 추가 체크리스트

## 빠른 판단 순서

### 1. `UseCase`에 둘 것인가

- 유스케이스 순서를 조합하는가
- 트랜잭션 경계를 여는가
- command/result 변환을 담당하는가

### 2. `Policy`에 둘 것인가

- 검증 규칙 또는 허용/비허용 판단인가
- 저장소 조회 없이 결정 가능한가

### 3. `Domain Service`에 둘 것인가

- 저장소 포트 조회가 필요한가
- 여러 도메인 객체를 함께 다루는가
- 그래도 여전히 비즈니스 규칙인가

### 4. `Repository Port`가 필요한가

- 도메인 규칙 수행에 조회/저장이 필요한가
- jOOQ 구현 상세를 도메인에서 숨겨야 하는가

### 5. 인프라 구현은 어떤 형태가 적합한가

- 단순 CRUD면 adapter 내부 메서드로 충분한가
- aggregate 조회면 reducer/factory 분리가 필요한가
- polling이면 `FOR UPDATE SKIP LOCKED`가 필요한가
- batch면 일반 insert와 jOOQ batch를 둘 다 보여줘야 하는가

## 금지 신호

- `UseCase`에 상태 전이 규칙이 계속 늘어난다
- 도메인 계층이 jOOQ generated type을 import 한다
- adapter가 request/response DTO를 직접 안다
- aggregate row fold 로직이 controller나 use case로 새어 나온다

## 관련 문서

- [아키텍처 경계 원칙](./architecture-boundaries.md)
- [jOOQ 패턴 선택 가이드](./jooq-pattern-selection-guide.md)
