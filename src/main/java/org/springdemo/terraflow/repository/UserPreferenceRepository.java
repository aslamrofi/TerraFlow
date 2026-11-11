package org.springdemo.terraflow.repository;

import org.springdemo.terraflow.model.UserPreference;

import java.util.Optional;

public interface UserPreferenceRepository {
    UserPreference save(UserPreference preference);
    Optional<UserPreference> findByOwnerUsername(String ownerUsername);
}
