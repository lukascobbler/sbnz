package com.luka.kbpdm.domain.safety;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MachineOverworked {
    private String machineId;
    private String details;
}
