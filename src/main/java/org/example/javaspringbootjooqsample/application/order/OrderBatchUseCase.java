package org.example.javaspringbootjooqsample.application.order;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.application.order.command.AddOrderItemsCommand;
import org.example.javaspringbootjooqsample.application.order.result.AddOrderItemsResult;
import org.example.javaspringbootjooqsample.domain.order.OrderItem;
import org.example.javaspringbootjooqsample.domain.order.service.OrderItemAppendService;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.example.javaspringbootjooqsample.generated.jooq_codegen.tables.PurchaseOrderItems.PURCHASE_ORDER_ITEMS;

@Service
@RequiredArgsConstructor
public class OrderBatchUseCase {
    private final DSLContext dsl;
    private final OrderItemAppendService orderItemAppendService;

    // jOOQ 배치는 Spring 트랜잭션 경계 안에서 실행해 전체 추가 요청을 원자적으로 처리합니다.
    @Transactional
    public AddOrderItemsResult addOrderItemsWithBatchSession(AddOrderItemsCommand command) {
        Long orderId = command == null ? null : command.getOrderId();
        List<OrderItem> items = command == null ? List.of() : command.toDomainItems();

        orderItemAppendService.validateAppendRequest(orderId, items);

        BatchBindStep batch = dsl.batch(
                dsl.insertInto(
                                PURCHASE_ORDER_ITEMS,
                                PURCHASE_ORDER_ITEMS.ORDER_ID,
                                PURCHASE_ORDER_ITEMS.PRODUCT_NAME,
                                PURCHASE_ORDER_ITEMS.QUANTITY,
                                PURCHASE_ORDER_ITEMS.UNIT_PRICE,
                                PURCHASE_ORDER_ITEMS.LINE_AMOUNT
                        )
                        .values((Long) null, null, null, null, null)
        );

        for (OrderItem item : items) {
            batch.bind(
                    orderId,
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getLineAmount()
            );
        }

        batch.execute();
        return new AddOrderItemsResult(orderId, items.size(), "JOOQ_BATCH");
    }
}
