package com.luka.kbpdm.simulation.drools;

import com.luka.kbpdm.domain.diagnosis.*;
import com.luka.kbpdm.domain.health.*;
import com.luka.kbpdm.domain.safety.*;
import com.luka.kbpdm.domain.telemetry.*;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import java.util.*;

public final class WorkingMemoryOps {

    private WorkingMemoryOps() {}

    public static <T> List<T> getFacts(KieSession session, Class<T> type) {
        List<T> out = new ArrayList<>();
        for (Object o : session.getObjects(new ClassObjectFilter(type))) {
            out.add(type.cast(o));
        }
        return out;
    }

    /** Machines in an operator-halt state (stress halt inserts {@code UnsafeReason} with this code). */
    public static final String OPERATOR_HALT_CODE = "OPERATOR_HALT";

    public static Set<String> haltedMachineIds(KieSession session) {
        Set<String> set = new HashSet<>();
        for (UnsafeReason u : getFacts(session, UnsafeReason.class)) {
            if (u.getMachineId() != null && OPERATOR_HALT_CODE.equals(u.getCode())) {
                set.add(u.getMachineId());
            }
        }
        return set;
    }

    public static void deleteFactsOfType(KieSession session, Class<?> type) {
        List<Object> copy = new ArrayList<>();
        for (Object o : session.getObjects(new ClassObjectFilter(type))) {
            copy.add(o);
        }
        for (Object o : copy) {
            FactHandle fh = session.getFactHandle(o);
            if (fh != null) {
                session.delete(fh);
            }
        }
    }

    /**
     * Removes {@link UnsafeReason} facts that are re-derived each evaluation step. Preserves
     * {@value #OPERATOR_HALT_CODE}, which must survive across ticks (same role the former {@code MachineHalted} fact had).
     */
    public static void deleteTransientUnsafeReasons(KieSession session) {
        for (Object o : new ArrayList<>(session.getObjects(new ClassObjectFilter(UnsafeReason.class)))) {
            UnsafeReason u = (UnsafeReason) o;
            if (OPERATOR_HALT_CODE.equals(u.getCode())) {
                continue;
            }
            FactHandle fh = session.getFactHandle(o);
            if (fh != null) {
                session.delete(fh);
            }
        }
    }

    public static void deleteFactsForMachine(KieSession session, Class<?> type, String machineId) {
        for (Object o : new ArrayList<>(session.getObjects(new ClassObjectFilter(type)))) {
            if (machineId.equals(extractMachineId(o))) {
                FactHandle fh = session.getFactHandle(o);
                if (fh != null) {
                    session.delete(fh);
                }
            }
        }
    }

    public static String extractMachineId(Object o) {
        if (o instanceof Anomaly a) {
            return a.getMachineId();
        }
        if (o instanceof Intervention i) {
            return i.getMachineId();
        }
        if (o instanceof TelemetryReading r) {
            return r.getMachineId();
        }
        if (o instanceof CurrentMetric r) {
            return r.getMachineId();
        }
        if (o instanceof TickStatus t) {
            return t.getMachineId();
        }
        if (o instanceof MetricTick t) {
            return t.getMachineId();
        }
        if (o instanceof UnsafeReason u) {
            return u.getMachineId();
        }
        if (o instanceof SafetyResult s) {
            return s.getMachineId();
        }
        if (o instanceof SafetyCheck s) {
            return s.getMachineId();
        }
        if (o instanceof RecordedAnomaly r) {
            return r.getMachineId();
        }
        if (o instanceof RecordedIntervention r) {
            return r.getMachineId();
        }
        if (o instanceof RecordedUnsafeReason r) {
            return r.getMachineId();
        }
        if (o instanceof RecordedFix r) {
            return r.getMachineId();
        }
        return null;
    }

    public static void pruneOldTelemetryAndTicks(KieSession session, long cutoffEpochMillis) {
        List<Object> toDelete = new ArrayList<>();
        for (Object o : session.getObjects(new ClassObjectFilter(TelemetryReading.class))) {
            TelemetryReading r = (TelemetryReading) o;
            if (r.getTs() < cutoffEpochMillis) {
                toDelete.add(o);
            }
        }
        for (Object o : session.getObjects(new ClassObjectFilter(TickStatus.class))) {
            TickStatus t = (TickStatus) o;
            if (t.getTs() < cutoffEpochMillis) {
                toDelete.add(o);
            }
        }
        for (Object o : session.getObjects(new ClassObjectFilter(MetricTick.class))) {
            MetricTick t = (MetricTick) o;
            if (t.getTs() < cutoffEpochMillis) {
                toDelete.add(o);
            }
        }
        for (Object o : toDelete) {
            FactHandle fh = session.getFactHandle(o);
            if (fh != null) {
                session.delete(fh);
            }
        }
    }
}
