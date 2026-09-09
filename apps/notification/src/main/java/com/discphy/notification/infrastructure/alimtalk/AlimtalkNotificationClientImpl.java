package com.discphy.notification.infrastructure.alimtalk;

import com.discphy.notification.core.alimtalk.AlimtalkNotificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class AlimtalkNotificationClientImpl implements AlimtalkNotificationClient {

    public String send(String phoneNumber, String content) {
        if (phoneNumber.endsWith("0000")) {
            throw new IllegalStateException("데모 공급자 발송 실패");
        }
        String id = "demo-" + UUID.randomUUID();
        log.info("데모 알림톡 접수 성공: providerId={}", id);
        return id;
    }
}
