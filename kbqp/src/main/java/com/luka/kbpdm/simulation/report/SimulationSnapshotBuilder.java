package com.luka.kbpdm.simulation.report;

import com.luka.kbpdm.api.MachineWorkload;
import com.luka.kbpdm.api.RuleFiring;
import com.luka.kbpdm.api.SensorStatus;
import com.luka.kbpdm.api.SimulationReport;
import com.luka.kbpdm.domain.*;
import com.luka.kbpdm.simulation.drools.WorkingMemoryOps;
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
            Map<String, MachineWorkload> workloadByMachine,
            String midLine,
            String midCnc,
            List<List<SensorStatus>> sensorTraceThisTick,
            List<RuleFiring> rulesFromLastCompletedTick
    ) {
        SimulationReport r = new SimulationReport();
        r.setTickMinutes(tickMinutes);
        r.setSimulatedTime(simulatedTime);
        r.setSafetyHaltedMachineIds(new ArrayList<>(WorkingMemoryOps.haltedMachineIds(session)));

        Map<String, MachineWorkload> wl = new LinkedHashMap<>();
        wl.put(midLine, workloadByMachine.getOrDefault(midLine, MachineWorkload.NORMAL));
        wl.put(midCnc, workloadByMachine.getOrDefault(midCnc, MachineWorkload.NORMAL));
        r.setMachineWorkloads(wl);

        r.setMachines(WorkingMemoryOps.getFacts(session, Machine.class));
        r.setTelemetry(List.of());
        r.setSensors(new ArrayList<>(sensors.values()));
        if (sensorTraceThisTick.isEmpty()) {
            r.setSensorSnapshotsThisTick(List.of());
        } else {
            r.setSensorSnapshotsThisTick(copySensorTrace(sensorTraceThisTick));
            sensorTraceThisTick.clear();
        }
        r.setConditions(WorkingMemoryOps.getFacts(session, Condition.class));
        Map<String, String> nameByMachineId = machineIdToDisplayName(session);
        r.setAnomalies(sortAnomalies(new ArrayList<>(WorkingMemoryOps.getFacts(session, Anomaly.class)), nameByMachineId));
        r.setInterventions(sortInterventions(new ArrayList<>(WorkingMemoryOps.getFacts(session, Intervention.class)), nameByMachineId));
        r.setComponents(WorkingMemoryOps.getFacts(session, ComponentStatus.class));
        r.setUnsafeReasons(WorkingMemoryOps.getFacts(session, UnsafeReason.class));
        r.setSafetyResults(sortSafetyResults(new ArrayList<>(WorkingMemoryOps.getFacts(session, SafetyResult.class)), nameByMachineId));
        r.setRulesFiredThisTick(new ArrayList<>(rulesFromLastCompletedTick));
        return r;
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
