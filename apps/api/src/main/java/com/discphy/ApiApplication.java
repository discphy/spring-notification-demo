package com.discphy;

import com.discphy.notification.NotificationConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.discphy.application.user", "com.discphy.infrastructure.persistence" +
        ".user", "com.discphy.infrastructure.notification.user", "com.discphy.presentation.user"})
@Import(NotificationConfiguration.class)
@EntityScan("com.discphy.core.user")
@EnableJpaRepositories("com.discphy.infrastructure.persistence.user")
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
