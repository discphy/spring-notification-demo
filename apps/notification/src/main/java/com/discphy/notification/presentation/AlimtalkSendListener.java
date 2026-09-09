package com.discphy.notification.presentation;

import com.discphy.notification.application.alimtalk.AlimtalkNotificationDispatcher;
import com.discphy.notification.application.alimtalk.AlimtalkSendEvent;
import com.discphy.notification.core.NotificationSendMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AlimtalkSendListener {

    private final AlimtalkNotificationDispatcher dispatcher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(AlimtalkSendEvent event) {
        if (event.sendMode() == NotificationSendMode.IMMEDIATE) {
            dispatcher.dispatch(event.groupId());
        }
    }
}
