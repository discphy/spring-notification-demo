package com.discphy.application.alimtalk;

import com.discphy.core.alimtalk.AlimtalkTemplate;
import com.discphy.core.alimtalk.AlimtalkTemplateRepository;
import com.discphy.notification.core.NotificationTemplateKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlimtalkTemplateQueryService {

    private final AlimtalkTemplateRepository repository;

    @Transactional(readOnly = true)
    public AlimtalkTemplate get(NotificationTemplateKey key) {
        return repository.findById(key).orElseThrow(() -> new IllegalArgumentException("템플릿이 없습니다: " + key));
    }
}
