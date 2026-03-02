package org.example.mealsweek.dto.mapper;

import org.example.mealsweek.dto.DayOfWeekDto;
import org.example.mealsweek.entity.DayOfWeek;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DayOfWeekMapper {
    DayOfWeekDto toDto(DayOfWeek entity);

    DayOfWeek toEntity(DayOfWeekDto dto);

    List<DayOfWeekDto> toDtoList(List<DayOfWeek> entities);

    List<DayOfWeek> toEntityList(List<DayOfWeekDto> dtos);
}
