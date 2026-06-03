package org.example.javaspringbootjooqsample.domain.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistory {
    private Long id;
    private Long paymentId;
    private PaymentStatus fromStatus;
    private PaymentStatus toStatus;
    private LocalDateTime occurredAt;
    private String reason;

    public static PaymentHistory of(
            PaymentStatus fromStatus,
            PaymentStatus toStatus,
            LocalDateTime occurredAt,
            String reason
    ) {
        return PaymentHistory.builder()
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .occurredAt(occurredAt)
                .reason(reason)
                .build();
    }
}
