package com.discphy.application.alimtalk;

import com.discphy.notification.core.NotificationMessage;
import com.discphy.notification.core.NotificationSendType;
import com.discphy.notification.core.NotificationTemplateKey;
import com.discphy.notification.core.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AlimtalkComponent {

    private final AlimtalkTemplateQueryService templateQueryService;

    private final AlimtalkSendCommandHandler sendCommandHandler;

    private final AlimtalkSendQueryService sendQueryService;

    public List<AlimtalkSendCommand.Item> render(
            NotificationTemplateKey templateKey,
            List<NotificationMessage> messages
    ) {
        var template = templateQueryService.get(templateKey);

        return messages.stream()
                .map(message -> {
                    if (!(message.destination() instanceof PhoneNumber phone)) {
                        throw new IllegalArgumentException(
                                "알림톡 수신 대상은 전화번호여야 합니다."
                        );
                    }

                    var content = template.render(message.variables());

                    return new AlimtalkSendCommand.Item(phone.value(), content);
                })
                .toList();
    }

    public AlimtalkSendCommandHandler.Registration register(
            NotificationSendType sendType,
            String key,
            List<AlimtalkSendCommand.Item> items
    ) {
        var command = new AlimtalkSendCommand.Register(
                sendType.namespace(),
                key,
                items
        );

        return sendCommandHandler.handle(command);
    }

    public boolean claim(Long groupId) {
        return sendCommandHandler.handle(
                new AlimtalkSendCommand.Claim(groupId)
        );
    }

    public AlimtalkSendQueryService.Result get(Long groupId) {
        return sendQueryService.get(groupId);
    }

    public void complete(
            Long groupId,
            List<AlimtalkSendCommand.ItemResult> results
    ) {
        sendCommandHandler.handle(
                new AlimtalkSendCommand.Complete(groupId, results)
        );
    }
}
