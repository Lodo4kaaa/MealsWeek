package org.example.mealsweek.dto.mapper;

import org.example.mealsweek.dto.WeekDishDto;
import org.example.mealsweek.dto.mapper.context.WeekDishMappingContext;
import org.example.mealsweek.entity.WeekDish;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WeekDishMapper {

    @Mapping(target = "weekId", source = "week.id")
    @Mapping(target = "dishId", source = "dish.id")
    @Mapping(target = "dishName", source = "dish.name")
    @Mapping(target = "dayOfWeekId", source = "dayOfWeek.id")
    @Mapping(target = "dayOfWeekName", source = "dayOfWeek.name")
    WeekDishDto toDto(WeekDish entity);

    List<WeekDishDto> toDtoList(List<WeekDish> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "week", expression = "java(ctx.getWeekRef(dto.weekId()))")
    @Mapping(target = "dish", expression = "java(ctx.getDishRef(dto.dishId()))")
    @Mapping(target = "dayOfWeek", expression = "java(ctx.getDayRef(dto.dayOfWeekId()))")
    WeekDish toEntity(WeekDishDto dto, @Context WeekDishMappingContext ctx);
}
