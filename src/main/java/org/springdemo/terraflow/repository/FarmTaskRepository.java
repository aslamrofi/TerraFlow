package org.springdemo.terraflow.repository;

import org.springdemo.terraflow.model.FarmTask;

import java.util.List;
import java.util.Optional;

public interface FarmTaskRepository {
    FarmTask save(FarmTask task);
    Optional<FarmTask> findById(String id);
    List<FarmTask> findByOwnerUsernameOrderByDueDateAsc(String ownerUsername);
    void delete(FarmTask task);
}
