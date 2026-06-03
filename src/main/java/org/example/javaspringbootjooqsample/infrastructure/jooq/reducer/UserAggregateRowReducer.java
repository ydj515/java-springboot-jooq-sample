package org.example.javaspringbootjooqsample.infrastructure.jooq.reducer;

import org.example.javaspringbootjooqsample.domain.user.Role;
import org.example.javaspringbootjooqsample.domain.user.User;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.Roles;
import org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.Users;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Component
public class UserAggregateRowReducer {

    public List<User> reduce(Result<? extends Record> records, Users users, Roles roles) {
        Map<Long, UserAggregate> aggregates = new LinkedHashMap<>();

        for (Record record : records) {
            Long userId = record.get(users.ID);
            if (userId == null) {
                continue;
            }

            UserAggregate aggregate = aggregates.computeIfAbsent(userId, ignored -> new UserAggregate(toUser(record, users)));
            Long roleId = record.get(roles.ID);
            if (roleId == null || aggregate.rolesById.containsKey(roleId)) {
                continue;
            }

            aggregate.rolesById.put(roleId, toRole(record, roles));
        }

        return aggregates.values().stream()
                .map(UserAggregate::toUser)
                .toList();
    }

    private User toUser(Record record, Users users) {
        User user = new User();
        user.setId(record.get(users.ID));
        user.setUsername(record.get(users.USERNAME));
        user.setName(record.get(users.NAME));
        user.setUserType(record.get(users.USER_TYPE));
        user.setPassword(record.get(users.PASSWORD));
        user.setEmail(record.get(users.EMAIL));
        user.setLastLoginAt(record.get(users.LAST_LOGIN_AT));
        user.setCreatedAt(record.get(users.CREATED_AT));
        user.setUpdatedAt(record.get(users.UPDATED_AT));
        user.setDeletedAt(record.get(users.DELETED_AT));
        user.setLastPasswordUpdatedAt(record.get(users.LAST_PASSWORD_UPDATED_AT));
        user.setTrialCount(record.get(users.TRIAL_CNT) == null ? 0 : record.get(users.TRIAL_CNT));
        user.setRoles(new LinkedHashSet<>());
        return user;
    }

    private Role toRole(Record record, Roles roles) {
        return Role.builder()
                .id(record.get(roles.ID))
                .name(record.get(roles.NAME))
                .description(record.get(roles.DESCRIPTION))
                .createdAt(record.get(roles.CREATED_AT))
                .updatedAt(record.get(roles.UPDATED_AT))
                .build();
    }

    private static final class UserAggregate {
        private final User user;
        private final Map<Long, Role> rolesById = new LinkedHashMap<>();

        private UserAggregate(User user) {
            this.user = user;
        }

        private User toUser() {
            user.setRoles(new LinkedHashSet<>(rolesById.values()));
            return user;
        }
    }
}
