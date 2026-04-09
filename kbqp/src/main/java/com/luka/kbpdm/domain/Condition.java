package com.luka.kbpdm.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Condition {
    private String machineId;
    private ConditionType type;
    private Instant detectedAt;
}

