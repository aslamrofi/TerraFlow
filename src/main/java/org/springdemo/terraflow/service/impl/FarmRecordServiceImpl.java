package org.springdemo.terraflow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springdemo.terraflow.dto.FarmRecordDTO;
import org.springdemo.terraflow.exception.ResourceNotFoundException;
import org.springdemo.terraflow.model.FarmRecord;
import org.springdemo.terraflow.repository.FarmRecordRepository;
import org.springdemo.terraflow.service.FarmRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FarmRecordServiceImpl implements FarmRecordService {

    private final FarmRecordRepository farmRecordRepository;

    @Override
    public List<FarmRecordDTO> getAllForUser(String username) {
        return farmRecordRepository.findByOwnerUsernameOrderByRecordDateDesc(username)
                .stream().map(this::toDto).toList();
    }

    @Override
    public FarmRecordDTO getById(String id, String username) {
        return toDto(findOwned(id, username));
    }

    @Override
    public FarmRecordDTO create(FarmRecordDTO dto, String username) {
        FarmRecord record = FarmRecord.builder()
                .recordDate(dto.getRecordDate())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .ownerUsername(username)
                .build();
        return toDto(farmRecordRepository.save(record));
    }

    @Override
    public FarmRecordDTO update(String id, FarmRecordDTO dto, String username) {
        FarmRecord record = findOwned(id, username);
        record.setRecordDate(dto.getRecordDate());
        record.setCategory(dto.getCategory());
        record.setDescription(dto.getDescription());
        record.setAmount(dto.getAmount());
        return toDto(farmRecordRepository.save(record));
    }

    @Override
    public void delete(String id, String username) {
        farmRecordRepository.delete(findOwned(id, username));
    }

    private FarmRecord findOwned(String id, String username) {
        FarmRecord record = farmRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
        if (!record.getOwnerUsername().equals(username)) {
            throw new ResourceNotFoundException("Record not found");
        }
        return record;
    }

    private FarmRecordDTO toDto(FarmRecord record) {
        return FarmRecordDTO.builder()
                .id(record.getId())
                .recordDate(record.getRecordDate())
                .category(record.getCategory())
                .description(record.getDescription())
                .amount(record.getAmount())
                .build();
    }
}
