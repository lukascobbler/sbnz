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

/**
 * Single registry of simulated machines. Drools threshold rules, telemetry, stress bands, WM seeding,
 * and API workload keys are all derived from {@link #profilesInOrder()}.
 * <p>To add a machine: define a {@link MachineProcessProfile} factory below, append it to the list in
 * the constructor, and ensure stress &lt; anomaly and stress above nominal×{@link com.luka.kbpdm.simulation.SimulationConstants#TELEMETRY_NORMAL_DRIFT_HIGH_MULT}.</p>
 */
@Component
public final class MachineProcessRegistry {

    private final List<MachineProcessProfile> orderedProfiles;
    private final Map<String, MachineProcessProfile> byId;

    public MachineProcessRegistry() {
        List<MachineProcessProfile> list = new ArrayList<>();
        list.add(conveyorLine());
        list.add(cncMill());
        this.orderedProfiles = Collections.unmodifiableList(list);
        Map<String, MachineProcessProfile> map = new LinkedHashMap<>();
        for (MachineProcessProfile p : orderedProfiles) {
            map.put(p.machineId(), p);
        }
        this.byId = Collections.unmodifiableMap(map);
    }

    private static MachineProcessProfile conveyorLine() {
        return new MachineProcessProfile(
                "LIN",
                "Line",
                CONVEYOR,
                BEARING,
                Duration.ofDays(30),
                Duration.ofDays(25),
                69.0,
                3.35,
                0.55,
                0.09,
                86.0,
                4.4,
                78.0,
                3.85
        );
    }

    private static MachineProcessProfile cncMill() {
        return new MachineProcessProfile(
                "CNC",
                "CNC",
                CNC,
                MOTOR,
                Duration.ofDays(90),
                Duration.ofDays(10),
                59.0,
                4.65,
                0.28,
                0.07,
                78.0,
                5.9,
                67.5,
                5.35
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
