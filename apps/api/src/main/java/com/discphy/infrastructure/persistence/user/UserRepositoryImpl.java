package com.discphy.infrastructure.persistence.user;

import com.discphy.core.user.User;
import com.discphy.core.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository repository;

    public User save(User user) {
        return repository.save(user);
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }
}
