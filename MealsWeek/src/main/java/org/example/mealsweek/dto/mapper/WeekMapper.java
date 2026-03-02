package org.example.mealsweek.dto.mapper;

import org.example.mealsweek.dto.WeekDto;
import org.example.mealsweek.entity.Week;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WeekMapper {
    WeekDto toDto(Week entity);

    Week toEntity(WeekDto dto);

    List<WeekDto> toDtoList(List<Week> entities);

    List<Week> toEntityList(List<WeekDto> dtos);
}
