package org.springdemo.terraflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropDTO {

    private String id;

    @NotBlank(message = "Crop name is required")
    private String name;

    @NotBlank(message = "Variety is required")
    private String variety;

    @NotNull(message = "Planting date is required")
    private LocalDate plantingDate;

    @NotBlank(message = "Growth stage is required")
    private String growthStage;
}
