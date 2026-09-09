package com.discphy.notification.core;

public record PhoneNumber(String value) implements NotificationDestination {

    public PhoneNumber {
        if (value == null || !value.matches("0[0-9]{9,10}")) {
            throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다.");
        }
    }
}
