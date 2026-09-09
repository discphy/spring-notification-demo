package com.discphy.infrastructure.persistence.alimtalk;

import com.discphy.core.alimtalk.AlimtalkTemplate;
import com.discphy.core.alimtalk.AlimtalkTemplateRepository;
import com.discphy.notification.core.NotificationTemplateKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AlimtalkTemplateRepositoryImpl implements AlimtalkTemplateRepository {

    private final AlimtalkTemplateJpaRepository repository;

    public Optional<AlimtalkTemplate> findById(NotificationTemplateKey key) {
        return repository.findById(key);
    }

    public AlimtalkTemplate save(AlimtalkTemplate template) {
        return repository.save(template);
    }
}
