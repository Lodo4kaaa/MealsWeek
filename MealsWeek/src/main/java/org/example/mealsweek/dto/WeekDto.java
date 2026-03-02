package org.example.mealsweek.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record WeekDto(
        Long id,

        @NotBlank(message = "Статус недели должен быть заполнен")
        boolean active
) implements Serializable {}