package org.springdemo.terraflow.repository.impl;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springdemo.terraflow.model.Crop;
import org.springdemo.terraflow.repository.CropRepository;
import org.springdemo.terraflow.util.FirebaseSync;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
public class CropRepositoryImpl implements CropRepository {

    private static final String NODE = "crops";
    private final DatabaseReference rootRef;

    public CropRepositoryImpl(FirebaseDatabase firebaseDatabase) {
        this.rootRef = firebaseDatabase.getReference(NODE);
    }

    @Override
    public Crop save(Crop crop) {
        boolean isNew = crop.getId() == null || crop.getId().isBlank();
        DatabaseReference ref = isNew ? rootRef.push() : rootRef.child(crop.getId());

        if (isNew) {
            crop.setId(ref.getKey());
            crop.setCreatedAt(LocalDateTime.now());
        }
        crop.setUpdatedAt(LocalDateTime.now());

        FirebaseSync.write(ref, toMap(crop));
        return crop;
    }

    @Override
    public Optional<Crop> findById(String id) {
        if (id == null) return Optional.empty();
        DataSnapshot snapshot = FirebaseSync.read(rootRef.child(id));
        return Optional.ofNullable(fromSnapshot(snapshot));
    }

    @Override
    public List<Crop> findByOwnerUsernameOrderByPlantingDateDesc(String ownerUsername) {
        DataSnapshot snapshot = FirebaseSync.read(rootRef.orderByChild("ownerUsername").equalTo(ownerUsername));
        List<Crop> crops = toList(snapshot);
        crops.sort(Comparator.comparing(Crop::getPlantingDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return crops;
    }

    @Override
    public List<Crop> findByOwnerUsernameAndGrowthStage(String ownerUsername, String growthStage) {
        DataSnapshot snapshot = FirebaseSync.read(rootRef.orderByChild("ownerUsername").equalTo(ownerUsername));
        List<Crop> crops = toList(snapshot);
        crops.removeIf(c -> !Objects.equals(c.getGrowthStage(), growthStage));
        return crops;
    }

    @Override
    public void delete(Crop crop) {
        if (crop == null || crop.getId() == null) return;
        FirebaseSync.remove(rootRef.child(crop.getId()));
    }

    private List<Crop> toList(DataSnapshot snapshot) {
        List<Crop> crops = new ArrayList<>();
        if (snapshot == null || !snapshot.exists()) return crops;
        for (DataSnapshot child : snapshot.getChildren()) {
            Crop crop = fromSnapshot(child);
            if (crop != null) crops.add(crop);
        }
        return crops;
    }

    // Dates are stored as ISO-8601 strings: Firebase's internal object
    // mapper has no java.time support, so automatic POJO (de)serialization
    // would throw the moment a Crop with a LocalDate field was saved.
    private Map<String, Object> toMap(Crop crop) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", crop.getName());
        map.put("variety", crop.getVariety());
        map.put("plantingDate", crop.getPlantingDate() == null ? null : crop.getPlantingDate().toString());
        map.put("growthStage", crop.getGrowthStage());
        map.put("plotLocation", crop.getPlotLocation());
        map.put("notes", crop.getNotes());
        map.put("ownerUsername", crop.getOwnerUsername());
        map.put("createdAt", crop.getCreatedAt() == null ? null : crop.getCreatedAt().toString());
        map.put("updatedAt", crop.getUpdatedAt() == null ? null : crop.getUpdatedAt().toString());
        return map;
    }

    private Crop fromSnapshot(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) return null;
        String plantingDate = snapshot.child("plantingDate").getValue(String.class);
        String createdAt = snapshot.child("createdAt").getValue(String.class);
        String updatedAt = snapshot.child("updatedAt").getValue(String.class);
        return Crop.builder()
                .id(snapshot.getKey())
                .name(snapshot.child("name").getValue(String.class))
                .variety(snapshot.child("variety").getValue(String.class))
                .plantingDate(plantingDate == null ? null : LocalDate.parse(plantingDate))
                .growthStage(snapshot.child("growthStage").getValue(String.class))
                .plotLocation(snapshot.child("plotLocation").getValue(String.class))
                .notes(snapshot.child("notes").getValue(String.class))
                .ownerUsername(snapshot.child("ownerUsername").getValue(String.class))
                .createdAt(createdAt == null ? null : LocalDateTime.parse(createdAt))
                .updatedAt(updatedAt == null ? null : LocalDateTime.parse(updatedAt))
                .build();
    }
}
