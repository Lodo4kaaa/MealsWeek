package org.example.mealsweek.dto;

import java.io.Serializable;
import java.util.List;

public record IngredientBulkSaveResult(
        List<IngredientDto> items,
        int created,
        int existed,
        int duplicatesInRequest
) implements Serializable {}