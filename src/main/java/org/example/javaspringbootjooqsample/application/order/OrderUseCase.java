package org.example.javaspringbootjooqsample.application.order;

import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.application.compensation.CompensationService;
import org.example.javaspringbootjooqsample.application.order.command.AddOrderItemsCommand;
import org.example.javaspringbootjooqsample.application.order.command.CancelOrderCommand;
import org.example.javaspringbootjooqsample.application.order.command.CreateOrderCommand;
import org.example.javaspringbootjooqsample.application.order.command.FindOrdersCommand;
import org.example.javaspringbootjooqsample.application.order.command.GetOrderCommand;
import org.example.javaspringbootjooqsample.application.order.command.PayOrderCommand;
import org.example.javaspringbootjooqsample.application.order.command.ShipOrderCommand;
import org.example.javaspringbootjooqsample.application.order.result.AddOrderItemsResult;
import org.example.javaspringbootjooqsample.application.order.result.OrderResult;
import org.example.javaspringbootjooqsample.application.order.result.OrderStatusChangeResult;
import org.example.javaspringbootjooqsample.application.order.result.OrderStatusCountResult;
import org.example.javaspringbootjooqsample.application.order.result.OrderSummaryResult;
import org.example.javaspringbootjooqsample.domain.customer.Customer;
import org.example.javaspringbootjooqsample.domain.customer.service.CustomerLookupService;
import org.example.javaspringbootjooqsample.domain.order.Order;
import org.example.javaspringbootjooqsample.domain.order.OrderItem;
import org.example.javaspringbootjooqsample.domain.order.OrderStatus;
import org.example.javaspringbootjooqsample.domain.order.exception.OrderStateConflictException;
import org.example.javaspringbootjooqsample.domain.order.policy.OrderStatusTransitionPolicy;
import org.example.javaspringbootjooqsample.domain.order.repository.OrderRepository;
import org.example.javaspringbootjooqsample.domain.order.service.OrderItemAppendService;
import org.example.javaspringbootjooqsample.domain.order.service.OrderLookupService;
import org.example.javaspringbootjooqsample.domain.order.service.OrderNoGenerator;
import org.example.javaspringbootjooqsample.domain.payment.exception.PaymentApprovalFailedException;
import org.example.javaspringbootjooqsample.domain.payment.gateway.ApproveResult;
import org.example.javaspringbootjooqsample.domain.payment.gateway.PaymentGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderUseCase {
    private final OrderRepository orderRepository;
    private final OrderLookupService orderLookupService;
    private final OrderItemAppendService orderItemAppendService;
    private final OrderStatusTransitionPolicy orderStatusTransitionPolicy;
    private final CustomerLookupService customerLookupService;
    private final OrderNoGenerator orderNoGenerator;
    private final PaymentGateway paymentGateway;
    private final CompensationService compensationService;
    private final OrderPaymentTransactionService orderPaymentTransactionService;

    public List<OrderResult> getOrders(FindOrdersCommand command) {
        FindOrdersCommand safeCommand = command == null ? FindOrdersCommand.empty() : command;
        return orderRepository.findAll(safeCommand.toCriteria()).stream()
                .map(OrderResult::from)
                .toList();
    }

    public List<OrderResult> getOrdersWithNestedSelect(FindOrdersCommand command) {
        FindOrdersCommand safeCommand = command == null ? FindOrdersCommand.empty() : command;
        return orderRepository.findAllWithNestedSelect(safeCommand.toCriteria()).stream()
                .map(OrderResult::from)
                .toList();
    }

    public OrderResult getOrder(GetOrderCommand command) {
        Long orderId = command == null ? null : command.orderId();
        return OrderResult.from(orderLookupService.requireById(orderId));
    }

    public OrderResult getOrderWithNestedSelect(GetOrderCommand command) {
        Long orderId = command == null ? null : command.orderId();
        return OrderResult.from(orderLookupService.requireByIdWithNestedSelect(orderId));
    }

    public List<OrderSummaryResult> getOrderSummaries(FindOrdersCommand command) {
        FindOrdersCommand safeCommand = command == null ? FindOrdersCommand.empty() : command;
        return orderRepository.findSummaries(safeCommand.toCriteria()).stream()
                .map(OrderSummaryResult::from)
                .toList();
    }

    public List<OrderResult> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findAllByStatus(status).stream()
                .map(OrderResult::from)
                .toList();
    }

    public List<OrderSummaryResult> getOrderSummariesByStatus(OrderStatus status) {
        return orderRepository.findOrderSummariesByStatus(status).stream()
                .map(OrderSummaryResult::from)
                .toList();
    }

    public List<OrderStatusCountResult> countOrdersByStatus() {
        return orderRepository.countOrdersByStatus().stream()
                .map(OrderStatusCountResult::from)
                .toList();
    }

    @Transactional
    public OrderResult createOrder(CreateOrderCommand command) {
        Customer customer = customerLookupService.requireById(command.customerId());
        List<OrderItem> items = command.toDomainItems();

        BigDecimal totalAmount = items.stream()
                .map(OrderItem::getLineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
                .orderNo(orderNoGenerator.generate())
                .status(OrderStatus.CREATED)
                .version(0L)
                .totalAmount(totalAmount)
                .orderedAt(now)
                .deliveryRequestedAt(command.deliveryRequestedAt())
                .customer(customer)
                .items(items)
                .build();

        orderRepository.save(order);
        if (!items.isEmpty()) {
            orderRepository.insertOrderItems(order.getId(), items);
        }

        return OrderResult.from(orderLookupService.requireById(order.getId()));
    }

    @Transactional
    public AddOrderItemsResult addOrderItems(AddOrderItemsCommand command) {
        Long orderId = command == null ? null : command.getOrderId();
        List<OrderItem> items = command == null ? List.of() : command.toDomainItems();

        int addedItemCount = orderItemAppendService.addItems(orderId, items);
        return new AddOrderItemsResult(orderId, addedItemCount, "FOREACH");
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OrderStatusChangeResult payOrder(PayOrderCommand command) {
        // 1. 주문 결제에 필요한 정보 조회 및 Payment.REQUESTED 저장.
        //    이 트랜잭션은 여기서 끝나야 PG approve 동안 DB 커넥션/락을 점유하지 않는다.
        PayOrderPreparation preparation = orderPaymentTransactionService.preparePayOrder(command);
        if (preparation instanceof PayOrderPreparation.Replay replay) {
            return replay.result();
        }

        PayOrderPreparation.ApprovalRequired approvalRequired =
                (PayOrderPreparation.ApprovalRequired) preparation;

        // 2. PG 승인 요청.
        //    실제 운영에서는 외부 HTTP 호출이므로 어떤 DB 트랜잭션에도 묶지 않는다.
        ApproveResult approveResult;
        try {
            approveResult = paymentGateway.approve(approvalRequired.amount(), approvalRequired.idempotencyKey());
        } catch (PaymentApprovalFailedException e) {
            // 3-A. PG가 명시적으로 승인을 거절한 경우 Payment.FAILED audit만 별도 기록한다.
            orderPaymentTransactionService.markPaymentFailed(
                    approvalRequired.paymentId(),
                    e.getMessage() == null ? "PG declined" : e.getMessage(),
                    LocalDateTime.now()
            );
            throw e;
        }

        try {
            // 3-B. PG 승인 성공 audit을 먼저 독립 트랜잭션으로 확정한다.
            //      completePayOrder와 한 트랜잭션으로 묶으면 주문/Outbox 저장 실패 시 Payment.APPROVED까지
            //      롤백되어 승인된 외부 결제의 paymentKey/audit을 잃고 환불 보상도 불안정해진다.
            orderPaymentTransactionService.markPaymentApproved(
                    approvalRequired.paymentId(),
                    approveResult.paymentKey(),
                    approveResult.approvedAt()
            );
        } catch (RuntimeException e) {
            // 3-C. PG는 승인했지만 승인 audit 저장이 실패한 경우 즉시 환불 보상을 시도한다.
            compensationService.compensateApprovedPayment(
                    approvalRequired.paymentId(),
                    approveResult.paymentKey(),
                    approvalRequired.amount(),
                    "payOrder approval persistence failure: " + e.getMessage(),
                    LocalDateTime.now()
            );
            throw e;
        }

        try {
            // 4. 주문 PAID 전이와 OrderPaidEvent outbox 저장은 하나의 짧은 트랜잭션으로 묶는다.
            //    여기서 실패해도 3-B의 Payment.APPROVED audit은 남아 있어 안전하게 환불 보상할 수 있다.
            return orderPaymentTransactionService.completePayOrder(
                    approvalRequired.orderId(),
                    approvalRequired.paymentId(),
                    approveResult.paymentKey(),
                    approveResult.approvedAt()
            );
        } catch (RuntimeException e) {
            // 5. 승인 이후 주문 완료/Outbox 저장이 실패하면 이미 승인된 PG 결제를 환불 보상한다.
            compensationService.compensateApprovedPayment(
                    approvalRequired.paymentId(),
                    approveResult.paymentKey(),
                    approvalRequired.amount(),
                    "payOrder downstream failure: " + e.getMessage(),
                    LocalDateTime.now()
            );
            throw e;
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OrderStatusChangeResult cancelOrder(CancelOrderCommand command) {
        CancelOrderPreparation preparation = orderPaymentTransactionService.prepareCancelOrder(command);
        if (preparation instanceof CancelOrderPreparation.Replay replay) {
            return replay.result();
        }
        if (preparation instanceof CancelOrderPreparation.Completed completed) {
            return completed.result();
        }

        CancelOrderPreparation.RefundRequired refundRequired =
                (CancelOrderPreparation.RefundRequired) preparation;
        var outcome = compensationService.compensateApprovedPayment(
                refundRequired.paymentId(),
                refundRequired.paymentKey(),
                refundRequired.amount(),
                refundRequired.reason(),
                LocalDateTime.now()
        );
        orderPaymentTransactionService.recordCancellationRefundOutcome(refundRequired.cancellationId(), outcome);
        return refundRequired.result();
    }

    @Transactional
    public OrderStatusChangeResult shipOrder(ShipOrderCommand command) {
        Long orderId = command == null ? null : command.orderId();
        var order = orderLookupService.requireById(orderId);
        orderStatusTransitionPolicy.validateShippable(order);

        LocalDateTime now = LocalDateTime.now();
        int updated = orderRepository.updateStatusToShipped(
                order.getId(),
                OrderStatus.PAID,
                order.getVersion(),
                now
        );
        if (updated == 0) {
            throw new OrderStateConflictException(orderId, order.getStatus(), OrderStatus.SHIPPED, order.getVersion());
        }

        return OrderStatusChangeResult.from(orderLookupService.requireById(orderId));
    }

    @Transactional
    public int bulkShipPaidOrders(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return 0;
        }
        return orderRepository.bulkShipPaidOrders(orderIds, LocalDateTime.now());
    }
}
