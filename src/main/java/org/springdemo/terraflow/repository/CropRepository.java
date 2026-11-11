package org.springdemo.terraflow.repository;

import org.springdemo.terraflow.model.Crop;

import java.util.List;
import java.util.Optional;

public interface CropRepository {
    Crop save(Crop crop);
    Optional<Crop> findById(String id);
    List<Crop> findByOwnerUsernameOrderByPlantingDateDesc(String ownerUsername);
    List<Crop> findByOwnerUsernameAndGrowthStage(String ownerUsername, String growthStage);
    void delete(Crop crop);
}
