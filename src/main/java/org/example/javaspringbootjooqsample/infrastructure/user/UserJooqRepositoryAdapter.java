package org.example.javaspringbootjooqsample.infrastructure.user;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.domain.user.Role;
import org.example.javaspringbootjooqsample.domain.user.User;
import org.example.javaspringbootjooqsample.domain.user.repository.UserRepository;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.Roles;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.Users;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.UsersRoles;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.records.UsersRecord;
import org.example.javaspringbootjooqsample.infrastructure.jooq.reducer.UserAggregateRowReducer;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record5;
import org.jooq.SelectConditionStep;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.Roles.ROLES;
import static org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.Users.USERS;
import static org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.UsersRoles.USERS_ROLES;

@Repository
@RequiredArgsConstructor
public class UserJooqRepositoryAdapter implements UserRepository {

    private final DSLContext dsl;
    private final UserAggregateRowReducer reducer;

    @Override
    public List<User> findAll() {

//        Users users = USERS.as("u");
//        UsersRoles usersRoles = USERS_ROLES.as("ur");
//        Roles roles = ROLES.as("r");
//
//        return reducer.reduce(
//            selectUsersWithRoles(users, usersRoles, roles)
//                .orderBy(users.ID.desc(), roles.ID.asc())
//                .fetch(),
//            users,
//            roles
//        );
        DSLContext multisetDsl = dslWithoutGroupConcatSessionVariable();
        Field<Set<Role>> rolesField = DSL.multiset(
                DSL.select(
                        ROLES.ID,
                        ROLES.NAME,
                        ROLES.DESCRIPTION,
                        ROLES.CREATED_AT,
                        ROLES.UPDATED_AT
                    )
                    .from(USERS_ROLES)
                    .join(ROLES).on(ROLES.ID.eq(USERS_ROLES.ROLE_ID))
                    .where(USERS_ROLES.USER_ID.eq(USERS.ID))
                    .orderBy(ROLES.ID.asc())
            )
            .convertFrom(roleRecords -> {
                Set<Role> roles = new LinkedHashSet<>(roleRecords.map(this::toRole));
                return roles;
            })
            .as("roles");

        return multisetDsl.select(
                USERS.ID,
                USERS.USERNAME,
                USERS.NAME,
                USERS.USER_TYPE,
                USERS.PASSWORD,
                USERS.EMAIL,
                USERS.LAST_LOGIN_AT,
                USERS.CREATED_AT,
                USERS.UPDATED_AT,
                USERS.DELETED_AT,
                USERS.LAST_PASSWORD_UPDATED_AT,
                USERS.TRIAL_CNT,
                rolesField
            )
            .from(USERS)
            .orderBy(USERS.ID.desc())
            .fetch(record -> {
                User user = new User();
                user.setId(record.get(USERS.ID));
                user.setUsername(record.get(USERS.USERNAME));
                user.setName(record.get(USERS.NAME));
                user.setUserType(record.get(USERS.USER_TYPE));
                user.setPassword(record.get(USERS.PASSWORD));
                user.setEmail(record.get(USERS.EMAIL));
                user.setLastLoginAt(record.get(USERS.LAST_LOGIN_AT));
                user.setCreatedAt(record.get(USERS.CREATED_AT));
                user.setUpdatedAt(record.get(USERS.UPDATED_AT));
                user.setDeletedAt(record.get(USERS.DELETED_AT));
                user.setLastPasswordUpdatedAt(record.get(USERS.LAST_PASSWORD_UPDATED_AT));
                user.setTrialCount(record.get(USERS.TRIAL_CNT) == null ? 0 : record.get(USERS.TRIAL_CNT));
                user.setRoles(record.get(rolesField));
                return user;
            });
    }

    @Override
    public User findById(Long id) {
        Users users = USERS.as("u");
        UsersRoles usersRoles = USERS_ROLES.as("ur");
        Roles roles = ROLES.as("r");

        return reducer.reduce(
                selectUsersWithRoles(users, usersRoles, roles)
                    .and(users.ID.eq(id))
                    .orderBy(users.ID.desc(), roles.ID.asc())
                    .fetch(),
                users,
                roles
            )
            .stream()
            .findFirst()
            .orElse(null);
    }

