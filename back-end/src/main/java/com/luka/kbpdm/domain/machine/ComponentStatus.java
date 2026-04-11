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
}
