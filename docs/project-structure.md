# 프로젝트 구조 가이드

## 개요

이 저장소는 계층별 상위 패키지와 기능별 하위 패키지를 함께 사용합니다. 원본 샘플의 레이어 구조는 유지하되, 영속성 구현은 `jOOQ repository adapter`와 `reducer / factory / listener` 조합으로 재구성했습니다.

## Java 소스 구조

- `src/main/java/.../presentation`
  - 컨트롤러와 HTTP request/response DTO를 둡니다.
- `src/main/java/.../application`
  - `*UseCase`, `command`, `result`를 둡니다.
- `src/main/java/.../domain`
  - 도메인 모델, policy, service, repository port를 둡니다.
- `src/main/java/.../infrastructure`
  - 외부 기술 연동 계층입니다.
  - 기능별 adapter 예시:
    - `infrastructure/user/UserJooqRepositoryAdapter`
    - `infrastructure/order/OrderJooqRepositoryAdapter`
    - `infrastructure/payment/PaymentJooqRepositoryAdapter`
  - 공통 jOOQ 지원 예시:
    - `infrastructure/jooq/reducer`
    - `infrastructure/jooq/listener`
    - `infrastructure/jooq/support`

## 리소스 구조

- `src/main/resources/db/migration`
  - 유일한 스키마 원본입니다.
- `src/main/resources/application*.yml`
  - local / test / integration 환경 설정을 둡니다.
- `src/main/resources/logback-spring.xml`
  - 로깅 패턴과 MDC 출력을 관리합니다.

## generated source

- `build/generated-src/jooq/main`
  - `jooqCodegen` 결과물입니다.
  - generated type은 인프라 내부에서만 사용하고, `application`/`domain`/`presentation`으로 노출하지 않습니다.

## 테스트 구조

- `src/test/java`
  - 웹 레이어, 도메인, 저장소, 통합 테스트가 위치합니다.
- `src/test/resources/db/common`
  - 공통 reset SQL과 샘플 데이터셋을 둡니다.

## 계층별 DTO 규칙

- 컨트롤러 계층: `*Request`, `*Response`
- 애플리케이션 계층: `*Command`, `*Result`
- 유스케이스 클래스: `*UseCase`
- 도메인 모델은 컨트롤러 응답으로 직접 노출하지 않습니다.

## 관련 문서

- [개발 및 실행 가이드](./development-guide.md)
- [아키텍처 경계 원칙](./architecture-boundaries.md)
- [jOOQ 샘플 개요](./jooq-sample-overview.md)
