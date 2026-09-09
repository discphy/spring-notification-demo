package com.discphy.application.user;

import com.discphy.core.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository repository;

    public record Result(Long id, String name, String phoneNumber) {

    }

    @Transactional(readOnly = true)
    public Result get(Long id) {
        var u = repository.findById(id).orElseThrow();
        return new Result(u.getId(), u.getName(), u.getPhoneNumber());
    }
}
