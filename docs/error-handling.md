# 에러 처리 가이드

## 개요

이 프로젝트는 서비스 계층에서 도메인 예외를 던지고, API 계층에서는 `RestControllerAdvice`로 공통 응답 포맷으로 변환합니다.

## 이 문서를 보면 좋은 경우

- 서비스 계층과 API 계층의 예외 처리 책임을 구분해서 보고 싶을 때
- 도메인별 커스텀 예외를 어디에 두는지 확인하고 싶을 때
- `traceId`와 에러 응답이 어떻게 연결되는지 알고 싶을 때

## 핵심 내용

### 구성 요소

- `BusinessException`
  - 공통 비즈니스 예외 기반 클래스입니다.
- `GlobalExceptionHandler`
  - `BusinessException`, 잘못된 파라미터, 잘못된 요청 본문, 예상하지 못한 예외를 공통 처리합니다.
- `ApiErrorResponse`
  - API 공통 에러 응답 포맷입니다.
  - `timestamp`, `status`, `error`, `code`, `message`, `path`, `traceId`를 포함합니다.

### 예외 계층 규칙

- 도메인별 예외는 각 도메인 하위 패키지에 둡니다.
  - `domain/user/exception`
  - `domain/order/exception`
- 서비스 계층은 mapper의 `null` 반환을 그대로 외부에 노출하지 않습니다.
- 조회 실패, 입력 누락, 도메인 규칙 위반을 구분해서 예외를 나눕니다.

### 예시

#### User 예외

- `UserNotFoundException`
- `UserAlreadyExistsException`
- `UserUsernameRequiredException`
- `InvalidUserException`

#### Order 예외

- `OrderNotFoundException`
- `OrderItemsRequiredException`
- `InvalidOrderItemException`

### 추적과의 연결

- 에러 응답의 `traceId`는 로그 MDC의 `traceId`와 동일합니다.
- 클라이언트는 에러 응답만으로도 서버 로그를 검색할 수 있습니다.

### 관련 테스트

- `GlobalExceptionHandlerWebMvcTests`
  - 비즈니스 예외와 잘못된 요청이 공통 응답 포맷으로 변환되는지 검증합니다.

## 관련 문서

- [로깅 및 추적 가이드](./logging-and-tracing.md)
- [프로젝트 구조 가이드](./project-structure.md)
