package com.luka.kbpdm.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Machine {
    private String machineId;
    private String name;
    private MachineType type;
}
