package org.example.javaspringbootjooqsample.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.domain.order.Order;
import org.example.javaspringbootjooqsample.domain.order.exception.OrderIdRequiredException;
import org.example.javaspringbootjooqsample.domain.order.exception.OrderNotFoundException;
import org.example.javaspringbootjooqsample.domain.order.repository.OrderRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderLookupService {

    private final OrderRepository orderRepository;

    public Order requireById(Long orderId) {
        validateOrderId(orderId);

        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }

        return order;
    }

    public Order requireByIdWithNestedSelect(Long orderId) {
        validateOrderId(orderId);

        Order order = orderRepository.findByIdWithNestedSelect(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }

        return order;
    }

    private void validateOrderId(Long orderId) {
        if (orderId == null) {
            throw new OrderIdRequiredException();
        }
    }
}
