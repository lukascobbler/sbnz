package com.luka.kbpdm.domain.telemetry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulatedClock {
    private Instant now;
}
