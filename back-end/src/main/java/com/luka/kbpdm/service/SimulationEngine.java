package com.luka.kbpdm.service;

import com.luka.kbpdm.api.MachineHealthReport;
import com.luka.kbpdm.api.MachineWorkload;
import com.luka.kbpdm.api.RuleFiring;
import com.luka.kbpdm.api.SensorStatus;
import com.luka.kbpdm.api.SimulationReport;
import com.luka.kbpdm.domain.diagnosis.*;
import com.luka.kbpdm.domain.health.*;
import com.luka.kbpdm.domain.machine.*;
import com.luka.kbpdm.domain.safety.*;
import com.luka.kbpdm.domain.telemetry.*;
import com.luka.kbpdm.simulation.drools.WorkingMemoryOps;
import com.luka.kbpdm.simulation.machines.MachineProcessProfile;
import com.luka.kbpdm.simulation.report.SimulationSnapshotBuilder;
import com.luka.kbpdm.simulation.rules.RuleMatchModel;
import com.luka.kbpdm.simulation.telemetry.SimulatedTelemetry;
import com.luka.kbpdm.simulation.machines.MachineProcessRegistry;
import com.luka.kbpdm.simulation.machines.MetricProfile;
import lombok.Getter;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.event.rule.DefaultRuleRuntimeEventListener;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.api.time.SessionPseudoClock;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static com.luka.kbpdm.simulation.SimulationConstants.*;

@Service
public class SimulationEngine implements DisposableBean {

    private final KieContainer kieContainer;
    private final MachineProcessRegistry machineRegistry;
    private final SimulatedTelemetry telemetry = new SimulatedTelemetry();

    @Getter
    private volatile long tickMinutes;

    private volatile KieSession session;
    private volatile SessionPseudoClock clock;
    private volatile Instant simulatedTime;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private final Map<String, SensorStatus> sensors = new HashMap<>();

    private final List<RuleFiring> rulesCollectedThisTick = new ArrayList<>();
    private volatile List<RuleFiring> rulesFromLastCompletedTick = List.of();

    private final Map<String, Map<String, MachineWorkload>> workloadByMachineMetric = new LinkedHashMap<>();
    private final Map<String, Long> tickIndexByMachine = new HashMap<>();
    private final List<List<SensorStatus>> sensorTraceThisTick = new ArrayList<>();

    public SimulationEngine(
            KieContainer kieContainer,
            MachineProcessRegistry machineRegistry,
            @Value("${simulation.tickMinutes:30}") long tickMinutes
    ) {
        this.kieContainer = kieContainer;
        this.machineRegistry = machineRegistry;
        this.tickMinutes = clampTickMinutes(tickMinutes);
        reset();
    }

    public synchronized void reset() {
        if (session != null) {
            try {
                session.dispose();
            } catch (Exception ignored) {
            }
        }

        this.session = kieContainer.newKieSession();
        this.clock = session.getSessionClock();
        this.simulatedTime = SIMULATION_EPOCH;
        this.workloadByMachineMetric.clear();
        this.tickIndexByMachine.clear();
        for (MachineProcessProfile profile : machineRegistry.profilesInOrder()) {
            Map<String, MachineWorkload> perMetric = new LinkedHashMap<>();
            for (MetricProfile metric : profile.metrics()) {
                perMetric.put(metric.metricKey(), MachineWorkload.NORMAL);
            }
            workloadByMachineMetric.put(profile.machineId(), perMetric);
        }

        this.sensors.clear();
        telemetry.clearGeneratorState();
        this.rulesFromLastCompletedTick = List.of();

        session.addEventListener(new DefaultAgendaEventListener() {
            @Override
            public void afterMatchFired(org.kie.api.event.rule.AfterMatchFiredEvent event) {
                synchronized (rulesCollectedThisTick) {
                    String name = event.getMatch().getRule().getName();
                    Instant at = simulatedTime;
                    String machineId = RuleMatchModel.inferMachineIdFromMatch(event);
                    rulesCollectedThisTick.add(new RuleFiring(name, machineId, at));
                }
            }
        });

        session.addEventListener(new DefaultRuleRuntimeEventListener() {
            @Override
            public void objectInserted(ObjectInsertedEvent e) {
                Object o = e.getObject();
                if (o instanceof RecordedAnomaly
                        || o instanceof RecordedIntervention
                        || o instanceof RecordedUnsafeReason
                        || o instanceof RecordedFix) {
                    return;
                }
                if (o instanceof Anomaly a) {
                    String typeStr = a.getType() == null ? "" : a.getType().name();
                    Instant at = a.getDetectedAt() != null ? a.getDetectedAt() : simulatedTime;
                    session.insert(new RecordedAnomaly(a.getMachineId(), typeStr, a.getDescription(), at));
                } else if (o instanceof Intervention i) {
                    String pri = i.getPriority() == null ? "" : i.getPriority().name();
                    Instant at = i.getDecidedAt() != null ? i.getDecidedAt() : simulatedTime;
                    session.insert(new RecordedIntervention(i.getMachineId(), pri, i.getRecommendation(), at));
                } else if (o instanceof UnsafeReason u) {
                    session.insert(new RecordedUnsafeReason(
                            u.getMachineId(), u.getCode(), u.getDetails(), simulatedTime));
                }
            }
        });

        seedFacts();
        rulesCollectedThisTick.clear();
        session.fireAllRules();
        finalizeTickReporting();
        broadcastSnapshot();
    }

