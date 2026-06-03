package org.example.javaspringbootjooqsample.application.order.result;

public record AddOrderItemsResult(
        Long orderId,
        int addedItemCount,
        String strategy
) {
}
