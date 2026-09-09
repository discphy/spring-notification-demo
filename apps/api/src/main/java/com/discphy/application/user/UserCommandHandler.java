package com.discphy.application.user;

import com.discphy.core.user.User;
import com.discphy.core.user.UserNotification;
import com.discphy.core.user.UserNotifier;
import com.discphy.core.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCommandHandler {

    private final UserRepository repository;

    private final UserNotifier notifier;

    @Transactional
    public Long handle(UserCommand.Create command) {
        var user = repository.save(new User(command.name(), command.phoneNumber()));
        notifier.notify(new UserNotification(user.getId(), user.getName(), user.getPhoneNumber()));
        return user.getId();
    }
}
