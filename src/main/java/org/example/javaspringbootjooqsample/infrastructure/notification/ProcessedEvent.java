package org.example.javaspringbootjooqsample.infrastructure.notification;

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
public class ProcessedEvent {
    private String eventId;
    private String consumerName;
    private LocalDateTime processedAt;
}
