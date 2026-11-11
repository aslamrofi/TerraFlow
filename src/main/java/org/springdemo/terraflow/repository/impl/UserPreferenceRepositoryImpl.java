package org.springdemo.terraflow.repository.impl;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springdemo.terraflow.model.UserPreference;
import org.springdemo.terraflow.repository.UserPreferenceRepository;
import org.springdemo.terraflow.util.FirebaseSync;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserPreferenceRepositoryImpl implements UserPreferenceRepository {

    private static final String NODE = "userPreferences";
    private final DatabaseReference rootRef;

    public UserPreferenceRepositoryImpl(FirebaseDatabase firebaseDatabase) {
        this.rootRef = firebaseDatabase.getReference(NODE);
    }

    @Override
    public UserPreference save(UserPreference preference) {
        // One preference row per user: if one already exists for this
        // username, overwrite it by its existing key instead of creating a
        // second node, mirroring the JPA unique-constraint behavior.
        String id = preference.getId();
        if (id == null || id.isBlank()) {
            id = findByOwnerUsername(preference.getOwnerUsername())
                    .map(UserPreference::getId)
                    .orElse(null);
        }
        DatabaseReference ref = (id == null || id.isBlank()) ? rootRef.push() : rootRef.child(id);
        if (preference.getId() == null || preference.getId().isBlank()) {
            preference.setId(ref.getKey());
        }
        FirebaseSync.write(ref, toMap(preference));
        return preference;
    }

    @Override
    public Optional<UserPreference> findByOwnerUsername(String ownerUsername) {
        if (ownerUsername == null) return Optional.empty();
        DataSnapshot snapshot = FirebaseSync.read(rootRef.orderByChild("ownerUsername").equalTo(ownerUsername));
        if (!snapshot.exists()) return Optional.empty();
        for (DataSnapshot child : snapshot.getChildren()) {
            return Optional.ofNullable(fromSnapshot(child));
        }
        return Optional.empty();
    }

    private Map<String, Object> toMap(UserPreference preference) {
        Map<String, Object> map = new HashMap<>();
        map.put("ownerUsername", preference.getOwnerUsername());
        map.put("language", preference.getLanguage());
        map.put("temperatureUnit", preference.getTemperatureUnit());
        map.put("emailNotifications", preference.isEmailNotifications());
        return map;
    }

    private UserPreference fromSnapshot(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) return null;
        Boolean emailNotifications = snapshot.child("emailNotifications").getValue(Boolean.class);
        return UserPreference.builder()
                .id(snapshot.getKey())
                .ownerUsername(snapshot.child("ownerUsername").getValue(String.class))
                .language(snapshot.child("language").getValue(String.class))
                .temperatureUnit(snapshot.child("temperatureUnit").getValue(String.class))
                .emailNotifications(emailNotifications == null || emailNotifications)
                .build();
    }
}
