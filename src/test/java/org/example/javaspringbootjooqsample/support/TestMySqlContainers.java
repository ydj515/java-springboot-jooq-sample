package org.example.javaspringbootjooqsample.support;

import org.testcontainers.containers.MySQLContainer;

public final class TestMySqlContainers {

    private static final String IMAGE = "mysql:8.4.3";
    private static final MySQLContainer<?> SHARED_MYSQL = create("jooq_test", "test_user", "test_password");

    static {
        SHARED_MYSQL.start();
    }

    private TestMySqlContainers() {
    }

    public static MySQLContainer<?> create(String databaseName, String username, String password) {
        return new MySQLContainer<>(IMAGE)
                .withDatabaseName(databaseName)
                .withUsername(username)
                .withPassword(password)
                .withUrlParam("serverTimezone", "Asia/Seoul")
                .withUrlParam("characterEncoding", "UTF-8");
    }

    public static MySQLContainer<?> shared() {
        return SHARED_MYSQL;
    }
}
