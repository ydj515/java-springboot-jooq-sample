package org.example.javaspringbootjooqsample.presentation.order;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.application.order.OrderBatchUseCase;
import org.example.javaspringbootjooqsample.application.order.OrderUseCase;
import org.example.javaspringbootjooqsample.application.order.command.CancelOrderCommand;
import org.example.javaspringbootjooqsample.application.order.command.GetOrderCommand;
import org.example.javaspringbootjooqsample.application.order.command.PayOrderCommand;
import org.example.javaspringbootjooqsample.application.order.command.ShipOrderCommand;
import org.example.javaspringbootjooqsample.application.order.result.AddOrderItemsResult;
import org.example.javaspringbootjooqsample.application.order.result.OrderResult;
import org.example.javaspringbootjooqsample.application.order.result.OrderStatusChangeResult;
import org.example.javaspringbootjooqsample.domain.order.OrderStatus;
import org.example.javaspringbootjooqsample.presentation.order.request.AddOrderItemsRequest;
import org.example.javaspringbootjooqsample.presentation.order.request.CreateOrderRequest;
import org.example.javaspringbootjooqsample.presentation.order.request.OrderSearchRequest;
import org.example.javaspringbootjooqsample.presentation.order.response.AddOrderItemsResponse;
import org.example.javaspringbootjooqsample.presentation.order.response.OrderResponse;
import org.example.javaspringbootjooqsample.presentation.order.response.OrderStatusChangeResponse;
import org.example.javaspringbootjooqsample.presentation.order.response.OrderStatusCountResponse;
import org.example.javaspringbootjooqsample.presentation.order.response.OrderSummaryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderUseCase orderUseCase;
    private final OrderBatchUseCase orderBatchUseCase;

    @GetMapping("")
    public ResponseEntity<List<OrderResponse>> getOrders(@ModelAttribute OrderSearchRequest request) {
        List<OrderResponse> orders = orderUseCase.getOrders(request.toCommand()).stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/nested-select")
    public ResponseEntity<List<OrderResponse>> getOrdersWithNestedSelect(@ModelAttribute OrderSearchRequest request) {
        List<OrderResponse> orders = orderUseCase.getOrdersWithNestedSelect(request.toCommand()).stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        OrderResult order = orderUseCase.getOrder(new GetOrderCommand(id));
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @GetMapping("/{id}/nested-select")
    public ResponseEntity<OrderResponse> getOrderWithNestedSelect(@PathVariable Long id) {
        OrderResult order = orderUseCase.getOrderWithNestedSelect(new GetOrderCommand(id));
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @GetMapping("/summaries")
    public ResponseEntity<List<OrderSummaryResponse>> getOrderSummaries(@ModelAttribute OrderSearchRequest request) {
        List<OrderSummaryResponse> summaries = orderUseCase.getOrderSummaries(request.toCommand()).stream()
                .map(OrderSummaryResponse::from)
                .toList();
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<OrderResponse> orders = orderUseCase.getOrdersByStatus(status).stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}/summaries")
    public ResponseEntity<List<OrderSummaryResponse>> getOrderSummariesByStatus(@PathVariable OrderStatus status) {
        List<OrderSummaryResponse> summaries = orderUseCase.getOrderSummariesByStatus(status).stream()
                .map(OrderSummaryResponse::from)
                .toList();
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/status-counts")
    public ResponseEntity<List<OrderStatusCountResponse>> countOrdersByStatus() {
        List<OrderStatusCountResponse> counts = orderUseCase.countOrdersByStatus().stream()
                .map(OrderStatusCountResponse::from)
                .toList();
        return ResponseEntity.ok(counts);
    }

    @PostMapping("")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        OrderResult order = orderUseCase.createOrder(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }

    @PostMapping("/{id}/items/batch")
    public ResponseEntity<AddOrderItemsResponse> addOrderItems(
            @PathVariable Long id,
            @RequestBody AddOrderItemsRequest request
    ) {
        AddOrderItemsResult result = orderUseCase.addOrderItems(request.toCommand(id));
        return ResponseEntity.ok(AddOrderItemsResponse.from(result));
    }

    @PostMapping("/{id}/items/batch-session")
    public ResponseEntity<AddOrderItemsResponse> addOrderItemsWithBatchSession(
            @PathVariable Long id,
            @RequestBody AddOrderItemsRequest request
    ) {
        AddOrderItemsResult result = orderBatchUseCase.addOrderItemsWithBatchSession(request.toCommand(id));
        return ResponseEntity.ok(AddOrderItemsResponse.from(result));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderStatusChangeResponse> payOrder(
            @PathVariable Long id,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        String validatedKey = validateIdempotencyKey(idempotencyKey);
        OrderStatusChangeResult result = orderUseCase.payOrder(new PayOrderCommand(id, validatedKey));
        return ResponseEntity.ok(OrderStatusChangeResponse.from(result));
    }

    private static final int IDEMPOTENCY_KEY_MAX_LENGTH = 255;

    private String validateIdempotencyKey(String rawKey) {
        String trimmed = rawKey == null ? "" : rawKey.trim();
        if (trimmed.isEmpty()) {
            throw new org.example.javaspringbootjooqsample.domain.payment.exception.IdempotencyKeyRequiredException();
        }
        if (trimmed.length() > IDEMPOTENCY_KEY_MAX_LENGTH) {
            throw new org.example.javaspringbootjooqsample.domain.payment.exception.IdempotencyKeyInvalidException(
                    "idempotency key must be at most " + IDEMPOTENCY_KEY_MAX_LENGTH + " characters"
            );
        }
        return trimmed;
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderStatusChangeResponse> cancelOrder(
            @PathVariable Long id,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestParam(value = "reason", required = false) String reason
    ) {
        String validatedKey = validateIdempotencyKey(idempotencyKey);
        OrderStatusChangeResult result = orderUseCase.cancelOrder(new CancelOrderCommand(id, validatedKey, reason));
        return ResponseEntity.ok(OrderStatusChangeResponse.from(result));
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<OrderStatusChangeResponse> shipOrder(@PathVariable Long id) {
        OrderStatusChangeResult result = orderUseCase.shipOrder(new ShipOrderCommand(id));
        return ResponseEntity.ok(OrderStatusChangeResponse.from(result));
    }
}
