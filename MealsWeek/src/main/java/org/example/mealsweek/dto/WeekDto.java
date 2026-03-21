package org.example.mealsweek.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record WeekDto(
        Long id,
        boolean active
) implements Serializable {}