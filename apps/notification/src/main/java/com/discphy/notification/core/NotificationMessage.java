package com.discphy.notification.core;

import java.util.Map;
import java.util.Objects;

public record NotificationMessage(NotificationDestination destination, Map<String, String> variables) {

    public NotificationMessage {
        Objects.requireNonNull(destination);
        variables = Map.copyOf(variables);
    }
}
