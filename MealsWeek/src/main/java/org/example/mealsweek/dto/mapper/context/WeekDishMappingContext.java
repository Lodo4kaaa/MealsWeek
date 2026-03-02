package org.example.mealsweek.dto.mapper.context;

import lombok.RequiredArgsConstructor;
import org.example.mealsweek.entity.DayOfWeek;
import org.example.mealsweek.entity.Dish;
import org.example.mealsweek.entity.Week;
import org.example.mealsweek.repository.DayOfWeekRepository;
import org.example.mealsweek.repository.DishRepository;
import org.example.mealsweek.repository.WeekRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeekDishMappingContext {
    private final WeekRepository weekRepository;
    private final DishRepository dishRepository;
    private final DayOfWeekRepository dayOfWeekRepository;

    public Week getWeekRef(Long id) {
        return weekRepository.getReferenceById(id);
    }

    public Dish getDishRef(Long id) {
        return dishRepository.getReferenceById(id);
    }

    public DayOfWeek getDayRef(Integer id) {
        return dayOfWeekRepository.getReferenceById(id);
    }
}
