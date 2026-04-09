package com.luka.kbpdm.simulation.machines;

import com.luka.kbpdm.domain.ComponentType;
import com.luka.kbpdm.domain.MachineType;

import java.time.Duration;
import java.util.Objects;

import static com.luka.kbpdm.simulation.SimulationConstants.TELEMETRY_NORMAL_DRIFT_HIGH_MULT;

public record MachineProcessProfile(
        String machineId,
        String displayName,
        MachineType machineType,
        ComponentType componentType,
        Duration serviceInterval,
        Duration componentAgeAtStart,
        double tempNominalC,
        double vibNominalRms,
        double tempCreepScale,
        double vibCreepScale,
        double tempAnomalyThresholdC,
        double vibAnomalyThresholdRms,
        double tempStressThresholdC,
        double vibStressThresholdRms
) {

    public MachineProcessProfile {
        Objects.requireNonNull(machineId, "machineId");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(machineType, "machineType");
        Objects.requireNonNull(componentType, "componentType");
        Objects.requireNonNull(serviceInterval, "serviceInterval");
        Objects.requireNonNull(componentAgeAtStart, "componentAgeAtStart");
        if (tempAnomalyThresholdC <= tempStressThresholdC) {
            throw new IllegalArgumentException(
                    machineId + ": temp anomaly must be > temp stress (" + tempAnomalyThresholdC + " <= " + tempStressThresholdC + ")");
        }
        if (vibAnomalyThresholdRms <= vibStressThresholdRms) {
            throw new IllegalArgumentException(
                    machineId + ": vib anomaly must be > vib stress");
        }
        double tempNormalHi = tempNominalC * TELEMETRY_NORMAL_DRIFT_HIGH_MULT;
        double vibNormalHi = vibNominalRms * TELEMETRY_NORMAL_DRIFT_HIGH_MULT;
        if (tempStressThresholdC <= tempNormalHi) {
            throw new IllegalArgumentException(
                    machineId + ": temp stress must exceed NORMAL workload upper envelope (~"
                            + tempNormalHi + "°C), got " + tempStressThresholdC);
        }
        if (vibStressThresholdRms <= vibNormalHi) {
            throw new IllegalArgumentException(
                    machineId + ": vib stress must exceed NORMAL workload upper envelope (~"
                            + vibNormalHi + " RMS), got " + vibStressThresholdRms);
        }
    }

    public boolean sustainedStressBandActive(double tempC, double vibRms) {
        if (Double.isNaN(tempC) || Double.isNaN(vibRms)) {
            return false;
        }
        return tempC >= tempStressThresholdC || vibRms >= vibStressThresholdRms;
    }

    public double telemetryTempThresholdHint() {
        return tempAnomalyThresholdC;
    }

    public double telemetryVibThresholdHint() {
        return vibAnomalyThresholdRms;
    }
}
