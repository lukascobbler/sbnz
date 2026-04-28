package com.luka.kbpdm.domain.health;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordedMachineOverworked {
    private String machineId;
    private String details;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant recordedAt;
}
