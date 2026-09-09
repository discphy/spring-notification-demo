package com.discphy.application.user;

public final class UserCommand {

    private UserCommand() {
    }

    public record Create(String name, String phoneNumber) {

    }
}
