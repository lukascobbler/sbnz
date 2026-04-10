package com.luka.kbpdm.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentMetric {
    private String machineId;
    private String metricKey;
    private double value;
    private long ts;
}

