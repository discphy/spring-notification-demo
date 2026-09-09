package com.discphy.notification.presentation;

import com.discphy.application.alimtalk.AlimtalkTemplateCommand;
import com.discphy.application.alimtalk.AlimtalkTemplateCommandHandler;
import com.discphy.notification.core.NotificationTemplateKey;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DemoTemplateInitializer implements ApplicationRunner {

    private final AlimtalkTemplateCommandHandler handler;

    public void run(ApplicationArguments args) {
        handler.handle(new AlimtalkTemplateCommand.Create(NotificationTemplateKey.USER_WELCOME, "#{name}님, 가입을 " +
                "환영합니다!"));
    }
}
