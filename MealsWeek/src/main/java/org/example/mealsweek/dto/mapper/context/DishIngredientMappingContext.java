package org.example.mealsweek.dto.mapper.context;

import lombok.RequiredArgsConstructor;
import org.example.mealsweek.entity.Dish;
import org.example.mealsweek.entity.Ingredient;
import org.example.mealsweek.repository.DishRepository;
import org.example.mealsweek.repository.IngredientRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DishIngredientMappingContext {

    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;

    public Dish dishRef(Long id) {
        return dishRepository.getReferenceById(id);
    }

    public Ingredient ingredientRef(Long id) {
        return ingredientRepository.getReferenceById(id);
    }
}
