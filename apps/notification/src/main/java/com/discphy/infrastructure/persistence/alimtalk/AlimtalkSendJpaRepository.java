package com.discphy.infrastructure.persistence.alimtalk;

import com.discphy.core.alimtalk.AlimtalkSend;
import com.discphy.core.alimtalk.AlimtalkSendStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AlimtalkSendJpaRepository extends JpaRepository<AlimtalkSend, Long> {

    Optional<AlimtalkSend> findByNamespaceAndKey(String namespace, String key);

    List<AlimtalkSend> findByStatus(AlimtalkSendStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AlimtalkSend s where s.id = :id")
    Optional<AlimtalkSend> findLockedById(Long id);
}
