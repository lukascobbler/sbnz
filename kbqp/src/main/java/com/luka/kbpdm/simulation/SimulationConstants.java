package com.luka.kbpdm.simulation;

import java.time.Instant;

public final class SimulationConstants {

    public static final String MID_LINE = "LIN";
    public static final String MID_CNC = "CNC";

    public static final int SIMULATED_MINUTES_BETWEEN_SAMPLES = 30;
    public static final long MIN_TICK_MINUTES = 30;
    public static final long MAX_TICK_MINUTES = 900;

    public static final double LIN_TEMP_NOMINAL = 69.0;
    public static final double LIN_VIB_NOMINAL = 3.35;
    public static final double CNC_TEMP_NOMINAL = 59.0;
    public static final double CNC_VIB_NOMINAL = 4.65;

    public static final Instant SIMULATION_EPOCH = Instant.parse("2026-01-01T00:00:00Z");

    private SimulationConstants() {}
}
