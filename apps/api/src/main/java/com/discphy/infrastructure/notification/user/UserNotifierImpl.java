package com.discphy.infrastructure.notification.user;

import com.discphy.core.user.UserNotification;
import com.discphy.core.user.UserNotifier;
import com.discphy.notification.core.NotificationMessage;
import com.discphy.notification.core.NotificationRegistry;
import com.discphy.notification.core.NotificationSendType;
import com.discphy.notification.core.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserNotifierImpl implements UserNotifier {

    private final NotificationRegistry registry;

    @Override
    public void notify(UserNotification notification) {
        var sendType = NotificationSendType.User.WELCOME;
        var dispatcher = registry.getDispatcher(sendType.channel());

        dispatcher.send(
                sendType,
                "welcome:" + notification.userId(),
                List.of(new NotificationMessage(
                        new PhoneNumber(notification.phoneNumber()),
                        Map.of("name", notification.name())
                ))
        );
    }
}
