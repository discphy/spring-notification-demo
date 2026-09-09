package com.discphy.notification.core;

import java.util.List;

public interface NotificationDispatcher {

    void send(NotificationSendType sendType, String key, List<NotificationMessage> messages);

    boolean supports(NotificationChannel channel);
}
