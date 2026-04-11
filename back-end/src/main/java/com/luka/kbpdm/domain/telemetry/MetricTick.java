package com.luka.kbpdm.domain.telemetry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricTick {
    private String machineId;
    private String metricKey;
    private double value;
    private long tickIndex;
    private long ts;
}
