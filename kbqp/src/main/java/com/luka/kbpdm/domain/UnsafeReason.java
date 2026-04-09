package com.luka.kbpdm.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnsafeReason {
    private String machineId;
    private String code;
    private String details;
}

