package com.discphy.notification;

import com.discphy.notification.core.NotificationDispatcher;
import com.discphy.notification.core.NotificationRegistry;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

@Configuration
@ComponentScan(basePackages = {
        "com.discphy.notification.application",
        "com.discphy.notification.infrastructure",
        "com.discphy.notification.presentation",
        "com.discphy.application.alimtalk",
        "com.discphy.infrastructure.persistence.alimtalk"
})
@EntityScan("com.discphy.core.alimtalk")
@EnableJpaRepositories("com.discphy.infrastructure.persistence.alimtalk")
public class NotificationConfiguration {

    @Bean
    public NotificationRegistry notificationRegistry(List<NotificationDispatcher> dispatchers) {
        return new NotificationRegistry(dispatchers);
    }
}
