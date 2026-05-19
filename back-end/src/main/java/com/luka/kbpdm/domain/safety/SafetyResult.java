package com.luka.kbpdm.domain.safety;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SafetyResult {
    private String machineId;
    private String reason;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant evaluatedAt;
}
