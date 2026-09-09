package com.discphy.infrastructure.persistence.alimtalk;

import com.discphy.core.alimtalk.AlimtalkSend;
import com.discphy.core.alimtalk.AlimtalkSendRepository;
import com.discphy.core.alimtalk.AlimtalkSendStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AlimtalkSendRepositoryImpl implements AlimtalkSendRepository {

    private final AlimtalkSendJpaRepository repository;

    public AlimtalkSend save(AlimtalkSend send) {
        return repository.saveAndFlush(send);
    }

    public Optional<AlimtalkSend> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<AlimtalkSend> findByNamespaceAndKey(String ns, String key) {
        return repository.findByNamespaceAndKey(ns, key);
    }

    public Optional<AlimtalkSend> findLockedById(Long id) {
        return repository.findLockedById(id);
    }

    public List<AlimtalkSend> findByStatus(AlimtalkSendStatus status) {
        return repository.findByStatus(status);
    }
}
