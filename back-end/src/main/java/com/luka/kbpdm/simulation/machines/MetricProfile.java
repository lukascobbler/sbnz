package com.luka.kbpdm.simulation.machines;

import java.util.Objects;

import static com.luka.kbpdm.simulation.SimulationConstants.TELEMETRY_NORMAL_DRIFT_HIGH_MULT;

public record MetricProfile(
        String metricKey,
        String displayName,
        String unit,
        int decimals,
        double nominal,
        double creepScale,
        Double anomalyThreshold,
        Double stressThreshold,
        boolean trendEnabled,
        boolean workloadEnabled
) {
    public MetricProfile {
        Objects.requireNonNull(metricKey, "metricKey");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(unit, "unit");
        if (decimals < 0 || decimals > 8) {
            throw new IllegalArgumentException("decimals out of range: " + decimals);
        }
        if (anomalyThreshold != null && stressThreshold != null && anomalyThreshold <= stressThreshold) {
            throw new IllegalArgumentException(metricKey + ": anomaly threshold must be > stress threshold");
        }
        if (stressThreshold != null) {
            double normalHi = nominal * TELEMETRY_NORMAL_DRIFT_HIGH_MULT;
            if (stressThreshold <= normalHi) {
                throw new IllegalArgumentException(metricKey + ": stress threshold must exceed NORMAL upper envelope");
            }
        }
    }

    public boolean hasAnomalyThreshold() {
        return anomalyThreshold != null;
    }

    public boolean hasStressThreshold() {
        return stressThreshold != null;
    }

    public double thresholdHintForGenerator() {
        if (anomalyThreshold != null) {
            return anomalyThreshold;
        }
        if (stressThreshold != null) {
            return stressThreshold;
        }
        return nominal * 1.35;
    }
}
