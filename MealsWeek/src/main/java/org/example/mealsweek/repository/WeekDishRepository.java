package org.example.mealsweek.repository;

import org.example.mealsweek.entity.WeekDish;
import org.example.mealsweek.entity.embeddable.WeekDishKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeekDishRepository extends JpaRepository<WeekDish, WeekDishKey> {

    List<WeekDish> findAllByWeek_IdOrderByDayOfWeek_IdAsc(Long weekId);

    List<WeekDish> findAllByWeek_IdAndDayOfWeek_IdOrderByDish_IdAsc(Long weekId, Integer dayOfWeekId);

    void deleteByWeek_IdAndDayOfWeek_IdAndDish_Id(Long weekId, Integer dayOfWeekId, Long dishId);
}