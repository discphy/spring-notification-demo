package com.discphy.core.alimtalk;

import com.discphy.notification.core.NotificationTemplateKey;

import java.util.Optional;

public interface AlimtalkTemplateRepository {

    Optional<AlimtalkTemplate> findById(NotificationTemplateKey key);

    AlimtalkTemplate save(AlimtalkTemplate template);
}
