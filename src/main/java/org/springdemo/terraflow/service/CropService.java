package org.springdemo.terraflow.service;

import org.springdemo.terraflow.dto.CropDTO;

import java.util.List;

public interface CropService {

    CropDTO create(CropDTO cropDTO, String ownerUsername);

    CropDTO getById(String id, String ownerUsername);

    List<CropDTO> getAllForUser(String ownerUsername);

    CropDTO update(String id, CropDTO cropDTO, String ownerUsername);

    void delete(String id, String ownerUsername);
}
