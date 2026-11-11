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
public class FarmTaskDTO {

    private String id;

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    // "Pending", "Done", or the computed "Overdue"
    private String status;
}
