package org.springdemo.terraflow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springdemo.terraflow.dto.CropDTO;
import org.springdemo.terraflow.exception.ResourceNotFoundException;
import org.springdemo.terraflow.mapper.CropMapper;
import org.springdemo.terraflow.model.Crop;
import org.springdemo.terraflow.repository.CropRepository;
import org.springdemo.terraflow.service.CropService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CropServiceImpl implements CropService {

    private final CropRepository cropRepository;
    private final CropMapper cropMapper;
    // UserRepository is no longer needed here: Crop now stores ownerUsername
    // directly instead of a full User reference (see model/Crop.java).

    @Override
    public CropDTO create(CropDTO dto, String ownerUsername) {
        Crop crop = Crop.builder()
                .name(dto.getName())
                .variety(dto.getVariety())
                .plantingDate(dto.getPlantingDate())
                .growthStage(dto.getGrowthStage())
                .ownerUsername(ownerUsername)
                .build();

        Crop saved = cropRepository.save(crop);
        return cropMapper.toDto(saved);
    }

    @Override
    public CropDTO getById(String id, String ownerUsername) {
        Crop crop = findOwnedCrop(id, ownerUsername);
        return cropMapper.toDto(crop);
    }

    @Override
    public List<CropDTO> getAllForUser(String ownerUsername) {
        return cropRepository.findByOwnerUsernameOrderByPlantingDateDesc(ownerUsername)
                .stream()
                .map(cropMapper::toDto)
                .toList();
    }

    @Override
    public CropDTO update(String id, CropDTO dto, String ownerUsername) {
        Crop crop = findOwnedCrop(id, ownerUsername);
        cropMapper.updateEntityFromDto(dto, crop);
        return cropMapper.toDto(cropRepository.save(crop));
    }

    @Override
    public void delete(String id, String ownerUsername) {
        Crop crop = findOwnedCrop(id, ownerUsername);
        cropRepository.delete(crop);
    }

    // Enforces that a user can only read/update/delete their own crops
    private Crop findOwnedCrop(String id, String ownerUsername) {
        Crop crop = cropRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crop not found with id: " + id));

        if (!crop.getOwnerUsername().equals(ownerUsername)) {
            throw new AccessDeniedException("You do not own this crop record.");
        }
        return crop;
    }
}
