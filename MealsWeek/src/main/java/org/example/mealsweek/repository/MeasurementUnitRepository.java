package org.example.mealsweek.repository;

import org.example.mealsweek.entity.MeasurementUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeasurementUnitRepository extends JpaRepository<MeasurementUnit, Long> {
    List<MeasurementUnit> findAllByOrderByNameAsc();
}