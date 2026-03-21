package org.example.mealsweek.dto.mapper;

import org.example.mealsweek.dto.MeasurementUnitDto;
import org.example.mealsweek.entity.MeasurementUnit;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MeasurementUnitMapper {
    MeasurementUnitDto toDto(MeasurementUnit entity);
    List<MeasurementUnitDto> toDtoList(List<MeasurementUnit> entities);
}