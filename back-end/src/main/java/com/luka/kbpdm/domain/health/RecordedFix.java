package com.luka.kbpdm.domain.health;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordedFix {
    private String machineId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant recordedAt;
}
