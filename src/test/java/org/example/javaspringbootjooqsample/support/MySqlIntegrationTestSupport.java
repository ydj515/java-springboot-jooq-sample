package org.example.javaspringbootjooqsample.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.testcontainers.containers.MySQLContainer;

@ActiveProfiles("integration-test")
@SqlGroup({
        @Sql(scripts = "/db/common/reset-base-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "/db/common/user-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "/db/common/order-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
public abstract class MySqlIntegrationTestSupport {

    @ServiceConnection
    static final MySQLContainer<?> MYSQL = TestMySqlContainers.shared();
}
