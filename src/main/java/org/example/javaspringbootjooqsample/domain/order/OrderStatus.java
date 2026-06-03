package org.example.javaspringbootjooqsample.domain.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    CREATED("CREATED"),
    PAID("PAID"),
    SHIPPED("SHIPPED"),
    CANCELLED("CANCELLED");

    private final String code;

    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Unknown order status: " + code);
    }
}
