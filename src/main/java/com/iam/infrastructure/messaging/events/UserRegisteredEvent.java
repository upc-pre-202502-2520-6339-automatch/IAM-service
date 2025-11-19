package com.iam.infrastructure.messaging.events;

import java.io.Serializable;
import java.util.List;

public record UserRegisteredEvent(
        Long userId,
        String username,
        List<String> roles
) implements Serializable {}