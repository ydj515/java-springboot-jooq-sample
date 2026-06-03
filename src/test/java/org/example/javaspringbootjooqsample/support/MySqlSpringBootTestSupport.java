package org.example.javaspringbootjooqsample.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;

@ActiveProfiles("test")
public abstract class MySqlSpringBootTestSupport {

    @ServiceConnection
    static final MySQLContainer<?> MYSQL = TestMySqlContainers.shared();
}
