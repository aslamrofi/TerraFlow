package org.springdemo.terraflow.model;

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
public class FarmRecord {

    private String id;
    private LocalDate recordDate;
    private String category;
    private String description;
    private BigDecimal amount;
    private String ownerUsername;
}
