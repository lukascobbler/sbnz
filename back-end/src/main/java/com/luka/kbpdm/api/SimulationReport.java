package com.luka.kbpdm.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.luka.kbpdm.domain.diagnosis.Anomaly;
import com.luka.kbpdm.domain.diagnosis.Intervention;
import com.luka.kbpdm.domain.machine.ComponentStatus;
import com.luka.kbpdm.domain.machine.Machine;
import com.luka.kbpdm.domain.safety.SafetyResult;
import com.luka.kbpdm.domain.safety.MachineOverworked;
import com.luka.kbpdm.domain.telemetry.TelemetryReading;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationReport {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant simulatedTime;
    private long tickMinutes;

    private List<String> safetyHaltedMachineIds = new ArrayList<>();

    private List<MachineProfileView> machineProfiles = new ArrayList<>();
    private Map<String, Map<String, MachineWorkload>> machineMetricWorkloads = new LinkedHashMap<>();

    private List<RuleFiring> rulesFiredThisTick = new ArrayList<>();

    private List<Machine> machines = new ArrayList<>();
    private List<TelemetryReading> telemetry = new ArrayList<>();
    private List<SensorStatus> sensors = new ArrayList<>();
    private List<List<SensorStatus>> sensorSnapshotsThisTick = new ArrayList<>();
    private List<Anomaly> anomalies = new ArrayList<>();
    private List<Intervention> interventions = new ArrayList<>();

    private List<ComponentStatus> components = new ArrayList<>();
    private List<MachineOverworked> machineOverworked = new ArrayList<>();
    private List<SafetyResult> safetyResults = new ArrayList<>();
}
