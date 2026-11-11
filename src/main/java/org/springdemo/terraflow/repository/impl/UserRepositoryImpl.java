package org.springdemo.terraflow.repository.impl;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springdemo.terraflow.model.Role;
import org.springdemo.terraflow.model.User;
import org.springdemo.terraflow.repository.UserRepository;
import org.springdemo.terraflow.util.FirebaseSync;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private static final String NODE = "users";
    private final DatabaseReference rootRef;

    public UserRepositoryImpl(FirebaseDatabase firebaseDatabase) {
        this.rootRef = firebaseDatabase.getReference(NODE);
    }

    @Override
    public User save(User user) {
        boolean isNew = user.getId() == null || user.getId().isBlank();
        DatabaseReference ref = isNew ? rootRef.push() : rootRef.child(user.getId());
        if (isNew) {
            user.setId(ref.getKey());
            user.setCreatedAt(LocalDateTime.now());
        }
        FirebaseSync.write(ref, toMap(user));
        return user;
    }

    @Override
    public Optional<User> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(fromSnapshot(FirebaseSync.read(rootRef.child(id))));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        DataSnapshot snapshot = FirebaseSync.read(rootRef.orderByChild("username").equalTo(username));
        if (!snapshot.exists()) return Optional.empty();
        for (DataSnapshot child : snapshot.getChildren()) {
            return Optional.ofNullable(fromSnapshot(child));
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) return false;
        DataSnapshot snapshot = FirebaseSync.read(rootRef.orderByChild("email").equalTo(email));
        return snapshot.exists();
    }

    private Map<String, Object> toMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("username", user.getUsername());
        map.put("password", user.getPassword());
        map.put("fullName", user.getFullName());
        map.put("email", user.getEmail());
        map.put("role", user.getRole() == null ? null : user.getRole().name());
        map.put("enabled", user.isEnabled());
        map.put("createdAt", user.getCreatedAt() == null ? null : user.getCreatedAt().toString());
        return map;
    }

    private User fromSnapshot(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) return null;
        String roleStr = snapshot.child("role").getValue(String.class);
        String createdAt = snapshot.child("createdAt").getValue(String.class);
        Boolean enabled = snapshot.child("enabled").getValue(Boolean.class);
        return User.builder()
                .id(snapshot.getKey())
                .username(snapshot.child("username").getValue(String.class))
                .password(snapshot.child("password").getValue(String.class))
                .fullName(snapshot.child("fullName").getValue(String.class))
                .email(snapshot.child("email").getValue(String.class))
                .role(roleStr == null ? null : Role.valueOf(roleStr))
                .enabled(enabled == null || enabled)
                .createdAt(createdAt == null ? null : LocalDateTime.parse(createdAt))
                .build();
    }
}
