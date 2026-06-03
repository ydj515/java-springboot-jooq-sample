package org.example.javaspringbootjooqsample.domain.order.policy;

import org.example.javaspringbootjooqsample.domain.order.OrderItem;
import org.example.javaspringbootjooqsample.domain.order.exception.InvalidOrderItemException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
class OrderItemPolicyUnitTests {

    private final OrderItemPolicy orderItemPolicy = new OrderItemPolicy();

    @Test
    void throwsWhenLineAmountDoesNotMatchQuantityAndUnitPrice() {
        // given - 정책의 방어 로직을 검증하기 위해 일관성이 깨진 OrderItem을 직접 생성한다
        List<OrderItem> items = List.of(
                new OrderItem(null, "USB Hub", 2, new BigDecimal("29000"), new BigDecimal("30000"))
        );

        // when & then
        assertThatThrownBy(() -> orderItemPolicy.validateBatchItems(items))
                .isInstanceOf(InvalidOrderItemException.class)
                .hasMessage("주문 상품 1번의 lineAmount는 quantity * unitPrice와 같아야 합니다.");
    }

    @Test
    void passesWhenItemsAreValid() {
        // given - 도메인 factory가 자동 계산한 lineAmount는 항상 정책을 통과한다
        List<OrderItem> items = List.of(
                OrderItem.of("USB Hub", 2, new BigDecimal("29000"))
        );

        // when & then
        assertThatCode(() -> orderItemPolicy.validateBatchItems(items))
                .doesNotThrowAnyException();
    }
}
