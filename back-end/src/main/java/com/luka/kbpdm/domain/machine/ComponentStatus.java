package com.luka.kbpdm.domain.machine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentStatus {
    private String machineId;
    private ComponentType component;

    private Instant lastServicedAt;
    private Duration serviceInterval;

    /**
     * Whether simulated {@code now} is still strictly before the next service due instant
     * ({@code lastServicedAt + serviceInterval}). Used by Drools queries without {@code eval}.
     */
    public boolean isWithinServiceInterval(Instant now) {
        if (lastServicedAt == null || serviceInterval == null || now == null) {
            return false;
        }
        return lastServicedAt.plus(serviceInterval).isAfter(now);
    }
}
