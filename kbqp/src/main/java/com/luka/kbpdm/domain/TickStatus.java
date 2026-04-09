package com.luka.kbpdm.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TickStatus {
    private String machineId;
    private boolean bearingFailurePresent;
    private double temperatureC;
    private double vibrationRms;
    private long tickIndex;
    private long ts;
}

