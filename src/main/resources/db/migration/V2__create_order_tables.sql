CREATE TABLE customers
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE purchase_orders
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id           BIGINT         NOT NULL,
    order_no              VARCHAR(50)    NOT NULL,
    order_status          VARCHAR(30)    NOT NULL,
    version               BIGINT         NOT NULL DEFAULT 0,
    total_amount          DECIMAL(12, 2) NOT NULL,
    ordered_at            DATETIME       NOT NULL,
    delivery_requested_at DATETIME,
    paid_at               DATETIME,
    shipped_at            DATETIME,
    cancelled_at          DATETIME,
    created_at            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tracking_number       VARCHAR(100),
    cancel_reason         VARCHAR(255),
    UNIQUE KEY unique_order_no (order_no),
    FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE TABLE purchase_order_items
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT         NOT NULL,
    product_name VARCHAR(255)   NOT NULL,
    quantity     INT            NOT NULL,
    unit_price   DECIMAL(12, 2) NOT NULL,
    line_amount  DECIMAL(12, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES purchase_orders (id)
);
