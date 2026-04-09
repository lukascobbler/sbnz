package com.luka.kbpdm.simulation.telemetry;

import com.luka.kbpdm.api.MachineWorkload;
import com.luka.kbpdm.api.SensorStatus;
import com.luka.kbpdm.domain.Machine;
import com.luka.kbpdm.domain.TelemetryMetric;
import com.luka.kbpdm.domain.TelemetryReading;
import com.luka.kbpdm.simulation.SimulationConstants;
import org.kie.api.runtime.KieSession;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class SimulatedTelemetry {

    private final Map<String, Double> generatorState = new ConcurrentHashMap<>();

    public Map<String, Double> generatorState() {
        return generatorState;
    }

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
            Map<String, MachineWorkload> workloadByMachine,
            Instant simulatedTime,
            Machine m
    ) {
        String id = m.getMachineId();
        if (id == null) {
            return;
        }
        if (haltedMachineIds.contains(id)) {
            return;
        }
        if (SimulationConstants.MID_LINE.equals(id)) {
            generateFor(session, sensors, workloadByMachine, simulatedTime, id, TelemetryMetric.TEMPERATURE_C, SimulationConstants.LIN_TEMP_NOMINAL, 0.55, 70.0);
            generateFor(session, sensors, workloadByMachine, simulatedTime, id, TelemetryMetric.VIBRATION_RMS, SimulationConstants.LIN_VIB_NOMINAL, 0.09, 4.0);
        } else if (SimulationConstants.MID_CNC.equals(id)) {
            generateFor(session, sensors, workloadByMachine, simulatedTime, id, TelemetryMetric.TEMPERATURE_C, SimulationConstants.CNC_TEMP_NOMINAL, 0.28, 80.0);
            generateFor(session, sensors, workloadByMachine, simulatedTime, id, TelemetryMetric.VIBRATION_RMS, SimulationConstants.CNC_VIB_NOMINAL, 0.07, 5.0);
        } else {
            generateFor(session, sensors, workloadByMachine, simulatedTime, id, TelemetryMetric.TEMPERATURE_C, 55.0, 0.22, 75.0);
            generateFor(session, sensors, workloadByMachine, simulatedTime, id, TelemetryMetric.VIBRATION_RMS, 3.2, 0.06, 4.5);
        }
    }

    public void resetToNominal(Map<String, SensorStatus> sensors, Instant simulatedTime, String machineId) {
        if (SimulationConstants.MID_LINE.equals(machineId)) {
            putPair(sensors, simulatedTime, machineId, SimulationConstants.LIN_TEMP_NOMINAL, SimulationConstants.LIN_VIB_NOMINAL);
            return;
        }
        if (SimulationConstants.MID_CNC.equals(machineId)) {
            putPair(sensors, simulatedTime, machineId, SimulationConstants.CNC_TEMP_NOMINAL, SimulationConstants.CNC_VIB_NOMINAL);
        }
    }

    private void putPair(
            Map<String, SensorStatus> sensors,
            Instant simulatedTime,
            String machineId,
            double temp,
            double vib
    ) {
        String tk = machineId + ":" + TelemetryMetric.TEMPERATURE_C.name();
        String vk = machineId + ":" + TelemetryMetric.VIBRATION_RMS.name();
        generatorState.put(tk, temp);
        generatorState.put(vk, vib);
        sensors.put(tk, new SensorStatus(machineId, TelemetryMetric.TEMPERATURE_C, temp, simulatedTime));
        sensors.put(vk, new SensorStatus(machineId, TelemetryMetric.VIBRATION_RMS, vib, simulatedTime));
    }

    public static List<SensorStatus> copySensorsSnapshot(Map<String, SensorStatus> sensors) {
        List<SensorStatus> row = new ArrayList<>(sensors.size());
        for (SensorStatus s : sensors.values()) {
            row.add(new SensorStatus(s.getMachineId(), s.getMetric(), s.getValue(), s.getTs()));
        }
        row.sort(Comparator
                .comparing(SensorStatus::getMachineId, Comparator.nullsFirst(String::compareTo))
                .thenComparing(s -> s.getMetric().name()));
        return row;
    }

    private void generateFor(
            KieSession session,
            Map<String, SensorStatus> sensors,
            Map<String, MachineWorkload> workloadByMachine,
            Instant simulatedTime,
            String machineId,
            TelemetryMetric metric,
            double nominal,
            double creepScale,
            double thresholdHint
    ) {
        MachineWorkload w = workloadByMachine.getOrDefault(machineId, MachineWorkload.NORMAL);
        String key = machineId + ":" + metric.name();
        double prev = generatorState.getOrDefault(key, nominal);

        double next = switch (w) {
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

        generatorState.put(key, next);
        TelemetryReading r = new TelemetryReading(machineId, metric, next, simulatedTime.toEpochMilli());
        session.insert(r);
        sensors.put(key, new SensorStatus(machineId, metric, next, simulatedTime));
    }
}