    public synchronized void setTickMinutes(long tickMinutes) {
        this.tickMinutes = clampTickMinutes(tickMinutes);
        broadcastSnapshot();
    }

    public synchronized MachineHealthReport machineHealthReport(String machineId) {
        if (session == null || machineId == null || machineId.isBlank()) {
            return null;
        }
        String id = machineId.trim();
        if (!machineRegistry.hasMachine(id)) {
            return null;
        }
        QueryResults results = session.getQueryResults("MachineHealth", id);
        if (results == null || results.size() == 0) {
            return null;
        }
        QueryResultsRow row = results.iterator().next();
        int a = queryCount(row, "$a");
        int iv = queryCount(row, "$i");
        int u = queryCount(row, "$u");
        int f = queryCount(row, "$f");
        int hp = queryCount(row, "$health");
        MachineHealthReport r = new MachineHealthReport();
        r.setMachineId(id);
        r.setHealthPercent(hp);
        r.setAnomalyCount(a);
        r.setInterventionCount(iv);
        r.setUnsafeReasonCount(u);
        r.setFixCount(f);
        appendRecordedHistories(session, id, r);
        return r;
    }

    private static int queryCount(QueryResultsRow row, String decl) {
        Object o = row.get(decl);
        if (o instanceof Number n) {
            return n.intValue();
        }
        return 0;
    }

    private static void appendRecordedHistories(KieSession session, String id, MachineHealthReport r) {
        Comparator<Instant> byTime = Comparator.nullsFirst(Comparator.naturalOrder());
        WorkingMemoryOps.getFacts(session, RecordedAnomaly.class).stream()
                .filter(x -> id.equals(x.getMachineId()))
                .sorted(Comparator.comparing(RecordedAnomaly::getRecordedAt, byTime).reversed())
                .forEach(x -> r.getAnomalyHistory()
                        .add(new MachineHealthReport.AnomalyHistoryLine(
                                x.getTypeName(), x.getDescription(), instantToString(x.getRecordedAt()))));
        WorkingMemoryOps.getFacts(session, RecordedIntervention.class).stream()
                .filter(x -> id.equals(x.getMachineId()))
                .sorted(Comparator.comparing(RecordedIntervention::getRecordedAt, byTime).reversed())
                .forEach(x -> r.getInterventionHistory()
                        .add(new MachineHealthReport.InterventionHistoryLine(
                                x.getPriority(), x.getRecommendation(), instantToString(x.getRecordedAt()))));
        WorkingMemoryOps.getFacts(session, RecordedUnsafeReason.class).stream()
                .filter(x -> id.equals(x.getMachineId()))
                .sorted(Comparator.comparing(RecordedUnsafeReason::getRecordedAt, byTime).reversed())
                .forEach(x -> r.getUnsafeReasonHistory()
                        .add(new MachineHealthReport.UnsafeReasonHistoryLine(
                                x.getCode(), x.getDetails(), instantToString(x.getRecordedAt()))));
        WorkingMemoryOps.getFacts(session, RecordedFix.class).stream()
                .filter(x -> id.equals(x.getMachineId()))
                .sorted(Comparator.comparing(RecordedFix::getRecordedAt, byTime).reversed())
                .forEach(x -> r.getFixHistory()
                        .add(new MachineHealthReport.FixHistoryLine(instantToString(x.getRecordedAt()))));
    }

    private static String instantToString(Instant i) {
        return i == null ? "" : i.toString();
    }

