package com.luka.kbpdm.controller;

import com.luka.kbpdm.api.MachineWorkload;
import com.luka.kbpdm.api.SimulationReport;
import com.luka.kbpdm.domain.TelemetryMetric;
import com.luka.kbpdm.service.SimulationEngine;
import lombok.Data;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/sim")
public class SimulationController {

    private final SimulationEngine engine;

    public SimulationController(SimulationEngine engine) {
        this.engine = engine;
    }

    @GetMapping("/stream")
    public SseEmitter stream() {
        return engine.stream();
    }

    @PostMapping("/reset")
    public SimulationReport reset() {
        engine.reset();
        return engine.snapshot();
    }

    @PostMapping("/tick")
    public SimulationReport setTick(@RequestBody TickRequest req) {
        engine.setTickMinutes(req.tickMinutes);
        return engine.snapshot();
    }

    @PostMapping("/step")
    public SimulationReport step(@RequestBody StepRequest req) {
        long step = req.stepMinutes <= 0 ? engine.getTickMinutes() : req.stepMinutes;
        engine.step(Math.max(30, step));
        return engine.snapshot();
    }

    @PostMapping("/fix")
    public SimulationReport safetyFix(@RequestBody FixRequest req) {
        if (req == null || req.getMachineId() == null || req.getMachineId().isBlank()) {
            return engine.snapshot();
        }
        engine.operatorSafetyFix(req.getMachineId().trim());
        return engine.snapshot();
    }

    @PostMapping("/workload")
    public SimulationReport setWorkload(@RequestBody WorkloadRequest req) {
        if (req == null || req.getMachineId() == null || req.getMachineId().isBlank() || req.getWorkload() == null) {
            return engine.snapshot();
        }
        engine.setMachineWorkload(req.getMachineId().trim(), req.getWorkload(), req.getMetric());
        return engine.snapshot();
    }

    @Data
    public static class TickRequest {
        private long tickMinutes;
    }

    @Data
    public static class StepRequest {
        private long stepMinutes;
    }

    @Data
    public static class FixRequest {
        private String machineId;
    }

    @Data
    public static class WorkloadRequest {
        private String machineId;
        private MachineWorkload workload;
        private TelemetryMetric metric;
    }
}
