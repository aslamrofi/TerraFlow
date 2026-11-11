package org.springdemo.terraflow.mapper;

import org.springdemo.terraflow.dto.CropDTO;
import org.springdemo.terraflow.model.Crop;
import org.springframework.stereotype.Component;

@Component
public class CropMapper {

    public CropDTO toDto(Crop crop) {
        if (crop == null) return null;
        return CropDTO.builder()
                .id(crop.getId())
                .name(crop.getName())
                .variety(crop.getVariety())
                .plantingDate(crop.getPlantingDate())
                .growthStage(crop.getGrowthStage())
                .build();
    }

    public void updateEntityFromDto(CropDTO dto, Crop crop) {
        crop.setName(dto.getName());
        crop.setVariety(dto.getVariety());
        crop.setPlantingDate(dto.getPlantingDate());
        crop.setGrowthStage(dto.getGrowthStage());
    }
}