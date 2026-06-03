package org.example.javaspringbootjooqsample.support;

import org.example.javaspringbootjooqsample.config.jooq.JooqConfiguration;
import org.example.javaspringbootjooqsample.infrastructure.jooq.listener.JooqQueryCountListener;
import org.example.javaspringbootjooqsample.infrastructure.jooq.support.JooqQueryCounter;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.testcontainers.containers.MySQLContainer;

@JooqTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import({
        JooqConfiguration.class,
        JooqQueryCounter.class,
        JooqQueryCountListener.class
})
@SqlGroup({
        @Sql(scripts = "/db/common/reset-base-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "/db/common/user-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "/db/common/order-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
public abstract class MySqlJooqRepositoryTestSupport {

    @ServiceConnection
    static final MySQLContainer<?> MYSQL = TestMySqlContainers.shared();
}
