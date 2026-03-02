package org.example.mealsweek.dto.mapper;

import org.example.mealsweek.dto.DishIngredientDto;
import org.example.mealsweek.dto.mapper.context.DishIngredientMappingContext;
import org.example.mealsweek.entity.DishIngredient;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DishIngredientMapper {

    @Mapping(target = "dishId", source = "dish.id")
    @Mapping(target = "dishName", source = "dish.name")
    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    DishIngredientDto toDto(DishIngredient entity);

    List<DishIngredientDto> toDtoList(List<DishIngredient> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dish", expression = "java(ctx.dishRef(dto.dishId()))")
    @Mapping(target = "ingredient", expression = "java(ctx.ingredientRef(dto.ingredientId()))")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "unit", source = "unit")
    @Mapping(target = "note", source = "note")
    DishIngredient toEntity(DishIngredientDto dto, @Context DishIngredientMappingContext ctx);
}
