package com.luka.kbpdm.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MachineHealthReport {

    private String machineId;
    private int healthPercent;

    private int anomalyCount;
    private int interventionCount;
    private int unsafeReasonCount;
    private int fixCount;

    private List<AnomalyHistoryLine> anomalyHistory = new ArrayList<>();
    private List<InterventionHistoryLine> interventionHistory = new ArrayList<>();
    private List<UnsafeReasonHistoryLine> unsafeReasonHistory = new ArrayList<>();
    private List<FixHistoryLine> fixHistory = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnomalyHistoryLine {
        private String type;
        private String description;
        private String at;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterventionHistoryLine {
        private String priority;
        private String recommendation;
        private String at;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnsafeReasonHistoryLine {
        private String code;
        private String details;
        private String at;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FixHistoryLine {
        private String at;
    }
}
