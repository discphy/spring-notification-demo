package com.discphy.application.alimtalk;

import java.util.List;

public final class AlimtalkSendCommand {

    private AlimtalkSendCommand() {
    }

    public record Item(String phoneNumber, String content) {

    }

    public record Register(String namespace, String key, List<Item> items) {

    }

    public record Claim(Long id) {

    }

    public record ItemResult(Long id, String providerId, String error) {

    }

    public record Complete(Long id, List<ItemResult> results) {

    }
}
