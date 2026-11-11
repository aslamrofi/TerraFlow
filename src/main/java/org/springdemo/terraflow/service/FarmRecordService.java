package org.springdemo.terraflow.service;

import org.springdemo.terraflow.dto.FarmRecordDTO;

import java.util.List;

public interface FarmRecordService {
    List<FarmRecordDTO> getAllForUser(String username);
    FarmRecordDTO getById(String id, String username);
    FarmRecordDTO create(FarmRecordDTO dto, String username);
    FarmRecordDTO update(String id, FarmRecordDTO dto, String username);
    void delete(String id, String username);
}
