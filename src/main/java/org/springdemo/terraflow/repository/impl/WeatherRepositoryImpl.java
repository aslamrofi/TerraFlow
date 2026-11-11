package org.springdemo.terraflow.repository.impl;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springdemo.terraflow.model.WeatherData;
import org.springdemo.terraflow.repository.WeatherRepository;
import org.springdemo.terraflow.util.FirebaseSync;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class WeatherRepositoryImpl implements WeatherRepository {

    private static final String NODE = "weatherData";
    private final DatabaseReference rootRef;

    public WeatherRepositoryImpl(FirebaseDatabase firebaseDatabase) {
        this.rootRef = firebaseDatabase.getReference(NODE);
    }

    @Override
    public WeatherData save(WeatherData data) {
        boolean isNew = data.getId() == null || data.getId().isBlank();
        DatabaseReference ref = isNew ? rootRef.push() : rootRef.child(data.getId());
        if (isNew) {
            data.setId(ref.getKey());
        }
        FirebaseSync.write(ref, toMap(data));
        return data;
    }

    @Override
    public Optional<WeatherData> findTopByOrderByRecordedAtDesc() {
        DataSnapshot snapshot = FirebaseSync.read(rootRef.orderByChild("recordedAt").limitToLast(1));
        if (!snapshot.exists()) return Optional.empty();
        for (DataSnapshot child : snapshot.getChildren()) {
            return Optional.ofNullable(fromSnapshot(child));
        }
        return Optional.empty();
    }

    private Map<String, Object> toMap(WeatherData data) {
        Map<String, Object> map = new HashMap<>();
        map.put("location", data.getLocation());
        map.put("temperature", data.getTemperature());
        map.put("condition", data.getCondition());
        map.put("humidity", data.getHumidity());
        map.put("windSpeed", data.getWindSpeed());
        map.put("rainfall", data.getRainfall());
        map.put("recordedAt", data.getRecordedAt() == null ? null : data.getRecordedAt().toString());
        return map;
    }

    private WeatherData fromSnapshot(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) return null;
        String recordedAt = snapshot.child("recordedAt").getValue(String.class);
        return WeatherData.builder()
                .id(snapshot.getKey())
                .location(snapshot.child("location").getValue(String.class))
                .temperature(snapshot.child("temperature").getValue(Double.class))
                .condition(snapshot.child("condition").getValue(String.class))
                .humidity(snapshot.child("humidity").getValue(Integer.class))
                .windSpeed(snapshot.child("windSpeed").getValue(Double.class))
                .rainfall(snapshot.child("rainfall").getValue(Double.class))
                .recordedAt(recordedAt == null ? null : LocalDateTime.parse(recordedAt))
                .build();
    }
}
