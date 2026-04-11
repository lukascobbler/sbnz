package com.luka.kbpdm.api;

import com.luka.kbpdm.domain.machine.MachineType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MachineProfileView {
    private String machineId;
    private String displayName;
    private MachineType machineType;
    private List<MetricProfileView> metrics = new ArrayList<>();
}
