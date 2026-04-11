package com.luka.kbpdm.domain.telemetry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryReading {
    private String machineId;
    private String metric;
    private double value;
    private long ts;
}
