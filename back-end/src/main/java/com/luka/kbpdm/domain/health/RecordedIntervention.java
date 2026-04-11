package com.luka.kbpdm.domain.health;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordedIntervention {
    private String machineId;
    private String priority;
    private String recommendation;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant recordedAt;
}
