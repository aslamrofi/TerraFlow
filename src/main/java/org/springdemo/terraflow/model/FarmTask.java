package org.springdemo.terraflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmTask {

    private String id;
    private String title;
    private String description;
    private LocalDate dueDate;

    // "Pending" or "Done". "Overdue" is computed, not stored.
    @Builder.Default
    private String status = "Pending";

    private String ownerUsername;
}
