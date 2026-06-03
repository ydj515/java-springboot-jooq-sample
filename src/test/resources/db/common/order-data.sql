INSERT INTO customers (name, email)
VALUES ('한수진', 'sujin.han@example.com');

INSERT INTO customers (name, email)
VALUES ('강민호', 'minho.kang@example.com');

INSERT INTO customers (name, email)
VALUES ('최아라', 'ara.choi@example.com');

INSERT INTO purchase_orders (
    customer_id, order_no, order_status, version, total_amount, ordered_at, delivery_requested_at, paid_at, shipped_at,
    cancelled_at, created_at, updated_at, tracking_number, cancel_reason
)
VALUES (
    1, 'ORD-2024-0001', 'CREATED', 0, 169000.00, '2024-07-01 09:30:00', '2024-07-03 18:00:00', NULL, NULL, NULL,
    '2024-07-01 09:31:00', '2024-07-01 09:31:00', NULL, NULL
);

INSERT INTO purchase_orders (
    customer_id, order_no, order_status, version, total_amount, ordered_at, delivery_requested_at, paid_at, shipped_at,
    cancelled_at, created_at, updated_at, tracking_number, cancel_reason
)
VALUES (
    1, 'ORD-2024-0002', 'PAID', 1, 87000.00, '2024-07-05 14:10:00', '2024-07-06 18:00:00', '2024-07-05 14:20:00',
    NULL, NULL, '2024-07-05 14:10:30', '2024-07-05 14:20:00', NULL, NULL
);

INSERT INTO purchase_orders (
    customer_id, order_no, order_status, version, total_amount, ordered_at, delivery_requested_at, paid_at, shipped_at,
    cancelled_at, created_at, updated_at, tracking_number, cancel_reason
)
VALUES (
    2, 'ORD-2024-0003', 'SHIPPED', 2, 243000.00, '2024-07-07 10:00:00', '2024-07-08 12:00:00', '2024-07-07 10:05:00',
    '2024-07-07 13:00:00', NULL, '2024-07-07 10:00:10', '2024-07-07 13:00:00', 'TRACK-2024-0003', NULL
);

INSERT INTO purchase_orders (
    customer_id, order_no, order_status, version, total_amount, ordered_at, delivery_requested_at, paid_at, shipped_at,
    cancelled_at, created_at, updated_at, tracking_number, cancel_reason
)
VALUES (
    3, 'ORD-2024-0004', 'CANCELLED', 1, 59000.00, '2024-07-08 16:20:00', NULL, NULL, NULL, '2024-07-08 16:25:00',
    '2024-07-08 16:20:10', '2024-07-08 16:25:00', NULL, '결제 검증 실패로 주문이 취소되었습니다.'
);

INSERT INTO purchase_order_items (order_id, product_name, quantity, unit_price, line_amount)
VALUES (1, 'Mechanical Keyboard', 1, 129000.00, 129000.00);

INSERT INTO purchase_order_items (order_id, product_name, quantity, unit_price, line_amount)
VALUES (1, 'Wrist Rest', 1, 40000.00, 40000.00);

INSERT INTO purchase_order_items (order_id, product_name, quantity, unit_price, line_amount)
VALUES (2, 'Wireless Mouse', 1, 52000.00, 52000.00);

INSERT INTO purchase_order_items (order_id, product_name, quantity, unit_price, line_amount)
VALUES (2, 'Desk Mat', 1, 35000.00, 35000.00);

INSERT INTO purchase_order_items (order_id, product_name, quantity, unit_price, line_amount)
VALUES (3, '27-inch Monitor', 1, 219000.00, 219000.00);

INSERT INTO purchase_order_items (order_id, product_name, quantity, unit_price, line_amount)
VALUES (3, 'HDMI Cable', 2, 12000.00, 24000.00);

INSERT INTO purchase_order_items (order_id, product_name, quantity, unit_price, line_amount)
VALUES (4, 'Web Camera', 1, 59000.00, 59000.00);
