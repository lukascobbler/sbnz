package com.luka.kbpdm.domain.diagnosis;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Intervention {
    private String machineId;
    private PriorityLevel priority;
    private String sourceRule;
    private String recommendation;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant decidedAt;

    public Intervention(String machineId, PriorityLevel priority, String recommendation, Instant decidedAt) {
        this(machineId, priority, null, recommendation, decidedAt);
    }
}
