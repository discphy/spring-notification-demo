package com.discphy.notification.core.alimtalk;

public interface AlimtalkNotificationClient {

    String send(String phoneNumber, String content);
}
