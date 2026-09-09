package com.discphy.application.alimtalk;

import com.discphy.notification.core.NotificationTemplateKey;

public final class AlimtalkTemplateCommand {

    private AlimtalkTemplateCommand() {
    }

    public record Create(NotificationTemplateKey key, String content) {

    }
}
