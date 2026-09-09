package com.discphy.infrastructure.persistence.alimtalk;

import com.discphy.core.alimtalk.AlimtalkTemplate;
import com.discphy.notification.core.NotificationTemplateKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlimtalkTemplateJpaRepository extends JpaRepository<AlimtalkTemplate, NotificationTemplateKey> {

}
