package org.springdemo.terraflow.repository;

import org.springdemo.terraflow.model.FarmRecord;

import java.util.List;
import java.util.Optional;

public interface FarmRecordRepository {
    FarmRecord save(FarmRecord record);
    Optional<FarmRecord> findById(String id);
    List<FarmRecord> findByOwnerUsernameOrderByRecordDateDesc(String ownerUsername);
    void delete(FarmRecord record);
}
