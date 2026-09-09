package com.discphy.core.alimtalk;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class AlimtalkSendItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false, length = 4000)
    private String content;

    @Enumerated(EnumType.STRING)
    private AlimtalkSendStatus status = AlimtalkSendStatus.PENDING;

    private String providerId;

    @Column(length = 1000)
    private String error;

    protected AlimtalkSendItem() {
    }

    public AlimtalkSendItem(String phoneNumber, String content) {
        this.phoneNumber = phoneNumber;
        this.content = content;
    }

    public void complete(String providerId, String error) {
        if (status != AlimtalkSendStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 아이템입니다.");
        }
        this.providerId = providerId;
        this.error = error == null ? null : error.substring(0, Math.min(1000, error.length()));
        this.status = error == null ? AlimtalkSendStatus.SENT : AlimtalkSendStatus.FAILED;
    }
}
