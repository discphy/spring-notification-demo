package com.discphy.notification.application.alimtalk;

import com.discphy.application.alimtalk.AlimtalkComponent;
import com.discphy.application.alimtalk.AlimtalkSendCommand;
import com.discphy.notification.core.NotificationChannel;
import com.discphy.notification.core.NotificationDispatcher;
import com.discphy.notification.core.NotificationMessage;
import com.discphy.notification.core.NotificationSendType;
import com.discphy.notification.core.alimtalk.AlimtalkNotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlimtalkNotificationDispatcher implements NotificationDispatcher {

    private final AlimtalkComponent alimtalkComponent;

    private final AlimtalkNotificationClient client;

    private final ApplicationEventPublisher events;

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.ALIMTALK;
    }

    @Override
    @Transactional
    public void send(NotificationSendType type, String key, List<NotificationMessage> messages) {
        if (key == null || key.isBlank() || key.length() > 255 || messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("키와 메시지는 필수입니다. 키는 255자 이하입니다.");
        }

        var items = alimtalkComponent.render(type.templateKey(), messages);
        var registered = alimtalkComponent.register(type, key, items);

        if (registered.created()) {
            events.publishEvent(new AlimtalkSendEvent(registered.id(), type.sendMode()));
        }
    }

    // 접수를 반복하지 않고, 저장된 그룹만 처리한다. Client 호출에는 DB 트랜잭션을 걸지 않는다.
    public void dispatch(Long groupId) {
        if (!alimtalkComponent.claim(groupId)) {
            return;
        }

        var send = alimtalkComponent.get(groupId);
        var results = new ArrayList<AlimtalkSendCommand.ItemResult>();

        for (var item : send.items()) {
            try {
                String providerId = client.send(item.phoneNumber(), item.content());
                results.add(new AlimtalkSendCommand.ItemResult(item.id(), providerId, null));
            } catch (RuntimeException exception) {
                log.error("알림톡 발송 실패: groupId={}, itemId={}", groupId, item.id(), exception);
                results.add(new AlimtalkSendCommand.ItemResult(item.id(), null, exception.toString()));
            }
        }

        alimtalkComponent.complete(groupId, results);
    }
}
