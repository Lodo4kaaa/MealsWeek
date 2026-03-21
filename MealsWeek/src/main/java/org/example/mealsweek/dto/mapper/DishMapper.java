package org.example.mealsweek.dto.mapper;

import org.example.mealsweek.dto.DishDto;
import org.example.mealsweek.entity.Dish;
import org.example.mealsweek.entity.DishIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface DishMapper {
    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    @Mapping(target = "measurementUnitId", source = "measurementUnit.id")
    @Mapping(target = "measurementUnitName", source = "measurementUnit.name")
    DishDto.IngredientLineDto toIngredientLineDto(DishIngredient entity);

    List<DishDto.IngredientLineDto> toIngredientLineDtoList(Set<DishIngredient> entities);

    @Mapping(target = "ingredients", source = "ingredients")
    DishDto toDto(Dish dish);
}
