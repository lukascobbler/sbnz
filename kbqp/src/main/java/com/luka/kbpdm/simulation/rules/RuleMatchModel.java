package com.luka.kbpdm.simulation.rules;

import com.luka.kbpdm.api.RuleFiring;
import com.luka.kbpdm.domain.*;
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
            return ids.iterator().next();
        }
        return String.join(", ", ids);
    }

    public static String summarizeMatch(AfterMatchFiredEvent event) {
        List<String> parts = new ArrayList<>();
        for (Object o : event.getMatch().getObjects()) {
            String s = summarizeFact(o);
            if (s != null) {
                parts.add(s);
            }
        }
        if (parts.isEmpty()) {
            return "";
        }
        if (parts.size() > 3) {
            parts = new ArrayList<>(parts.subList(0, 3));
            parts.add("…");
        }
        return String.join(", ", parts);
    }

    private static String summarizeFact(Object o) {
        if (o instanceof Machine m) {
            return "Machine(" + m.getType() + ")";
        }
        if (o instanceof TelemetryReading r) {
            String v = String.format(Locale.ROOT, "%.2f", r.getValue());
            return "Reading(" + r.getMetric() + "=" + v + ")";
        }
        if (o instanceof Anomaly a) {
            if (a.getMetricKey() != null) {
                return "Anomaly(" + a.getType() + ":" + a.getMetricKey() + ")";
            }
            return "Anomaly(" + a.getType() + ")";
        }
        if (o instanceof Intervention i) {
            return "Intervention(" + i.getPriority() + ")";
        }
        if (o instanceof UnsafeReason u) {
            if (u.getDetails() != null && !u.getDetails().isBlank()) {
                return "Unsafe(" + u.getCode() + ":" + u.getDetails() + ")";
            }
            return "Unsafe(" + u.getCode() + ")";
        }
        if (o instanceof SafetyCheck) {
            return "SafetyCheck";
        }
        if (o instanceof SafetyResult s) {
            return s.isSafe() ? "SAFE" : "UNSAFE";
        }
        if (o instanceof TickStatus t) {
            return t.isSustainedStressPresent() ? "Tick(stress)" : "Tick(ok)";
        }
        if (o instanceof MetricTick t) {
            String v = String.format(Locale.ROOT, "%.2f", t.getValue());
            return "MetricTick(" + t.getMetricKey() + "=" + v + ")";
        }
        if (o instanceof MachineHalted) {
            return "HALTED";
        }
        return null;
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
        if (o instanceof UnsafeReason u) {
            return u.getMachineId();
        }
        if (o instanceof SafetyResult s) {
            return s.getMachineId();
        }
        if (o instanceof MachineHalted h) {
            return h.getMachineId();
        }
        if (o instanceof MetricTick t) {
            return t.getMachineId();
        }
        return null;
    }
}
