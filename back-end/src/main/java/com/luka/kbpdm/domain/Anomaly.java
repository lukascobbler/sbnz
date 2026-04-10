package com.luka.kbpdm.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Anomaly {
    private String machineId;
    private AnomalyType type;
    private String metricKey;
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant detectedAt;
}
