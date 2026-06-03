package org.example.javaspringbootjooqsample.presentation.order.response;

import org.example.javaspringbootjooqsample.application.order.result.AddOrderItemsResult;

public record AddOrderItemsResponse(
        Long orderId,
        int addedItemCount,
        String strategy
) {
    public static AddOrderItemsResponse from(AddOrderItemsResult result) {
        return new AddOrderItemsResponse(
                result.orderId(),
                result.addedItemCount(),
                result.strategy()
        );
    }
}
