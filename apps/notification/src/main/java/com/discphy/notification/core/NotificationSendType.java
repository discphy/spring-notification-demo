package com.discphy.notification.core;

public sealed interface NotificationSendType permits NotificationSendType.User {

    String namespace();

    NotificationChannel channel();

    NotificationSendMode sendMode();

    NotificationTemplateKey templateKey();

    String name();

    enum User implements NotificationSendType {
        WELCOME(NotificationSendMode.IMMEDIATE),
        WELCOME_BATCH(NotificationSendMode.BATCH);

        private final NotificationSendMode mode;

        User(NotificationSendMode mode) {
            this.mode = mode;
        }

        public String namespace() {
            return "USER";
        }

        public NotificationChannel channel() {
            return NotificationChannel.ALIMTALK;
        }

        public NotificationSendMode sendMode() {
            return mode;
        }

        public NotificationTemplateKey templateKey() {
            return NotificationTemplateKey.USER_WELCOME;
        }
    }
}
