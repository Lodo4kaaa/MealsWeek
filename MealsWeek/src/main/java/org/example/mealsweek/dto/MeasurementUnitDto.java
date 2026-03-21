package org.example.mealsweek.dto;

import java.io.Serializable;

public record MeasurementUnitDto(
        Long id,
        String name
) implements Serializable {
}