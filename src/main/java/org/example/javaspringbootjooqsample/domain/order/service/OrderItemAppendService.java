package org.example.javaspringbootjooqsample.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.domain.order.OrderItem;
import org.example.javaspringbootjooqsample.domain.order.policy.OrderItemPolicy;
import org.example.javaspringbootjooqsample.domain.order.repository.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderItemAppendService {

    private final OrderRepository orderRepository;
    private final OrderLookupService orderLookupService;
    private final OrderItemPolicy orderItemPolicy;

    public int addItems(Long orderId, List<OrderItem> items) {
        validateAppendRequest(orderId, items);
        return orderRepository.insertOrderItems(orderId, items);
    }

    public void validateAppendRequest(Long orderId, List<OrderItem> items) {
        orderLookupService.requireById(orderId);
        orderItemPolicy.validateBatchItems(items);
    }
}