    public synchronized void setMachineWorkload(String machineId, MachineWorkload workload, String metricKey) {
        if (session == null || workload == null || metricKey == null || metricKey.isBlank()) {
            return;
        }
        if (!machineRegistry.hasMachine(machineId)) {
            return;
        }
        MachineProcessProfile profile = machineRegistry.require(machineId);
        MetricProfile metric = profile.metricOrNull(metricKey);
        if (metric == null || !metric.workloadEnabled()) {
            return;
        }
        Map<String, MachineWorkload> perMetric = workloadByMachineMetric.computeIfAbsent(machineId, k -> new LinkedHashMap<>());
        perMetric.put(metricKey, workload);
        broadcastSnapshot();
    }

    private static long clampTickMinutes(long tickMinutes) {
        long t = Math.max(MIN_TICK_MINUTES, Math.min(MAX_TICK_MINUTES, tickMinutes));
        t = (t / SIMULATED_MINUTES_BETWEEN_SAMPLES) * SIMULATED_MINUTES_BETWEEN_SAMPLES;
        return t;
    }

    public synchronized void step(long stepMinutes) {
        long m = stepMinutes <= 0 ? tickMinutes : stepMinutes;
        tickInternal(Math.max(MIN_TICK_MINUTES, m), MIN_TICK_MINUTES);
        broadcastSnapshot();
    }

