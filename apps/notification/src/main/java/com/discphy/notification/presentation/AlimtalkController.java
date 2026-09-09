package com.discphy.notification.presentation;

import com.discphy.application.alimtalk.AlimtalkSendQueryService;
import com.discphy.notification.application.alimtalk.AlimtalkNotificationDispatcher;
import com.discphy.notification.core.NotificationMessage;
import com.discphy.notification.core.NotificationRegistry;
import com.discphy.notification.core.NotificationSendType;
import com.discphy.notification.core.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alimtalk")
@RequiredArgsConstructor
public class AlimtalkController {

    private final NotificationRegistry registry;

    private final AlimtalkNotificationDispatcher alimtalk;

    private final AlimtalkSendQueryService queries;

    public record Message(String phoneNumber, java.util.Map<String, String> variables) {

    }

    public record Request(NotificationSendType.User sendType, String key, List<Message> messages) {

    }

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.ACCEPTED)
    public void send(@RequestBody Request request) {
        var dispatcher = registry.getDispatcher(request.sendType().channel());

        dispatcher.send(request.sendType(), request.key(), request.messages().stream()
                .map(m -> new NotificationMessage(new PhoneNumber(m.phoneNumber()), m.variables())).toList());
    }

    @GetMapping("/{id}")
    public AlimtalkSendQueryService.Result get(@PathVariable Long id) {
        return queries.get(id);
    }

    @PostMapping("/dispatch-pending")
    public void dispatchPending() {
        queries.pendingIds().forEach(alimtalk::dispatch);
    }
}
