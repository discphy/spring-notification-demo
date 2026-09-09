package com.discphy.notification.application.alimtalk;

import com.discphy.notification.core.NotificationSendMode;

public record AlimtalkSendEvent(Long groupId, NotificationSendMode sendMode) {

}
