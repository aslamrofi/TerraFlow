package org.springdemo.terraflow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springdemo.terraflow.dto.FarmTaskDTO;
import org.springdemo.terraflow.exception.ResourceNotFoundException;
import org.springdemo.terraflow.model.FarmTask;
import org.springdemo.terraflow.repository.FarmTaskRepository;
import org.springdemo.terraflow.service.FarmTaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FarmTaskServiceImpl implements FarmTaskService {

    private final FarmTaskRepository farmTaskRepository;

    @Override
    public List<FarmTaskDTO> getAllForUser(String username) {
        return farmTaskRepository.findByOwnerUsernameOrderByDueDateAsc(username)
                .stream().map(this::toDto).toList();
    }

    @Override
    public FarmTaskDTO getById(String id, String username) {
        return toDto(findOwned(id, username));
    }

    @Override
    public FarmTaskDTO create(FarmTaskDTO dto, String username) {
        FarmTask task = FarmTask.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .status(dto.getStatus() == null || dto.getStatus().isBlank() ? "Pending" : dto.getStatus())
                .ownerUsername(username)
                .build();
        return toDto(farmTaskRepository.save(task));
    }

    @Override
    public FarmTaskDTO update(String id, FarmTaskDTO dto, String username) {
        FarmTask task = findOwned(id, username);
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            task.setStatus(dto.getStatus());
        }
        return toDto(farmTaskRepository.save(task));
    }

    @Override
    public void delete(String id, String username) {
        farmTaskRepository.delete(findOwned(id, username));
    }

    @Override
    public long countActiveForUser(String username) {
        return farmTaskRepository.findByOwnerUsernameOrderByDueDateAsc(username)
                .stream().filter(t -> !"Done".equalsIgnoreCase(t.getStatus())).count();
    }

    @Override
    public List<FarmTaskDTO> getUpcomingForUser(String username, int limit) {
        return farmTaskRepository.findByOwnerUsernameOrderByDueDateAsc(username)
                .stream()
                .filter(t -> !"Done".equalsIgnoreCase(t.getStatus()))
                .limit(limit)
                .map(this::toDto)
                .toList();
    }

    private FarmTask findOwned(String id, String username) {
        FarmTask task = farmTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!task.getOwnerUsername().equals(username)) {
            throw new ResourceNotFoundException("Task not found");
        }
        return task;
    }

    // "Overdue" is shown when a task is still open past its due date; it is
    // never stored, so a task that's edited still shows its real status.
    private FarmTaskDTO toDto(FarmTask task) {
        String displayStatus = task.getStatus();
        if (!"Done".equalsIgnoreCase(displayStatus) && task.getDueDate().isBefore(LocalDate.now())) {
            displayStatus = "Overdue";
        }
        return FarmTaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .status(displayStatus)
                .build();
    }
}
