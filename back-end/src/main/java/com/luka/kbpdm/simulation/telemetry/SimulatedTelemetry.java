package com.luka.kbpdm.simulation.telemetry;

import com.luka.kbpdm.api.MachineWorkload;
import com.luka.kbpdm.api.SensorStatus;
import com.luka.kbpdm.domain.telemetry.TelemetryReading;
import com.luka.kbpdm.simulation.machines.MachineProfile;
import com.luka.kbpdm.simulation.machines.MetricProfile;
import org.kie.api.runtime.KieSession;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class SimulatedTelemetry {

    private final Map<String, Double> generatorState = new ConcurrentHashMap<>();

    public void clearGeneratorState() {
        generatorState.clear();
    }

    public void removeKeysStartingWith(String prefix) {
        generatorState.keySet().removeIf(k -> k.startsWith(prefix));
    }

    public void generateStep(
            KieSession session,
            Map<String, SensorStatus> sensors,
            Set<String> haltedMachineIds,
            Map<String, Map<String, MachineWorkload>> workloadByMachineMetric,
            Instant simulatedTime,
            MachineProfile profile
    ) {
        String machineId = profile.machineId();
        if (haltedMachineIds.contains(machineId)) {
            return;
        }
        Map<String, MachineWorkload> machineWorkloads = workloadByMachineMetric.getOrDefault(machineId, Map.of());
        for (MetricProfile metric : profile.metrics()) {
            generateMetric(session, sensors, simulatedTime, machineId, metric, machineWorkloads);
        }
    }

    public void resetToNominal(
            Map<String, SensorStatus> sensors,
            Instant simulatedTime,
            MachineProfile profile
    ) {
        String machineId = profile.machineId();
        for (MetricProfile metric : profile.metrics()) {
            String key = metricStateKey(machineId, metric.metricKey());
            generatorState.put(key, metric.nominal());
            sensors.put(key, new SensorStatus(machineId, metric.metricKey(), metric.nominal(), simulatedTime));
        }
    }

    public static List<SensorStatus> copySensorsSnapshot(Map<String, SensorStatus> sensors) {
        List<SensorStatus> row = new ArrayList<>(sensors.size());
        for (SensorStatus s : sensors.values()) {
            row.add(new SensorStatus(s.getMachineId(), s.getMetric(), s.getValue(), s.getTs()));
        }
        row.sort(Comparator
                .comparing(SensorStatus::getMachineId, Comparator.nullsFirst(String::compareTo))
                .thenComparing(SensorStatus::getMetric));
        return row;
    }

    private void generateMetric(
            KieSession session,
            Map<String, SensorStatus> sensors,
            Instant simulatedTime,
            String machineId,
            MetricProfile metric,
            Map<String, MachineWorkload> machineWorkloads
    ) {
        MachineWorkload workload = machineWorkloads.getOrDefault(metric.metricKey(), MachineWorkload.NORMAL);
        String key = metricStateKey(machineId, metric.metricKey());
        double prev = generatorState.getOrDefault(key, metric.nominal());
        double next = nextValue(prev, metric, workload);
        generatorState.put(key, next);
        session.insert(new TelemetryReading(machineId, metric.metricKey(), next, simulatedTime.toEpochMilli()));
        sensors.put(key, new SensorStatus(machineId, metric.metricKey(), next, simulatedTime));
    }

    private static String metricStateKey(String machineId, String metricKey) {
        return machineId + ":" + metricKey;
    }

    private static double nextValue(double prev, MetricProfile metric, MachineWorkload workload) {
        double nominal = metric.nominal();
        double creepScale = metric.creepScale();
        double thresholdHint = metric.thresholdHintForGenerator();
        return switch (workload) {
            case OVERWORKED -> {
                double creep = creepScale * 0.26 * (1.0 + ThreadLocalRandom.current().nextDouble() * 0.25);
                double n = prev + creep;
                double floor = Math.min(nominal * 0.88, thresholdHint * 0.75);
                yield Math.max(floor, n);
            }
            case REST -> {
                double pull = (nominal - prev) * 0.24;
                double noise = (ThreadLocalRandom.current().nextDouble() - 0.5) * creepScale * 0.55;
                double n = prev + pull + noise;
                double lo = nominal * 0.92;
                double hi = nominal * 1.08;
                yield Math.min(hi, Math.max(lo, n));
            }
            case NORMAL -> {
                double pull = (nominal - prev) * 0.16;
                double noise = (ThreadLocalRandom.current().nextDouble() - 0.5) * creepScale * 1.1;
                double n = prev + pull + noise;
                double lo = nominal * 0.94;
                double hi = nominal * 1.12;
                yield Math.min(hi, Math.max(lo, n));
            }
        };
    }
}
