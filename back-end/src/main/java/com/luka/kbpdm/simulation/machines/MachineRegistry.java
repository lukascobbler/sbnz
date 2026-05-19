package com.luka.kbpdm.simulation.machines;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public final class MachineRegistry {

    private final List<MachineProfile> orderedProfiles;
    private final Map<String, MachineProfile> byId;

    public MachineRegistry() {
        this.orderedProfiles = List.of(plantClimate(), conveyorLine(), cncMill(), packLine());
        Map<String, MachineProfile> map = new LinkedHashMap<>();
        for (MachineProfile p : orderedProfiles) {
            map.put(p.machineId(), p);
        }
        this.byId = Collections.unmodifiableMap(map);
    }

    private static MachineProfile plantClimate() {
        return new MachineProfile(
                "CLM",
                "Plant climate",
                List.of(
                        new MetricProfile("AMBIENT_C", "Ambient temperature", "C", 1, 21.0, 0.14, 29.0, null, true, false),
                        new MetricProfile("HUMIDITY_PCT", "Humidity", "%RH", 1, 52.0, 0.75, 64.0, null, true, false)
                )
        );
    }

    private static MachineProfile conveyorLine() {
        return new MachineProfile(
                "LIN",
                "Conveyor line",
                List.of(
                        new MetricProfile("VIBRATION_RMS", "Vibration", "RMS", 2, 3.35, 0.09, 3.85, 4.4, true, true),
                        new MetricProfile("BELT_SPEED_PCT", "Belt speed", "%", 1, 95.0, 0.95, null, null, true, true)
                )
        );
    }

    private static MachineProfile cncMill() {
        return new MachineProfile(
                "CNC",
                "CNC mill",
                List.of(
                        new MetricProfile("TEMPERATURE_C", "Temperature", "C", 2, 59.0, 0.28, 67.5, 78.0, true, true),
                        new MetricProfile("VIBRATION_RMS", "Vibration", "RMS", 2, 4.65, 0.07, 5.35, 5.9, true, true),
                        new MetricProfile("SPINDLE_LOAD_PCT", "Spindle load", "%", 1, 72.0, 1.05, 84.0, 93.0, true, true)
                )
        );
    }

    private static MachineProfile packLine() {
        return new MachineProfile(
                "PKG",
                "Auto packer",
                List.of(
                        new MetricProfile("CASES_PER_MIN", "Throughput", "cases/min", 1, 118.0, 2.4, null, null, true, true),
                        new MetricProfile("REJECT_PCT", "Reject rate", "%", 2, 1.8, 0.1, 4.5, null, true, true),
                        new MetricProfile("SEAL_TEMP_C", "Seal temperature", "C", 1, 88.0, 0.32, 99.0, 105.0, true, true)
                )
        );
    }

    public List<MachineProfile> profilesInOrder() {
        return orderedProfiles;
    }

    public Optional<MachineProfile> profile(String machineId) {
        return Optional.ofNullable(byId.get(machineId));
    }

    public MachineProfile require(String machineId) {
        MachineProfile p = byId.get(machineId);
        if (p == null) {
            throw new IllegalArgumentException("Unknown machineId: " + machineId);
        }
        return p;
    }

    public boolean hasMachine(String machineId) {
        return machineId != null && byId.containsKey(machineId);
    }
}
