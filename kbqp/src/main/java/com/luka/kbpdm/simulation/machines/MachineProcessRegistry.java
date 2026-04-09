package com.luka.kbpdm.simulation.machines;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.luka.kbpdm.domain.ComponentType.BEARING;
import static com.luka.kbpdm.domain.ComponentType.MOTOR;
import static com.luka.kbpdm.domain.MachineType.CNC;
import static com.luka.kbpdm.domain.MachineType.CONVEYOR;

@Component
public final class MachineProcessRegistry {

    private final List<MachineProcessProfile> orderedProfiles;
    private final Map<String, MachineProcessProfile> byId;

    public MachineProcessRegistry() {
        this.orderedProfiles = List.of(conveyorLine(), cncMill());
        Map<String, MachineProcessProfile> map = new LinkedHashMap<>();
        for (MachineProcessProfile p : orderedProfiles) {
            map.put(p.machineId(), p);
        }
        this.byId = Collections.unmodifiableMap(map);
    }

    private static MachineProcessProfile conveyorLine() {
        return new MachineProcessProfile(
                "LIN",
                "Conveyor line",
                CONVEYOR,
                BEARING,
                Duration.ofDays(30),
                Duration.ofDays(25),
                List.of(
                        new MetricProfile("TEMPERATURE_C", "Temperature", "C", 2, 69.0, 0.55, 86.0, 78.0, true, true),
                        new MetricProfile("VIBRATION_RMS", "Vibration", "RMS", 2, 3.35, 0.09, 4.4, 3.85, true, true)
                )
        );
    }

    private static MachineProcessProfile cncMill() {
        return new MachineProcessProfile(
                "CNC",
                "CNC Mill",
                CNC,
                MOTOR,
                Duration.ofDays(90),
                Duration.ofDays(10),
                List.of(
                        new MetricProfile("TEMPERATURE_C", "Temperature", "C", 2, 59.0, 0.28, 78.0, 67.5, true, true),
                        new MetricProfile("VIBRATION_RMS", "Vibration", "RMS", 2, 4.65, 0.07, 5.9, 5.35, true, true)
                )
        );
    }

    public List<MachineProcessProfile> profilesInOrder() {
        return orderedProfiles;
    }

    public List<String> machineIdsInOrder() {
        return orderedProfiles.stream().map(MachineProcessProfile::machineId).toList();
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
