package com.luka.kbpdm.simulation.rules;

import com.luka.kbpdm.api.RuleFiring;
import com.luka.kbpdm.domain.diagnosis.*;
import com.luka.kbpdm.domain.health.*;
import com.luka.kbpdm.domain.machine.ComponentStatus;
import com.luka.kbpdm.domain.machine.Machine;
import com.luka.kbpdm.domain.safety.*;
import com.luka.kbpdm.domain.telemetry.*;
import org.kie.api.event.rule.AfterMatchFiredEvent;

import java.util.*;

public final class RuleMatchModel {

    private RuleMatchModel() {}

    public static List<RuleFiring> sortRuleFiringsDesc(List<RuleFiring> list) {
        list.sort(Comparator.comparing(RuleFiring::getFiredAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    public static String inferMachineIdFromMatch(AfterMatchFiredEvent event) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        for (Object o : event.getMatch().getObjects()) {
            String id = machineIdFromFact(o);
            if (id != null) {
                ids.add(id);
            }
        }
        if (ids.isEmpty()) {
            return null;
        }
        if (ids.size() == 1) {
            return ids.getFirst();
        }
        return String.join(", ", ids);
    }

    private static String machineIdFromFact(Object o) {
        if (o instanceof Machine m) {
            return m.getMachineId();
        }
        if (o instanceof TelemetryReading r) {
            return r.getMachineId();
        }
        if (o instanceof Anomaly a) {
            return a.getMachineId();
        }
        if (o instanceof Intervention i) {
            return i.getMachineId();
        }
        if (o instanceof ComponentStatus c) {
            return c.getMachineId();
        }
        if (o instanceof SafetyCheck s) {
            return s.getMachineId();
        }
        if (o instanceof MachineOverworked u) {
            return u.getMachineId();
        }
        if (o instanceof SafetyResult s) {
            return s.getMachineId();
        }
        if (o instanceof MetricTick t) {
            return t.getMachineId();
        }
        if (o instanceof CurrentMetric c) {
            return c.getMachineId();
        }
        if (o instanceof RecordedAnomaly r) {
            return r.getMachineId();
        }
        if (o instanceof RecordedIntervention r) {
            return r.getMachineId();
        }
        if (o instanceof RecordedMachineOverworked r) {
            return r.getMachineId();
        }
        if (o instanceof RecordedFix r) {
            return r.getMachineId();
        }
        return null;
    }
}
