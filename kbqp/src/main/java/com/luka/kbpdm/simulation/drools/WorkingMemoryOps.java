package com.luka.kbpdm.simulation.drools;

import com.luka.kbpdm.domain.*;
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

    public static Set<String> haltedMachineIds(KieSession session) {
        Set<String> set = new HashSet<>();
        for (MachineHalted h : getFacts(session, MachineHalted.class)) {
            if (h.getMachineId() != null) {
                set.add(h.getMachineId());
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
        if (o instanceof TickStatus t) {
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
        if (o instanceof MachineHalted h) {
            return h.getMachineId();
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
        for (Object o : toDelete) {
            FactHandle fh = session.getFactHandle(o);
            if (fh != null) {
                session.delete(fh);
            }
        }
    }
}
