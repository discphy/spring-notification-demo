package com.discphy.notification.core;

import java.util.List;

public class NotificationRegistry {

    private final List<NotificationDispatcher> dispatchers;

    public NotificationRegistry(List<NotificationDispatcher> dispatchers) {
        this.dispatchers = List.copyOf(dispatchers);
    }

    public NotificationDispatcher getDispatcher(NotificationChannel channel) {
        var matches = dispatchers.stream()
                .filter(dispatcher -> dispatcher.supports(channel))
                .toList();

        if (matches.size() != 1) {
            throw new IllegalStateException("채널 Dispatcher는 하나여야 합니다: " + channel);
        }

        return matches.getFirst();
    }
}
