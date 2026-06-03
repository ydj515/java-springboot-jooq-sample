package org.example.javaspringbootjooqsample.infrastructure.user;

import org.example.javaspringbootjooqsample.domain.user.Role;
import org.example.javaspringbootjooqsample.domain.user.User;
import org.example.javaspringbootjooqsample.domain.user.repository.UserRepository;
import org.example.javaspringbootjooqsample.infrastructure.jooq.reducer.UserAggregateRowReducer;
import org.example.javaspringbootjooqsample.support.MySqlJooqRepositoryTestSupport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("jooq-repository")
@Import({
        UserJooqRepositoryAdapter.class,
        UserAggregateRowReducer.class
})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class UserJooqRepositoryTests extends MySqlJooqRepositoryTestSupport {

    private final UserRepository userRepository;

    UserJooqRepositoryTests(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Test
    void findAllMapsUsersAndRolesWithMultiset() {
        List<User> users = userRepository.findAll();

        assertThat(users)
                .extracting(User::getUsername)
                .containsExactly("user456", "user123");
        assertThat(users.getFirst().getRoles())
                .extracting(Role::getName)
                .containsExactly("ADMIN", "AAA", "BBB");
        assertThat(users.get(1).getRoles())
                .extracting(Role::getName)
                .containsExactly("ADMIN", "PLAIN");
    }
}
