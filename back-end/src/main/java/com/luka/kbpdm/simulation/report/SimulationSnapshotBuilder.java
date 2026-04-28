package com.luka.kbpdm.simulation.report;

import com.luka.kbpdm.api.*;
import com.luka.kbpdm.domain.diagnosis.Anomaly;
import com.luka.kbpdm.domain.diagnosis.Intervention;
import com.luka.kbpdm.domain.machine.ComponentStatus;
import com.luka.kbpdm.domain.machine.Machine;
import com.luka.kbpdm.domain.safety.SafetyResult;
import com.luka.kbpdm.domain.safety.MachineOverworked;
import com.luka.kbpdm.simulation.drools.WorkingMemoryOps;
import com.luka.kbpdm.simulation.machines.MachineProcessProfile;
import com.luka.kbpdm.simulation.machines.MetricProfile;
import org.kie.api.runtime.KieSession;

import java.time.Instant;
import java.util.*;

public final class SimulationSnapshotBuilder {

    private SimulationSnapshotBuilder() {}

    public static SimulationReport build(
            long tickMinutes,
            Instant simulatedTime,
            KieSession session,
            Map<String, SensorStatus> sensors,
            Map<String, Map<String, MachineWorkload>> workloadByMachineMetric,
            List<MachineProcessProfile> machineProfiles,
            List<List<SensorStatus>> sensorTraceThisTick,
            List<RuleFiring> rulesFromLastCompletedTick
    ) {
        SimulationReport r = new SimulationReport();
        r.setTickMinutes(tickMinutes);
        r.setSimulatedTime(simulatedTime);
        r.setSafetyHaltedMachineIds(new ArrayList<>(WorkingMemoryOps.haltedMachineIds(session)));
        r.setMachineProfiles(toMachineProfileViews(machineProfiles));
        r.setMachineMetricWorkloads(copyWorkloads(workloadByMachineMetric, machineProfiles));

        r.setMachines(WorkingMemoryOps.getFacts(session, Machine.class));
        r.setTelemetry(List.of());
        r.setSensors(new ArrayList<>(sensors.values()));
        if (sensorTraceThisTick.isEmpty()) {
            r.setSensorSnapshotsThisTick(List.of());
        } else {
            r.setSensorSnapshotsThisTick(copySensorTrace(sensorTraceThisTick));
            sensorTraceThisTick.clear();
        }
        Map<String, String> nameByMachineId = machineIdToDisplayName(session);
        r.setAnomalies(sortAnomalies(new ArrayList<>(WorkingMemoryOps.getFacts(session, Anomaly.class)), nameByMachineId));
        r.setInterventions(sortInterventions(new ArrayList<>(WorkingMemoryOps.getFacts(session, Intervention.class)), nameByMachineId));
        r.setComponents(WorkingMemoryOps.getFacts(session, ComponentStatus.class));
        r.setMachineOverworked(WorkingMemoryOps.getFacts(session, MachineOverworked.class));
        r.setSafetyResults(sortSafetyResults(new ArrayList<>(WorkingMemoryOps.getFacts(session, SafetyResult.class)), nameByMachineId));
        r.setRulesFiredThisTick(new ArrayList<>(rulesFromLastCompletedTick));
        return r;
    }

    private static List<MachineProfileView> toMachineProfileViews(List<MachineProcessProfile> profiles) {
        List<MachineProfileView> out = new ArrayList<>(profiles.size());
        for (MachineProcessProfile p : profiles) {
            List<MetricProfileView> metrics = new ArrayList<>();
            for (MetricProfile m : p.metrics()) {
                metrics.add(new MetricProfileView(
                        m.metricKey(),
                        m.displayName(),
                        m.unit(),
                        m.decimals(),
                        m.workloadEnabled(),
                        m.trendEnabled(),
                        m.hasAnomalyThreshold(),
                        m.hasStressThreshold()
                ));
            }
            out.add(new MachineProfileView(p.machineId(), p.displayName(), p.machineType(), metrics));
        }
        return out;
    }

    private static Map<String, Map<String, MachineWorkload>> copyWorkloads(
            Map<String, Map<String, MachineWorkload>> source,
            List<MachineProcessProfile> profiles
    ) {
        Map<String, Map<String, MachineWorkload>> out = new LinkedHashMap<>();
        for (MachineProcessProfile p : profiles) {
            Map<String, MachineWorkload> row = new LinkedHashMap<>();
            Map<String, MachineWorkload> srcRow = source.getOrDefault(p.machineId(), Map.of());
            for (MetricProfile m : p.metrics()) {
                row.put(m.metricKey(), srcRow.getOrDefault(m.metricKey(), MachineWorkload.NORMAL));
            }
            out.put(p.machineId(), row);
        }
        return out;
    }

    private static List<List<SensorStatus>> copySensorTrace(List<List<SensorStatus>> trace) {
        List<List<SensorStatus>> out = new ArrayList<>(trace.size());
        for (List<SensorStatus> snap : trace) {
            List<SensorStatus> row = new ArrayList<>(snap.size());
            for (SensorStatus s : snap) {
                row.add(new SensorStatus(s.getMachineId(), s.getMetric(), s.getValue(), s.getTs()));
            }
            out.add(row);
        }
        return out;
    }

    private static Map<String, String> machineIdToDisplayName(KieSession session) {
        Map<String, String> map = new HashMap<>();
        for (Machine m : WorkingMemoryOps.getFacts(session, Machine.class)) {
            String id = m.getMachineId();
            String label = m.getName() != null && !m.getName().isBlank() ? m.getName() : id;
            map.put(id, label);
        }
        return map;
    }

    private static String sortNameForMachine(String machineId, Map<String, String> nameByMachineId) {
        if (machineId == null) {
            return "";
        }
        return nameByMachineId.getOrDefault(machineId, machineId);
    }

    private static List<Anomaly> sortAnomalies(List<Anomaly> list, Map<String, String> nameByMachineId) {
        list.sort(Comparator
                .comparing((Anomaly a) -> sortNameForMachine(a.getMachineId(), nameByMachineId), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Anomaly::getMachineId, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Anomaly::getDetectedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    private static List<Intervention> sortInterventions(List<Intervention> list, Map<String, String> nameByMachineId) {
        list.sort(Comparator
                .comparing((Intervention i) -> sortNameForMachine(i.getMachineId(), nameByMachineId), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Intervention::getMachineId, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Intervention::getDecidedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    private static List<SafetyResult> sortSafetyResults(List<SafetyResult> list, Map<String, String> nameByMachineId) {
        list.sort(Comparator
                .comparing((SafetyResult s) -> sortNameForMachine(s.getMachineId(), nameByMachineId), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(SafetyResult::getMachineId, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SafetyResult::getEvaluatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }
}
