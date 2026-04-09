package com.luka.kbpdm.domain;

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
    private Duration horizon;
    private Instant checkedAt;
}

