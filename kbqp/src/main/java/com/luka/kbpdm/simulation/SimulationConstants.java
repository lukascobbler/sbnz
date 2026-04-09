package com.luka.kbpdm.simulation;

import java.time.Instant;

public final class SimulationConstants {

    public static final int SIMULATED_MINUTES_BETWEEN_SAMPLES = 30;
    public static final long MIN_TICK_MINUTES = 30;
    public static final long MAX_TICK_MINUTES = 900;

    public static final double TELEMETRY_NORMAL_DRIFT_HIGH_MULT = 1.12;

    public static final Instant SIMULATION_EPOCH = Instant.parse("2026-01-01T00:00:00Z");

    private SimulationConstants() {}
}
