package com.luka.kbpdm.simulation.machines;

import com.luka.kbpdm.domain.machine.ComponentType;
import com.luka.kbpdm.domain.machine.MachineType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record MachineProcessProfile(
        String machineId,
        String displayName,
        MachineType machineType,
        ComponentType componentType,
        List<MetricProfile> metrics
) {
    public MachineProcessProfile {
        Objects.requireNonNull(machineId, "machineId");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(machineType, "machineType");
        Objects.requireNonNull(componentType, "componentType");
        Objects.requireNonNull(metrics, "metrics");
        if (metrics.isEmpty()) {
            throw new IllegalArgumentException(machineId + ": machine must define at least one metric");
        }
        Map<String, Boolean> seen = new LinkedHashMap<>();
        for (MetricProfile m : metrics) {
            if (seen.put(m.metricKey(), Boolean.TRUE) != null) {
                throw new IllegalArgumentException(machineId + ": duplicate metricKey " + m.metricKey());
            }
        }
        metrics = List.copyOf(metrics);
    }

    public MetricProfile metricOrNull(String metricKey) {
        for (MetricProfile m : metrics) {
            if (m.metricKey().equals(metricKey)) {
                return m;
            }
        }
        return null;
    }

    public boolean sustainedStressBandActive(Map<String, Double> valuesByMetric) {
        if (valuesByMetric == null || valuesByMetric.isEmpty()) {
            return false;
        }
        for (MetricProfile m : metrics) {
            if (!m.hasStressThreshold()) {
                continue;
            }
            Double v = valuesByMetric.get(m.metricKey());
            if (v != null && !Double.isNaN(v) && v >= m.stressThreshold()) {
                return true;
            }
        }
        return false;
    }
}
