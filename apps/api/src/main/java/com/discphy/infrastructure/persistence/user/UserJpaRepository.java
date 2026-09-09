package com.discphy.infrastructure.persistence.user;

import com.discphy.core.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long> {

}
