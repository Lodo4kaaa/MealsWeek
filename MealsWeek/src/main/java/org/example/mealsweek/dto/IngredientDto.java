package org.example.mealsweek.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record IngredientDto(
        Long id,

        @NotBlank(message = "Название ингредиент обязательно")
        String name
) implements Serializable {}
