package com.luka.kbpdm.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleFiring {
    private String ruleName;
    private String machineId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant firedAt;
}
