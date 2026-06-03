# jOOQ 패턴 선택 가이드

## 목적

이 문서는 이 저장소에서 어떤 이유로 특정 jOOQ 패턴을 선택했는지 설명합니다. 핵심 기준은 `타입 안정성`, `명시적 SQL 제어`, `학습 포인트의 비교 가능성`, `도메인 경계 유지`입니다.

## 기본 선택 기준

### 1. aggregate 조회가 필요한가

- 예: 주문 + 고객 + 주문 항목
- 선택: join + reducer
- 이유: 한 번의 SQL로 읽고 query count 비교 실험을 유지할 수 있습니다.
- 비교 예외: `UserJooqRepositoryAdapter.findAll()`은 단순 `User -> Role` 1:N 조회에서 `MULTISET`과 join reducer의 차이를 비교하기 위해 `MULTISET`을 사용합니다. 단건 조회인 `findById`, `findByUsername`은 기존 join reducer 방식을 유지합니다.

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

## `User.findAll()` MULTISET 비교 예외

`UserJooqRepositoryAdapter.findAll()`은 기본 원칙인 join + reducer 대신 `MULTISET`을 사용합니다. 이 선택은 단순 `User -> Role` 1:N 조회에서 jOOQ `MULTISET`이 어떤 SQL을 만들고, 기존 join reducer와 어떤 trade-off가 있는지 비교하기 위한 예외입니다.

MySQL에서 jOOQ `MULTISET`은 네이티브 문법이 아니라 JSON 집계와 `GROUP_CONCAT` 기반으로 에뮬레이션됩니다. 실제 렌더링되는 SQL은 아래와 같은 형태입니다.

```sql
select
    `users`.`id`,
    `users`.`username`,
    `users`.`name`,
    `users`.`user_type`,
    `users`.`password`,
    `users`.`email`,
    `users`.`last_login_at`,
    `users`.`created_at`,
    `users`.`updated_at`,
    `users`.`deleted_at`,
    `users`.`last_password_updated_at`,
    `users`.`trial_cnt`,
    (
        select coalesce(
            json_merge_preserve(
                '[]',
                concat(
                    '[',
                    group_concat(json_array(t.`v0`, t.`v1`, t.`v2`, t.`v3`, t.`v4`) separator ','),
                    ']'
                )
            ),
            json_array()
        )
        from (
            select
                `roles`.`id` as `v0`,
                `roles`.`name` as `v1`,
                `roles`.`description` as `v2`,
                `roles`.`created_at` as `v3`,
                `roles`.`updated_at` as `v4`
            from `users_roles`
            join `roles` on `roles`.`id` = `users_roles`.`role_id`
            where `users_roles`.`user_id` = `users`.`id`
            order by `v0` asc
            limit 9223372036854775807
        ) as t
    ) as `roles`
from `users`
order by `users`.`id` desc;
```

반대로 기존 join + reducer 방식은 아래 코드처럼 `users`, `users_roles`, `roles`를 조인한 row를 가져온 뒤 Java reducer에서 다시 `User -> roles` 구조로 묶습니다.

```java
Users users = USERS.as("u");
UsersRoles usersRoles = USERS_ROLES.as("ur");
Roles roles = ROLES.as("r");

return reducer.reduce(
        selectUsersWithRoles(users, usersRoles, roles)
                .orderBy(users.ID.desc(), roles.ID.asc())
                .fetch(),
        users,
        roles
);
```

이 방식에서 렌더링되는 SQL은 아래처럼 단순한 left join 형태입니다.

```sql
select
    `u`.`id`,
    `u`.`username`,
    `u`.`name`,
    `u`.`user_type`,
    `u`.`password`,
    `u`.`email`,
    `u`.`last_login_at`,
    `u`.`created_at`,
    `u`.`updated_at`,
    `u`.`deleted_at`,
    `u`.`last_password_updated_at`,
    `u`.`trial_cnt`,
    `r`.`id`,
    `r`.`name`,
    `r`.`description`,
    `r`.`created_at`,
    `r`.`updated_at`
from `users` as `u`
left outer join `users_roles` as `ur` on `ur`.`user_id` = `u`.`id`
left outer join `roles` as `r` on `r`.`id` = `ur`.`role_id`
order by `u`.`id` desc, `r`.`id` asc;
```

위 `MULTISET` 쿼리는 단일 SQL이지만, MySQL 내부에서는 사용자별 correlated subquery, JSON 배열 생성, `GROUP_CONCAT` 집계를 수행합니다. 따라서 단순 조인보다 SQL 가독성과 실행계획 해석이 어려워질 수 있습니다.

| 관점 | `MULTISET` | join + reducer |
| --- | --- | --- |
| SQL 모양 | JSON 집계와 correlated subquery가 포함되어 복잡합니다. | `users -> users_roles -> roles` 조인이라 단순합니다. |
| Java 매핑 | 부모 row 안에 자식 컬렉션이 들어와 매핑 의도가 직접적입니다. | 펼쳐진 row를 reducer가 다시 묶어야 합니다. |
| 네트워크 전송 | user row가 role 수만큼 반복되지 않습니다. | role 수만큼 user 컬럼이 반복됩니다. |
| MySQL 적합성 | jOOQ 에뮬레이션 비용과 `GROUP_CONCAT` 제약을 고려해야 합니다. | MySQL에서 자연스러운 조인 실행계획을 기대하기 쉽습니다. |
| 페이지네이션 | 부모 user 기준 조회와 결합하기 쉽습니다. | 조인 row 기준 limit/offset 실수를 주의해야 합니다. |
| 학습 포인트 | jOOQ `MULTISET`의 장단점을 관찰하기 좋습니다. | 기존 aggregate reducer 패턴을 관찰하기 좋습니다. |

실무 기본값은 조회 목적에 따라 선택합니다. 단순 user-role 목록처럼 SQL 단순성과 운영 가독성이 더 중요하면 join + reducer가 더 적합할 수 있고, 부모 단위 결과 모양과 중첩 매핑을 명확히 보여줘야 하면 `MULTISET`을 제한적으로 사용할 수 있습니다.

## 실전 규칙

- 조합이 복잡한 조회는 adapter + reducer + factory로 나눕니다.
- 단순한 부모-자식 목록 조회를 비교 실험해야 하는 경우에는 `MULTISET`을 제한적으로 사용할 수 있습니다.
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
