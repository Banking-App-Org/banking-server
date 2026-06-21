package com.banking.bankingserver.kafka;

import com.banking.bankingserver.event.NotificationEvent;
import com.banking.bankingserver.event.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class EventProducer {

    private static final String NOTIFICATION_TOPIC = "notification-events";

    @Autowired
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public void sendNotificationEvent(NotificationEvent event) {
        try {
            if (event.getEventId() == null) {
                event.setEventId(UUID.randomUUID().toString());
            }

            Message<NotificationEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, NOTIFICATION_TOPIC)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, event.getUserId())
                    .setHeader("X-Event-Type", event.getEventType())
                    .setHeader("X-Timestamp", System.currentTimeMillis())
                    .build();

            kafkaTemplate.send(message);
            log.info("Banking event sent: type={}, userId={}, eventId={}",
                    event.getEventType(), event.getUserId(), event.getEventId());
        } catch (Exception e) {
            log.error("Failed to send banking event: type={}, userId={}",
                    event.getEventType(), event.getUserId(), e);
            throw new RuntimeException("Failed to send event to Kafka", e);
        }
    }

    public void sendEvent(String userId, EventType eventType, String payload) {
        NotificationEvent event = NotificationEvent.builder()
                .userId(userId)
                .eventType(eventType.name())
                .payload(payload)
                .timestamp(java.time.LocalDateTime.now())
                .build();
        sendNotificationEvent(event);
    }

}
