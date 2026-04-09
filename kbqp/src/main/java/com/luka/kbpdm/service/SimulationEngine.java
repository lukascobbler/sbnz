package com.luka.kbpdm.service;

import com.luka.kbpdm.api.MachineWorkload;
import com.luka.kbpdm.api.RuleFiring;
import com.luka.kbpdm.api.SensorStatus;
import com.luka.kbpdm.api.SimulationReport;
import com.luka.kbpdm.domain.*;
import com.luka.kbpdm.simulation.SimulationConstants;
import com.luka.kbpdm.simulation.drools.WorkingMemoryOps;
import com.luka.kbpdm.simulation.report.SimulationSnapshotBuilder;
import com.luka.kbpdm.simulation.rules.RuleMatchModel;
import com.luka.kbpdm.simulation.telemetry.SimulatedTelemetry;
import lombok.Getter;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
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

    private final Map<String, MachineWorkload> workloadByMachine = new HashMap<>();
    private final Map<String, Long> tickIndexByMachine = new HashMap<>();

    private final List<List<SensorStatus>> sensorTraceThisTick = new ArrayList<>();

    public SimulationEngine(
            KieContainer kieContainer,
            @Value("${simulation.tickMinutes:30}") long tickMinutes
    ) {
        this.kieContainer = kieContainer;
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
        this.workloadByMachine.clear();
        this.tickIndexByMachine.clear();
        this.workloadByMachine.put(MID_LINE, MachineWorkload.NORMAL);
        this.workloadByMachine.put(MID_CNC, MachineWorkload.NORMAL);

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
                    String out = RuleMatchModel.summarizeMatch(event);
                    rulesCollectedThisTick.add(new RuleFiring(name, machineId, out, at));
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

    public synchronized void setMachineWorkload(String machineId, MachineWorkload workload) {
        if (session == null || workload == null) {
            return;
        }
        if (!MID_LINE.equals(machineId) && !MID_CNC.equals(machineId)) {
            return;
        }
        workloadByMachine.put(machineId, workload);
        broadcastSnapshot();
    }

    private static long clampTickMinutes(long tickMinutes) {
        long t = Math.max(MIN_TICK_MINUTES, Math.min(MAX_TICK_MINUTES, tickMinutes));
        t = (t / SIMULATED_MINUTES_BETWEEN_SAMPLES) * SIMULATED_MINUTES_BETWEEN_SAMPLES;
        if (t < MIN_TICK_MINUTES) {
            t = MIN_TICK_MINUTES;
        }
        return t;
    }

    public synchronized void step(long stepMinutes) {
        long m = stepMinutes <= 0 ? tickMinutes : stepMinutes;
        tickInternal(Math.max(MIN_TICK_MINUTES, m), MIN_TICK_MINUTES);
        broadcastSnapshot();
    }

    public synchronized void operatorSafetyFix(String machineId) {
        if (session == null) {
            return;
        }
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
        WorkingMemoryOps.deleteFactsForMachine(session, Condition.class, machineId);
        WorkingMemoryOps.deleteFactsForMachine(session, Anomaly.class, machineId);
        WorkingMemoryOps.deleteFactsForMachine(session, Intervention.class, machineId);
        WorkingMemoryOps.deleteFactsForMachine(session, UnsafeReason.class, machineId);
        WorkingMemoryOps.deleteFactsForMachine(session, SafetyResult.class, machineId);
        WorkingMemoryOps.deleteFactsForMachine(session, SafetyCheck.class, machineId);
        WorkingMemoryOps.deleteFactsForMachine(session, MachineHalted.class, machineId);
        WorkingMemoryOps.deleteFactsForMachine(session, TickStatus.class, machineId);
        WorkingMemoryOps.deleteFactsForMachine(session, TelemetryReading.class, machineId);
        telemetry.removeKeysStartingWith(machineId + ":");
        tickIndexByMachine.remove(machineId);

        restoreComponentStatusAfterOperatorFix(machineId);
        telemetry.resetToNominal(sensors, simulatedTime, machineId);
        workloadByMachine.put(machineId, MachineWorkload.NORMAL);

        refreshSimulatedClock();
        rulesCollectedThisTick.clear();
        session.insert(new SafetyCheck(machineId, Duration.ofHours(24), simulatedTime));
        session.fireAllRules();
        finalizeTickReporting();
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
                workloadByMachine,
                MID_LINE,
                MID_CNC,
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
            WorkingMemoryOps.deleteFactsOfType(session, UnsafeReason.class);

            refreshSimulatedClock();

            Set<String> halted = WorkingMemoryOps.haltedMachineIds(session);

            long cutoff = simulatedTime.minus(Duration.ofHours(14)).toEpochMilli();
            WorkingMemoryOps.pruneOldTelemetryAndTicks(session, cutoff);

            for (Machine m : WorkingMemoryOps.getFacts(session, Machine.class)) {
                telemetry.generateStep(session, sensors, halted, workloadByMachine, simulatedTime, m);
            }

            for (Machine m : WorkingMemoryOps.getFacts(session, Machine.class)) {
                String id = m.getMachineId();
                if (id != null) {
                    session.insert(new SafetyCheck(id, Duration.ofHours(24), simulatedTime));
                }
            }

            session.fireAllRules();

            insertTickStatusForAllMachines(halted);
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

    private void insertTickStatusForAllMachines(Set<String> haltedMachineIds) {
        for (Machine m : WorkingMemoryOps.getFacts(session, Machine.class)) {
            String id = m.getMachineId();
            if (id == null) {
                continue;
            }
            if (haltedMachineIds.contains(id)) {
                continue;
            }
            boolean bearing = hasBearingFailureAnomaly(id);
            double temp = latestSensorValueOrNaN(id, TelemetryMetric.TEMPERATURE_C);
            double vib = latestSensorValueOrNaN(id, TelemetryMetric.VIBRATION_RMS);
            long idx = tickIndexByMachine.getOrDefault(id, 0L) + 1L;
            tickIndexByMachine.put(id, idx);
            session.insert(new TickStatus(id, bearing, temp, vib, idx, simulatedTime.toEpochMilli()));
        }
    }

    private double latestSensorValueOrNaN(String machineId, TelemetryMetric metric) {
        SensorStatus s = sensors.get(machineId + ":" + metric.name());
        if (s == null) {
            return Double.NaN;
        }
        return s.getValue();
    }

    private boolean hasBearingFailureAnomaly(String machineId) {
        for (Anomaly a : WorkingMemoryOps.getFacts(session, Anomaly.class)) {
            if (machineId.equals(a.getMachineId()) && a.getType() == AnomalyType.POTENTIAL_BEARING_FAILURE) {
                return true;
            }
        }
        return false;
    }

    private void refreshSimulatedClock() {
        WorkingMemoryOps.deleteFactsOfType(session, SimulatedClock.class);
        session.insert(new SimulatedClock(simulatedTime));
    }

    private void seedFacts() {
        session.insert(new Machine(MID_LINE, "Line", MachineType.CONVEYOR));
        session.insert(new Machine(MID_CNC, "CNC", MachineType.CNC));
        insertInitialComponentStatuses();
        session.insert(new SimulatedClock(simulatedTime));
    }

    private void insertInitialComponentStatuses() {
        session.insert(new ComponentStatus(
                MID_LINE,
                ComponentType.BEARING,
                HealthStatus.OK,
                simulatedTime.minus(Duration.ofDays(25)),
                Duration.ofDays(30)
        ));
        session.insert(new ComponentStatus(
                MID_CNC,
                ComponentType.MOTOR,
                HealthStatus.OK,
                simulatedTime.minus(Duration.ofDays(10)),
                Duration.ofDays(90)
        ));
    }

    private void restoreComponentStatusAfterOperatorFix(String machineId) {
        WorkingMemoryOps.deleteFactsForMachine(session, ComponentStatus.class, machineId);
        if (MID_LINE.equals(machineId)) {
            session.insert(new ComponentStatus(
                    MID_LINE,
                    ComponentType.BEARING,
                    HealthStatus.OK,
                    simulatedTime,
                    Duration.ofDays(30)
            ));
        } else if (MID_CNC.equals(machineId)) {
            session.insert(new ComponentStatus(
                    MID_CNC,
                    ComponentType.MOTOR,
                    HealthStatus.OK,
                    simulatedTime,
                    Duration.ofDays(90)
            ));
        }
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
