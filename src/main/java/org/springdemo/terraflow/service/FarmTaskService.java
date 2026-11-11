package org.springdemo.terraflow.service;

import org.springdemo.terraflow.dto.FarmTaskDTO;

import java.util.List;

public interface FarmTaskService {
    List<FarmTaskDTO> getAllForUser(String username);
    FarmTaskDTO getById(String id, String username);
    FarmTaskDTO create(FarmTaskDTO dto, String username);
    FarmTaskDTO update(String id, FarmTaskDTO dto, String username);
    void delete(String id, String username);
    long countActiveForUser(String username);
    List<FarmTaskDTO> getUpcomingForUser(String username, int limit);
}
