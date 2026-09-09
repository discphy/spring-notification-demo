package com.discphy.application.alimtalk;

import com.discphy.core.alimtalk.AlimtalkSend;
import com.discphy.core.alimtalk.AlimtalkSendItem;
import com.discphy.core.alimtalk.AlimtalkSendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlimtalkSendCommandHandler {

    private final AlimtalkSendRepository repository;

    public record Registration(Long id, boolean created) {

    }

    @Transactional
    public Registration handle(AlimtalkSendCommand.Register command) {
        var existing = repository.findByNamespaceAndKey(command.namespace(), command.key());
        if (existing.isPresent()) {
            return new Registration(existing.get().getId(), false);
        }

        var items = command.items().stream()
                .map(item -> new AlimtalkSendItem(item.phoneNumber(), item.content()))
                .toList();
        var send = repository.save(new AlimtalkSend(command.namespace(), command.key(), items));

        return new Registration(send.getId(), true);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean handle(AlimtalkSendCommand.Claim command) {
        return repository.findLockedById(command.id()).orElseThrow().claim();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(AlimtalkSendCommand.Complete command) {
        var send = repository.findLockedById(command.id()).orElseThrow();

        for (var item : send.getItems()) {
            var result = command.results().stream().filter(r -> r.id().equals(item.getId())).findFirst().orElseThrow();
            item.complete(result.providerId(), result.error());
        }

        send.finish();
    }
}
