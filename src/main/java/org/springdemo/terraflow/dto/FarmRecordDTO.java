package org.springdemo.terraflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmRecordDTO {

    private String id;

    @NotNull(message = "Date is required")
    private LocalDate recordDate;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Description is required")
    private String description;

    private BigDecimal amount;
}
