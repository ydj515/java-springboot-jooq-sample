# 로깅 및 추적 가이드

## 개요

이 프로젝트는 요청 단위 로그 추적을 위해 `traceId`, `requestId`를 MDC에 넣고, 응답 헤더와 에러 응답 본문에도 같은 값을 반영합니다.

## 핵심 구성

- `logback-spring.xml`
  - `app`, `traceId`, `requestId`, `method`, `uri`, `clientIp`를 함께 출력합니다.
- `config/logging/MdcLoggingFilter`
  - 요청 시작/종료 시 MDC와 응답 헤더를 관리합니다.
- `common/logging/TraceContext`
  - 현재 스레드의 추적 식별자를 읽는 공통 진입점입니다.
- `config/async/MdcTaskDecorator`
  - `@Async` 작업에도 MDC를 전파합니다.
- `infrastructure/jooq/listener/JooqQueryCountListener`
  - 저장소 테스트에서 SQL 실행 횟수를 셉니다.

## 추적 흐름

1. 클라이언트가 `X-Trace-Id`, `X-Request-Id`를 보낼 수 있습니다.
2. 값이 없으면 서버가 생성합니다.
3. 필터가 MDC를 채우고 응답 헤더에 다시 내려줍니다.
4. 에러 응답에도 같은 `traceId`가 실립니다.
5. 저장소 테스트에서는 `JooqQueryCounter`로 쿼리 수를 비교합니다.

## 관련 문서

- [에러 처리 가이드](./error-handling.md)
- [개발 및 실행 가이드](./development-guide.md)
