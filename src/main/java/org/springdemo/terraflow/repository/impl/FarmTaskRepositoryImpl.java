package org.springdemo.terraflow.repository.impl;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springdemo.terraflow.model.FarmTask;
import org.springdemo.terraflow.repository.FarmTaskRepository;
import org.springdemo.terraflow.util.FirebaseSync;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class FarmTaskRepositoryImpl implements FarmTaskRepository {

    private static final String NODE = "farmTasks";
    private final DatabaseReference rootRef;

    public FarmTaskRepositoryImpl(FirebaseDatabase firebaseDatabase) {
        this.rootRef = firebaseDatabase.getReference(NODE);
    }

    @Override
    public FarmTask save(FarmTask task) {
        boolean isNew = task.getId() == null || task.getId().isBlank();
        DatabaseReference ref = isNew ? rootRef.push() : rootRef.child(task.getId());
        if (isNew) {
            task.setId(ref.getKey());
        }
        FirebaseSync.write(ref, toMap(task));
        return task;
    }

    @Override
    public Optional<FarmTask> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(fromSnapshot(FirebaseSync.read(rootRef.child(id))));
    }

    @Override
    public List<FarmTask> findByOwnerUsernameOrderByDueDateAsc(String ownerUsername) {
        DataSnapshot snapshot = FirebaseSync.read(rootRef.orderByChild("ownerUsername").equalTo(ownerUsername));
        List<FarmTask> tasks = toList(snapshot);
        tasks.sort(Comparator.comparing(FarmTask::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));
        return tasks;
    }

    @Override
    public void delete(FarmTask task) {
        if (task == null || task.getId() == null) return;
        FirebaseSync.remove(rootRef.child(task.getId()));
    }

    private List<FarmTask> toList(DataSnapshot snapshot) {
        List<FarmTask> tasks = new ArrayList<>();
        if (snapshot == null || !snapshot.exists()) return tasks;
        for (DataSnapshot child : snapshot.getChildren()) {
            FarmTask task = fromSnapshot(child);
            if (task != null) tasks.add(task);
        }
        return tasks;
    }

    private Map<String, Object> toMap(FarmTask task) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", task.getTitle());
        map.put("description", task.getDescription());
        map.put("dueDate", task.getDueDate() == null ? null : task.getDueDate().toString());
        map.put("status", task.getStatus());
        map.put("ownerUsername", task.getOwnerUsername());
        return map;
    }

    private FarmTask fromSnapshot(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) return null;
        String dueDate = snapshot.child("dueDate").getValue(String.class);
        return FarmTask.builder()
                .id(snapshot.getKey())
                .title(snapshot.child("title").getValue(String.class))
                .description(snapshot.child("description").getValue(String.class))
                .dueDate(dueDate == null ? null : LocalDate.parse(dueDate))
                .status(snapshot.child("status").getValue(String.class))
                .ownerUsername(snapshot.child("ownerUsername").getValue(String.class))
                .build();
    }
}
