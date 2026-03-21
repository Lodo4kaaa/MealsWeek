package org.example.mealsweek.service;

import lombok.RequiredArgsConstructor;
import org.example.mealsweek.dto.MeasurementUnitDto;
import org.example.mealsweek.dto.mapper.MeasurementUnitMapper;
import org.example.mealsweek.repository.MeasurementUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeasurementUnitService {

    private final MeasurementUnitRepository measurementUnitRepository;
    private final MeasurementUnitMapper measurementUnitMapper;

    public List<MeasurementUnitDto> getAll() {
        return measurementUnitMapper.toDtoList(
                measurementUnitRepository.findAllByOrderByNameAsc()
        );
    }
}