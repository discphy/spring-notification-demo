package com.discphy.application.alimtalk;

import com.discphy.core.alimtalk.AlimtalkSend;
import com.discphy.core.alimtalk.AlimtalkSendRepository;
import com.discphy.core.alimtalk.AlimtalkSendStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
public class AlimtalkSendQueryService {

    private final AlimtalkSendRepository repository;

    public record Item(Long id, String phoneNumber, String content, AlimtalkSendStatus status, String providerId,
                       String error) {

    }

    public record Result(
            Long id,
            String namespace,
            String key,
            AlimtalkSendStatus status,
            List<Item> items
    ) {

    }

    public Result get(Long id) {
        var send = repository.findById(id).orElseThrow();
        var items = send.getItems().stream()
                .map(item -> new Item(
                        item.getId(),
                        item.getPhoneNumber(),
                        item.getContent(),
                        item.getStatus(),
                        item.getProviderId(),
                        item.getError()
                ))
                .toList();

        return new Result(
                send.getId(),
                send.getNamespace(),
                send.getKey(),
                send.getStatus(),
                items
        );
    }

    public List<Long> pendingIds() {
        return repository.findByStatus(AlimtalkSendStatus.PENDING).stream().map(AlimtalkSend::getId).toList();
    }
}
