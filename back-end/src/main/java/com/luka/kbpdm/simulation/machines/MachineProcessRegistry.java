package com.luka.kbpdm.simulation.machines;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.luka.kbpdm.domain.machine.ComponentType.BEARING;
import static com.luka.kbpdm.domain.machine.ComponentType.ENV_SENSOR;
import static com.luka.kbpdm.domain.machine.ComponentType.MOTOR;
import static com.luka.kbpdm.domain.machine.ComponentType.SEALING;
import static com.luka.kbpdm.domain.machine.MachineType.CNC;
import static com.luka.kbpdm.domain.machine.MachineType.CLIMATE;
import static com.luka.kbpdm.domain.machine.MachineType.CONVEYOR;
import static com.luka.kbpdm.domain.machine.MachineType.PACK_LINE;

@Component
public final class MachineProcessRegistry {

    private final List<MachineProcessProfile> orderedProfiles;
    private final Map<String, MachineProcessProfile> byId;

    public MachineProcessRegistry() {
        this.orderedProfiles = List.of(plantClimate(), conveyorLine(), cncMill(), packLine());
        Map<String, MachineProcessProfile> map = new LinkedHashMap<>();
        for (MachineProcessProfile p : orderedProfiles) {
            map.put(p.machineId(), p);
        }
        this.byId = Collections.unmodifiableMap(map);
    }

    private static MachineProcessProfile plantClimate() {
        return new MachineProcessProfile(
                "CLM",
                "Plant climate",
                CLIMATE,
                ENV_SENSOR,
                List.of(
                        new MetricProfile("AMBIENT_C", "Ambient temperature", "C", 1, 21.0, 0.14, 29.0, null, true, false),
                        new MetricProfile("HUMIDITY_PCT", "Humidity", "%RH", 1, 52.0, 0.75, 64.0, null, true, false)
                )
        );
    }

    private static MachineProcessProfile conveyorLine() {
        return new MachineProcessProfile(
                "LIN",
                "Conveyor line",
                CONVEYOR,
                BEARING,
                List.of(
                        new MetricProfile("VIBRATION_RMS", "Vibration", "RMS", 2, 3.35, 0.09, 3.85, 4.4, true, true),
                        new MetricProfile("BELT_SPEED_PCT", "Belt speed", "%", 1, 95.0, 0.95, null, null, true, true)
                )
        );
    }

    private static MachineProcessProfile cncMill() {
        return new MachineProcessProfile(
                "CNC",
                "CNC mill",
                CNC,
                MOTOR,
                List.of(
                        new MetricProfile("TEMPERATURE_C", "Temperature", "C", 2, 59.0, 0.28, 67.5, 78.0, true, true),
                        new MetricProfile("VIBRATION_RMS", "Vibration", "RMS", 2, 4.65, 0.07, 5.35, 5.9, true, true),
                        new MetricProfile("SPINDLE_LOAD_PCT", "Spindle load", "%", 1, 72.0, 1.05, 84.0, 93.0, true, true)
                )
        );
    }

    private static MachineProcessProfile packLine() {
        return new MachineProcessProfile(
                "PKG",
                "Auto packer",
                PACK_LINE,
                SEALING,
                List.of(
                        new MetricProfile("CASES_PER_MIN", "Throughput", "cases/min", 1, 118.0, 2.4, null, null, true, true),
                        new MetricProfile("REJECT_PCT", "Reject rate", "%", 2, 1.8, 0.1, 4.5, null, true, true),
                        new MetricProfile("SEAL_TEMP_C", "Seal temperature", "C", 1, 88.0, 0.32, 99.0, 105.0, true, true)
                )
        );
    }

    public List<MachineProcessProfile> profilesInOrder() {
        return orderedProfiles;
    }

    public Optional<MachineProcessProfile> profile(String machineId) {
        return Optional.ofNullable(byId.get(machineId));
    }

    public MachineProcessProfile require(String machineId) {
        MachineProcessProfile p = byId.get(machineId);
        if (p == null) {
            throw new IllegalArgumentException("Unknown machineId: " + machineId);
        }
        return p;
    }

    public boolean hasMachine(String machineId) {
        return machineId != null && byId.containsKey(machineId);
    }
}