    public synchronized void operatorSafetyFix(String machineId) {
        if (session == null || !machineRegistry.hasMachine(machineId)) {
            return;
        }
        MachineProcessProfile profile = machineRegistry.require(machineId);
        boolean known = false;
        for (Machine m : WorkingMemoryOps.getFacts(session, Machine.class)) {
            if (machineId.equals(m.getMachineId())) {
                known = true;
                break;
            }
        }
        if (!known) {
            return;
        }
        session.insert(new RecordedFix(machineId, simulatedTime));
        WorkingMemoryOps.deleteFactsForMachine(session, Anomaly.class, machineId);
        WorkingMemoryOps.deleteFactsForMachine(session, Intervention.class, machineId);
        WorkingMemoryOps.deleteFactsForMachine(session, UnsafeReason.class, machineId);
        WorkingMemoryOps.deleteFactsForMachine(session, SafetyResult.class, machineId);
        WorkingMemoryOps.deleteFactsForMachine(session, SafetyCheck.class, machineId);
        WorkingMemoryOps.deleteFactsForMachine(session, TickStatus.class, machineId);
        WorkingMemoryOps.deleteFactsForMachine(session, MetricTick.class, machineId);
        WorkingMemoryOps.deleteFactsForMachine(session, TelemetryReading.class, machineId);
        telemetry.removeKeysStartingWith(machineId + ":");
        tickIndexByMachine.remove(machineId);

        telemetry.resetToNominal(sensors, simulatedTime, profile);
        Map<String, MachineWorkload> perMetric = workloadByMachineMetric.computeIfAbsent(machineId, k -> new LinkedHashMap<>());
        for (MetricProfile metric : profile.metrics()) {
            perMetric.put(metric.metricKey(), MachineWorkload.NORMAL);
        }

        refreshSimulatedClock();
        session.insert(new SafetyCheck(machineId, Duration.ofHours(24), simulatedTime));
        session.fireAllRules();
        broadcastSnapshot();
    }

    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("snapshot").data(snapshot()));
        } catch (IOException ignored) {
        }
        return emitter;
    }

    public SimulationReport snapshot() {
        return SimulationSnapshotBuilder.build(
                tickMinutes,
                simulatedTime,
                session,
                sensors,
                workloadByMachineMetric,
                machineRegistry.profilesInOrder(),
                sensorTraceThisTick,
                rulesFromLastCompletedTick
        );
    }

    private void broadcastSnapshot() {
        SimulationReport report = snapshot();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("snapshot").data(report));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }

    private synchronized void tickInternal(long stepMinutes, long floorMinutes) {
        if (session == null) {
            return;
        }

        long step = Math.max(floorMinutes, stepMinutes);
        rulesCollectedThisTick.clear();
        sensorTraceThisTick.clear();

        long remaining = step;
        while (remaining > 0) {
            long adv = Math.min(SIMULATED_MINUTES_BETWEEN_SAMPLES, remaining);
            clock.advanceTime(adv * 60_000L, TimeUnit.MILLISECONDS);
            simulatedTime = simulatedTime.plus(Duration.ofMinutes(adv));
            remaining -= adv;

            WorkingMemoryOps.deleteFactsOfType(session, SafetyCheck.class);
            WorkingMemoryOps.deleteFactsOfType(session, SafetyResult.class);
            WorkingMemoryOps.deleteTransientUnsafeReasons(session);

            refreshSimulatedClock();

            Set<String> halted = WorkingMemoryOps.haltedMachineIds(session);

            long cutoff = simulatedTime.minus(Duration.ofHours(14)).toEpochMilli();
            WorkingMemoryOps.pruneOldTelemetryAndTicks(session, cutoff);

            for (Machine m : WorkingMemoryOps.getFacts(session, Machine.class)) {
                String machineId = m.getMachineId();
                machineRegistry.profile(machineId).ifPresent(profile ->
                        telemetry.generateStep(session, sensors, halted, workloadByMachineMetric, simulatedTime, profile)
                );
            }

            for (Machine m : WorkingMemoryOps.getFacts(session, Machine.class)) {
                String id = m.getMachineId();
                if (id != null) {
                    session.insert(new SafetyCheck(id, Duration.ofHours(24), simulatedTime));
                }
            }

            session.fireAllRules();
            insertTickFactsForAllMachines(halted);
            session.fireAllRules();

            sensorTraceThisTick.add(SimulatedTelemetry.copySensorsSnapshot(sensors));
        }

        finalizeTickReporting();
    }

    private void finalizeTickReporting() {
        synchronized (rulesCollectedThisTick) {
            rulesFromLastCompletedTick = RuleMatchModel.sortRuleFiringsDesc(new ArrayList<>(rulesCollectedThisTick));
        }
    }

    private void insertTickFactsForAllMachines(Set<String> haltedMachineIds) {
        Map<String, CurrentMetric> currentByKey = new HashMap<>();
        for (CurrentMetric cm : WorkingMemoryOps.getFacts(session, CurrentMetric.class)) {
            if (cm.getMachineId() != null && cm.getMetricKey() != null) {
                currentByKey.put(cm.getMachineId() + ":" + cm.getMetricKey(), cm);
            }
        }

        for (Machine m : WorkingMemoryOps.getFacts(session, Machine.class)) {
            String id = m.getMachineId();
            if (id == null || haltedMachineIds.contains(id)) {
                continue;
            }
            MachineProcessProfile profile = machineRegistry.require(id);
            Map<String, Double> valuesByMetric = latestValuesByMetric(profile);
            boolean stress = profile.sustainedStressBandActive(valuesByMetric);
            long idx = tickIndexByMachine.getOrDefault(id, 0L) + 1L;
            tickIndexByMachine.put(id, idx);
            session.insert(new TickStatus(id, stress, idx, simulatedTime.toEpochMilli()));
            for (MetricProfile metric : profile.metrics()) {
                double v = valuesByMetric.getOrDefault(metric.metricKey(), Double.NaN);
                if (!Double.isNaN(v)) {
                    session.insert(new MetricTick(id, metric.metricKey(), v, idx, simulatedTime.toEpochMilli()));

                    String cmKey = id + ":" + metric.metricKey();
                    CurrentMetric existing = currentByKey.get(cmKey);
                    if (existing == null) {
                        session.insert(new CurrentMetric(id, metric.metricKey(), v, simulatedTime.toEpochMilli()));
                    } else {
                        existing.setValue(v);
                        existing.setTs(simulatedTime.toEpochMilli());
                        session.update(session.getFactHandle(existing), existing);
                    }
                }
            }
        }
    }

    private Map<String, Double> latestValuesByMetric(MachineProcessProfile profile) {
        Map<String, Double> values = new LinkedHashMap<>();
        for (MetricProfile metric : profile.metrics()) {
            SensorStatus s = sensors.get(profile.machineId() + ":" + metric.metricKey());
            values.put(metric.metricKey(), s == null ? Double.NaN : s.getValue());
        }
        return values;
    }

    private void refreshSimulatedClock() {
        WorkingMemoryOps.deleteFactsOfType(session, SimulatedClock.class);
        session.insert(new SimulatedClock(simulatedTime));
    }

    private void seedFacts() {
        for (MachineProcessProfile p : machineRegistry.profilesInOrder()) {
            session.insert(new Machine(p.machineId(), p.displayName(), p.machineType()));
            session.insert(new ComponentStatus(
                    p.machineId(),
                    p.componentType(),
                    simulatedTime.minus(p.componentAgeAtStart()),
                    p.serviceInterval()
            ));
            telemetry.resetToNominal(sensors, simulatedTime, p);
        }
        session.insert(new SimulatedClock(simulatedTime));
    }

    @Override
    public void destroy() {
        if (session != null) {
            try {
                session.dispose();
            } catch (Exception ignored) {
            }
        }
    }
}
