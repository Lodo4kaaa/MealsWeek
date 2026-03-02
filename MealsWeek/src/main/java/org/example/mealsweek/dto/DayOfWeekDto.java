package org.example.mealsweek.dto;

import java.io.Serializable;

public record DayOfWeekDto(
        Integer id,
        String name
) implements Serializable {}
