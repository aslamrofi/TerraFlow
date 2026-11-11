package org.springdemo.terraflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Crop {

    private String id;

    private String name; // e.g. "Tomato - Plot A"

    private String variety;

    private LocalDate plantingDate;

    private String growthStage; // e.g. SEEDLING, VEGETATIVE, FLOWERING, HARVEST

    private String plotLocation;

    private String notes;

    // Was a @ManyToOne User reference under JPA. Firebase has no joins, and
    // storing a full nested User under every crop would duplicate and stale
    // that data, so this now matches the ownerUsername pattern already used
    // by FarmRecord and FarmTask.
    private String ownerUsername;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
