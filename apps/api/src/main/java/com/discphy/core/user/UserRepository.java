package com.discphy.core.user;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);
}
