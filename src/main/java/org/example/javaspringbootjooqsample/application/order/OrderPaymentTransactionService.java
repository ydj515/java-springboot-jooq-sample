package org.example.javaspringbootjooqsample.application.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.javaspringbootjooqsample.application.compensation.CompensationOutcome;
import org.example.javaspringbootjooqsample.application.order.command.CancelOrderCommand;
import org.example.javaspringbootjooqsample.application.order.command.PayOrderCommand;
import org.example.javaspringbootjooqsample.application.order.result.OrderStatusChangeResult;
import org.example.javaspringbootjooqsample.domain.order.Cancellation;
import org.example.javaspringbootjooqsample.domain.order.Order;
import org.example.javaspringbootjooqsample.domain.order.OrderStatus;
import org.example.javaspringbootjooqsample.domain.order.event.OrderPaidEvent;
import org.example.javaspringbootjooqsample.domain.order.exception.OrderNotFoundException;
import org.example.javaspringbootjooqsample.domain.order.exception.OrderStateConflictException;
import org.example.javaspringbootjooqsample.domain.order.policy.OrderStatusTransitionPolicy;
import org.example.javaspringbootjooqsample.domain.order.repository.CancellationRepository;
import org.example.javaspringbootjooqsample.domain.order.repository.OrderRepository;
import org.example.javaspringbootjooqsample.domain.outbox.OutboxEvent;
import org.example.javaspringbootjooqsample.domain.outbox.repository.OutboxEventRepository;
import org.example.javaspringbootjooqsample.domain.payment.Payment;
import org.example.javaspringbootjooqsample.domain.payment.PaymentStatus;
import org.example.javaspringbootjooqsample.domain.payment.exception.IdempotencyConflictException;
import org.example.javaspringbootjooqsample.domain.payment.exception.PaymentApprovalFailedException;
import org.example.javaspringbootjooqsample.domain.payment.exception.PaymentNotFoundException;
import org.example.javaspringbootjooqsample.domain.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderPaymentTransactionService {

    private static final List<OrderStatus> CANCELLABLE_STATUSES = List.of(OrderStatus.CREATED, OrderStatus.PAID);

    private final OrderRepository orderRepository;
    private final OrderStatusTransitionPolicy orderStatusTransitionPolicy;
    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final CancellationRepository cancellationRepository;

    @Transactional
    public PayOrderPreparation preparePayOrder(PayOrderCommand command) {
        Long orderId = command == null ? null : command.orderId();
        String idempotencyKey = command == null ? null : command.idempotencyKey();
        Order order = requireOrderForUpdate(orderId);

        Payment existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing != null) {
            return new PayOrderPreparation.Replay(replayExistingPayment(existing, order));
        }

        orderStatusTransitionPolicy.validatePayable(order);
        rejectIfOrderHasActivePayment(order.getId());
        Payment payment = paymentRepository.save(
                Payment.request(order.getId(), idempotencyKey, order.getTotalAmount(), LocalDateTime.now())
        );

        return new PayOrderPreparation.ApprovalRequired(
                order.getId(),
                payment.getId(),
                payment.getAmount(),
                idempotencyKey
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPaymentApproved(Long paymentId, String paymentKey, LocalDateTime approvedAt) {
        Payment payment = findPayment(paymentId);
        payment.markApproved(paymentKey, approvedAt);
        paymentRepository.save(payment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPaymentFailed(Long paymentId, String reason, LocalDateTime occurredAt) {
        Payment payment = findPayment(paymentId);
        payment.markFailed(reason, occurredAt);
        paymentRepository.save(payment);
    }

    @Transactional
    public OrderStatusChangeResult completePayOrder(
            Long orderId,
            Long paymentId,
            String paymentKey,
            LocalDateTime approvedAt
    ) {
        Order order = requireOrderForUpdate(orderId);
        Payment payment = findPayment(paymentId);

        if (!payment.getOrderId().equals(order.getId())) {
            throw new IllegalStateException(
                    "payment order mismatch: paymentId=%s paymentOrderId=%s orderId=%s"
                            .formatted(paymentId, payment.getOrderId(), order.getId())
            );
        }
        if (payment.getStatus() != PaymentStatus.APPROVED || !paymentKey.equals(payment.getPaymentKey())) {
            throw new IllegalStateException(
                    "payment must be APPROVED before completing order: paymentId=%s status=%s"
                            .formatted(paymentId, payment.getStatus())
            );
        }

        orderStatusTransitionPolicy.validatePayable(order);
        int updated = orderRepository.updateStatusToPaid(
                order.getId(),
                OrderStatus.CREATED,
                order.getVersion(),
                approvedAt
        );
        if (updated == 0) {
            throw new OrderStateConflictException(orderId, order.getStatus(), OrderStatus.PAID, order.getVersion());
        }

        publishOrderPaidEventToOutbox(orderId, payment, paymentKey, approvedAt);
        return OrderStatusChangeResult.from(requireOrder(orderId), paymentKey);
    }

    @Transactional
    public CancelOrderPreparation prepareCancelOrder(CancelOrderCommand command) {
        Order order = requireOrderForUpdate(command.orderId());

        Cancellation existing = cancellationRepository.findByIdempotencyKey(command.idempotencyKey());
        if (existing != null) {
            return new CancelOrderPreparation.Replay(replayExistingCancellation(existing, order, command));
        }

        orderStatusTransitionPolicy.validateCancellable(order);
        Cancellation cancellation = cancellationRepository.save(
                Cancellation.requested(order.getId(), command.idempotencyKey(), command.reason())
        );

        boolean wasPaid = order.getStatus() == OrderStatus.PAID;
        LocalDateTime now = LocalDateTime.now();
        int updated = orderRepository.updateStatusToCancelled(
                order.getId(),
                CANCELLABLE_STATUSES,
                order.getVersion(),
                now
        );
        if (updated == 0) {
            throw new OrderStateConflictException(
                    command.orderId(), order.getStatus(), OrderStatus.CANCELLED, order.getVersion()
            );
        }

        if (!wasPaid) {
            cancellation.markSucceeded(null);
            cancellationRepository.save(cancellation);
            return new CancelOrderPreparation.Completed(OrderStatusChangeResult.from(requireOrder(command.orderId())));
        }

        Payment approved = paymentRepository.findByOrderId(order.getId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.APPROVED)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("PAID order has no APPROVED payment: orderId=" + order.getId())
                );

        return new CancelOrderPreparation.RefundRequired(
                OrderStatusChangeResult.from(requireOrder(command.orderId())),
                cancellation.getId(),
                approved.getId(),
                approved.getPaymentKey(),
                approved.getAmount(),
                "order cancel: " + (command.reason() == null ? "user requested" : command.reason())
        );
    }

    @Transactional
    public void recordCancellationRefundOutcome(Long cancellationId, CompensationOutcome outcome) {
        Cancellation cancellation = cancellationRepository.findById(cancellationId);
        if (cancellation == null) {
            throw new IllegalStateException("cancellation not found: id=" + cancellationId);
        }

        if (outcome instanceof CompensationOutcome.Refunded refunded) {
            cancellation.markSucceeded(refunded.refundedAt());
        } else {
            cancellation.markRefundFailed();
        }
        cancellationRepository.save(cancellation);
    }

    private void publishOrderPaidEventToOutbox(
            Long orderId,
            Payment payment,
            String paymentKey,
            LocalDateTime paidAt
    ) {
        OrderPaidEvent event = new OrderPaidEvent(
                UUID.randomUUID().toString(),
                orderId,
                payment.getId(),
                paymentKey,
                payment.getAmount(),
                paidAt
        );
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize OrderPaidEvent", e);
        }
        outboxEventRepository.save(
                OutboxEvent.pending(
                        OrderPaidEvent.AGGREGATE_TYPE,
                        String.valueOf(orderId),
                        OrderPaidEvent.EVENT_TYPE,
                        payload,
                        LocalDateTime.now()
                )
        );
    }

    private Payment findPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId);
        if (payment == null) {
            throw new PaymentNotFoundException("payment not found: id=" + paymentId);
        }
        return payment;
    }

    private Order requireOrderForUpdate(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }
        return order;
    }

    private Order requireOrder(Long orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId);
        }
        return order;
    }

    private void rejectIfOrderHasActivePayment(Long orderId) {
        Payment activePayment = paymentRepository.findByOrderId(orderId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.REQUESTED || p.getStatus() == PaymentStatus.APPROVED)
                .findFirst()
                .orElse(null);
        if (activePayment == null) {
            return;
        }

        throw new IdempotencyConflictException(
                "order already has an active payment (paymentId=%s, status=%s)"
                        .formatted(activePayment.getId(), activePayment.getStatus())
        );
    }

    private OrderStatusChangeResult replayExistingPayment(Payment existing, Order order) {
        if (!existing.getOrderId().equals(order.getId())) {
            throw new IdempotencyConflictException(
                    "idempotency key already used for another order (orderId=" + existing.getOrderId() + ")"
            );
        }
        if (existing.getAmount().compareTo(order.getTotalAmount()) != 0) {
            throw new IdempotencyConflictException(
                    "idempotency key was used with a different amount (expected="
                            + existing.getAmount() + ", requested=" + order.getTotalAmount() + ")"
            );
        }
        return switch (existing.getStatus()) {
            case APPROVED -> OrderStatusChangeResult.from(requireOrder(order.getId()), existing.getPaymentKey());
            case FAILED -> throw new PaymentApprovalFailedException(
                    "payment previously failed for this idempotency key"
            );
            case REQUESTED -> throw new IdempotencyConflictException(
                    "payment for this idempotency key is still in progress"
            );
            default -> throw new IdempotencyConflictException(
                    "payment for this idempotency key is in an unrecoverable state: " + existing.getStatus()
            );
        };
    }

    private OrderStatusChangeResult replayExistingCancellation(
            Cancellation existing,
            Order order,
            CancelOrderCommand command
    ) {
        if (!existing.getOrderId().equals(order.getId())) {
            throw new IdempotencyConflictException(
                    "idempotency key already used for another order (orderId=" + existing.getOrderId() + ")"
            );
        }
        String existingReason = existing.getReason() == null ? "" : existing.getReason();
        String requestedReason = command.reason() == null ? "" : command.reason();
        if (!existingReason.equals(requestedReason)) {
            throw new IdempotencyConflictException(
                    "idempotency key was used with a different reason (expected="
                            + existing.getReason() + ", requested=" + command.reason() + ")"
            );
        }
        return OrderStatusChangeResult.from(requireOrder(order.getId()));
    }
}