    @Override
    public User findByUsername(String username) {
        Users users = USERS.as("u");
        UsersRoles usersRoles = USERS_ROLES.as("ur");
        Roles roles = ROLES.as("r");

        return reducer.reduce(
                selectUsersWithRoles(users, usersRoles, roles)
                    .and(users.USERNAME.eq(username))
                    .orderBy(users.ID.desc(), roles.ID.asc())
                    .fetch(),
                users,
                roles
            )
            .stream()
            .findFirst()
            .orElse(null);
    }

    @Override
    public int save(User user) {
        UsersRecord inserted = dsl.insertInto(USERS)
            .set(USERS.USERNAME, user.getUsername())
            .set(USERS.NAME, user.getName())
            .set(USERS.USER_TYPE, user.getUserType())
            .set(USERS.PASSWORD, user.getPassword())
            .set(USERS.EMAIL, user.getEmail())
            .set(USERS.LAST_LOGIN_AT, user.getLastLoginAt())
            .set(USERS.LAST_PASSWORD_UPDATED_AT, user.getLastPasswordUpdatedAt())
            .set(USERS.TRIAL_CNT, user.getTrialCount())
            .returning(USERS.ID)
            .fetchOne();

        if (inserted == null) {
            return 0;
        }

        user.setId(inserted.getId());
        return 1;
    }

    @Override
    public int update(User user) {
        return dsl.update(USERS)
            .set(USERS.USERNAME, user.getUsername())
            .set(USERS.NAME, user.getName())
            .set(USERS.USER_TYPE, user.getUserType())
            .set(USERS.PASSWORD, user.getPassword())
            .set(USERS.EMAIL, user.getEmail())
            .set(USERS.LAST_LOGIN_AT, user.getLastLoginAt())
            .set(USERS.UPDATED_AT, user.getUpdatedAt())
            .set(USERS.DELETED_AT, user.getDeletedAt())
            .set(USERS.LAST_PASSWORD_UPDATED_AT, user.getLastPasswordUpdatedAt())
            .set(USERS.TRIAL_CNT, user.getTrialCount())
            .where(USERS.ID.eq(user.getId()))
            .execute();
    }

    @Override
    public int deleteUserRolesByUserId(Long userId) {
        return dsl.deleteFrom(USERS_ROLES)
            .where(USERS_ROLES.USER_ID.eq(userId))
            .execute();
    }

    @Override
    public int delete(Long id) {
        return dsl.deleteFrom(USERS)
            .where(USERS.ID.eq(id))
            .execute();
    }

    private SelectConditionStep<?> selectUsersWithRoles(Users users, UsersRoles usersRoles, Roles roles) {
        return dsl.select(
                users.ID,
                users.USERNAME,
                users.NAME,
                users.USER_TYPE,
                users.PASSWORD,
                users.EMAIL,
                users.LAST_LOGIN_AT,
                users.CREATED_AT,
                users.UPDATED_AT,
                users.DELETED_AT,
                users.LAST_PASSWORD_UPDATED_AT,
                users.TRIAL_CNT,
                roles.ID,
                roles.NAME,
                roles.DESCRIPTION,
                roles.CREATED_AT,
                roles.UPDATED_AT
            )
            .from(users)
            .leftJoin(usersRoles).on(usersRoles.USER_ID.eq(users.ID))
            .leftJoin(roles).on(roles.ID.eq(usersRoles.ROLE_ID))
            .where(DSL.noCondition());
    }

    private Role toRole(Record5<Long, String, String, LocalDateTime, LocalDateTime> record) {
        return Role.builder()
            .id(record.value1())
            .name(record.value2())
            .description(record.value3())
            .createdAt(record.value4())
            .updatedAt(record.value5())
            .build();
    }

    private DSLContext dslWithoutGroupConcatSessionVariable() {
        Settings settings = (Settings) dsl.configuration().settings().clone();
        settings.withRenderGroupConcatMaxLenSessionVariable(false);
        return DSL.using(dsl.configuration().derive(settings));
    }
}
