package org.example.mealsweek.dto;

import java.io.Serializable;

public record WeekDishDto(
        Long weekId,
        Long dishId,
        String dishName,
        Integer dayOfWeekId,
        String dayOfWeekName
) implements Serializable {}
