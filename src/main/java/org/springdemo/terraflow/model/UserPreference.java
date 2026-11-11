package org.springdemo.terraflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

    private String id;

    private String ownerUsername;

    @Builder.Default
    private String language = "en";

    // "C" or "F"
    @Builder.Default
    private String temperatureUnit = "C";

    @Builder.Default
    private boolean emailNotifications = true;
}
