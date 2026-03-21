package org.example.mealsweek.dto.mapper;

import org.example.mealsweek.dto.DishIngredientDto;
import org.example.mealsweek.entity.DishIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DishIngredientMapper {

    @Mapping(target = "dishId", source = "dish.id")
    @Mapping(target = "dishName", source = "dish.name")
    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    @Mapping(target = "measurementUnitId", source = "measurementUnit.id")
    @Mapping(target = "measurementUnitName", source = "measurementUnit.name")
    DishIngredientDto toDto(DishIngredient entity);

    List<DishIngredientDto> toDtoList(List<DishIngredient> entities);
}