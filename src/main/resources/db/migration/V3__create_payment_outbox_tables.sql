CREATE TABLE payments
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT         NOT NULL,
    idempotency_key VARCHAR(255)   NOT NULL,
    amount          DECIMAL(12, 2) NOT NULL,
    status          VARCHAR(30)    NOT NULL,
    payment_key     VARCHAR(100),
    approved_at     DATETIME,
    refunded_at     DATETIME,
    version         BIGINT         NOT NULL DEFAULT 0,
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_idempotency_key (idempotency_key),
    FOREIGN KEY (order_id) REFERENCES purchase_orders (id)
);

CREATE TABLE payment_histories
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id  BIGINT       NOT NULL,
    from_status VARCHAR(30),
    to_status   VARCHAR(30)  NOT NULL,
    occurred_at DATETIME     NOT NULL,
    reason      VARCHAR(255),
    FOREIGN KEY (payment_id) REFERENCES payments (id)
);

CREATE TABLE outbox_events
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id        VARCHAR(36)  NOT NULL,
    aggregate_type  VARCHAR(50)  NOT NULL,
    aggregate_id    VARCHAR(100) NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         TEXT         NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    retry_count     INT          NOT NULL DEFAULT 0,
    next_attempt_at DATETIME     NOT NULL,
    published_at    DATETIME,
    last_error      VARCHAR(1000),
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_outbox_event_id (event_id),
    KEY idx_outbox_status_next (status, next_attempt_at)
);

CREATE TABLE processed_events
(
    event_id      VARCHAR(36)  NOT NULL,
    consumer_name VARCHAR(100) NOT NULL,
    processed_at  DATETIME     NOT NULL,
    PRIMARY KEY (event_id, consumer_name)
);

CREATE TABLE compensation_tasks
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_type       VARCHAR(50)   NOT NULL,
    payload         TEXT          NOT NULL,
    status          VARCHAR(20)   NOT NULL,
    retry_count     INT           NOT NULL DEFAULT 0,
    next_attempt_at DATETIME      NOT NULL,
    last_error      VARCHAR(1000),
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_compensation_status_next (status, next_attempt_at)
);

CREATE TABLE cancellations
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT       NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    reason          VARCHAR(500),
    status          VARCHAR(30)  NOT NULL,
    refunded_at     DATETIME,
    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_cancellation_idempotency_key (idempotency_key),
    FOREIGN KEY (order_id) REFERENCES purchase_orders (id)
);
