package com.discphy.core.alimtalk;

import java.util.List;
import java.util.Optional;

public interface AlimtalkSendRepository {

    AlimtalkSend save(AlimtalkSend send);

    Optional<AlimtalkSend> findById(Long id);

    Optional<AlimtalkSend> findByNamespaceAndKey(String namespace, String key);

    Optional<AlimtalkSend> findLockedById(Long id);

    List<AlimtalkSend> findByStatus(AlimtalkSendStatus status);
}
