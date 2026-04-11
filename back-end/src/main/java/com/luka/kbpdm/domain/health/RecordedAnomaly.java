package com.luka.kbpdm.domain.health;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordedAnomaly {
    private String machineId;
    private String typeName;
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant recordedAt;
}
