package com.luka.kbpdm.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricProfileView {
    private String metricKey;
    private String displayName;
    private String unit;
    private int decimals;
    private boolean workloadEnabled;
    private boolean trendEnabled;
    private boolean anomalyEnabled;
    private boolean stressEnabled;
}
