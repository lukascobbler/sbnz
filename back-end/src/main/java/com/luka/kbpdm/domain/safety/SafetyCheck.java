package com.luka.kbpdm.domain.safety;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SafetyCheck {
    private String machineId;
    private Instant checkedAt;
}
