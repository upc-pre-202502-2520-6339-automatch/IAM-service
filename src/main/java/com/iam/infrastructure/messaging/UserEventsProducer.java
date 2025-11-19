// com.iam.infrastructure.messaging.UserEventsProducer
package com.iam.infrastructure.messaging;

import com.iam.infrastructure.messaging.events.UserRegisteredEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventsProducer {

    private static final String TOPIC = "user-registered";

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public UserEventsProducer(KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserRegistered(UserRegisteredEvent event) {
        kafkaTemplate.send(TOPIC, event.userId().toString(), event);
    }
}
