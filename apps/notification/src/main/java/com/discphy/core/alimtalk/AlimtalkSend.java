package com.discphy.core.alimtalk;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"namespace", "request_key"}))
public class AlimtalkSend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String namespace;

    @Column(name = "request_key", nullable = false)
    private String key;

    @Enumerated(EnumType.STRING)
    private AlimtalkSendStatus status = AlimtalkSendStatus.PENDING;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "send_id", nullable = false)
    private List<AlimtalkSendItem> items = new ArrayList<>();

    protected AlimtalkSend() {
    }

    public AlimtalkSend(String namespace, String key, List<AlimtalkSendItem> items) {
        this.namespace = namespace;
        this.key = key;
        this.items.addAll(items);
    }

    public boolean claim() {
        if (status != AlimtalkSendStatus.PENDING) {
            return false;
        }

        status = AlimtalkSendStatus.PROCESSING;
        return true;
    }

    public void finish() {
        if (status != AlimtalkSendStatus.PROCESSING || items.stream()
                .anyMatch(i -> i.getStatus() == AlimtalkSendStatus.PENDING)) {
            throw new IllegalStateException("발송 결과가 완성되지 않았습니다.");
        }

        status = items.stream().allMatch(item -> item.getStatus() == AlimtalkSendStatus.SENT)
                ? AlimtalkSendStatus.SENT
                : AlimtalkSendStatus.FAILED;
    }
}
