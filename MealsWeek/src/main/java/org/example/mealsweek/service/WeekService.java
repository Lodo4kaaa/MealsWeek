package org.example.mealsweek.service;

import lombok.RequiredArgsConstructor;
import org.example.mealsweek.dto.WeekDishDto;
import org.example.mealsweek.dto.WeekDto;
import org.example.mealsweek.dto.mapper.WeekDishMapper;
import org.example.mealsweek.dto.mapper.WeekMapper;
import org.example.mealsweek.entity.DayOfWeek;
import org.example.mealsweek.entity.Dish;
import org.example.mealsweek.entity.Week;
import org.example.mealsweek.entity.WeekDish;
import org.example.mealsweek.entity.embeddable.WeekDishKey;
import org.example.mealsweek.exception.ResourceNotFoundException;
import org.example.mealsweek.repository.DayOfWeekRepository;
import org.example.mealsweek.repository.DishRepository;
import org.example.mealsweek.repository.WeekDishRepository;
import org.example.mealsweek.repository.WeekRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WeekService {

    private final WeekRepository weekRepository;
    private final WeekDishRepository weekDishRepository;
    private final DayOfWeekRepository dayOfWeekRepository;
    private final DishRepository dishRepository;
    private final WeekMapper weekMapper;
    private final WeekDishMapper weekDishMapper;

    @Transactional(readOnly = true)
    public List<WeekDto> getAllActiveWeeks() {
        return weekMapper.toDtoList(weekRepository.findAllByActiveTrue());
    }

    @Transactional(readOnly = true)
    public WeekDto getById(Long id) {
        Week week = getActiveWeekEntity(id);
        return weekMapper.toDto(week);
    }

    public WeekDto create() {
        Week week = new Week();
        week.setActive(true);
        return weekMapper.toDto(weekRepository.save(week));
    }

    public void softDelete(Long id) {
        Week week = weekRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Week", "id", id));
        week.setActive(false);
        weekRepository.save(week);
    }

    @Transactional(readOnly = true)
    public List<WeekDishDto> getWeekPlan(Long weekId) {
        getActiveWeekEntity(weekId);
        return weekDishMapper.toDtoList(
                weekDishRepository.findAllByWeek_IdOrderByDayOfWeek_IdAsc(weekId)
        );
    }

    @Transactional(readOnly = true)
    public List<WeekDishDto> getDayPlan(Long weekId, Integer dayOfWeekId) {
        getActiveWeekEntity(weekId);

        if (dayOfWeekId == null || dayOfWeekId < 1 || dayOfWeekId > 7) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "День недели должен быть от 1 до 7");
        }

        return weekDishMapper.toDtoList(
                weekDishRepository.findAllByWeek_IdAndDayOfWeek_IdOrderByDish_IdAsc(weekId, dayOfWeekId)
        );
    }

    public WeekDishDto assignDishToDay(Long weekId, Integer dayOfWeekId, Long dishId) {
        if (dayOfWeekId == null || dayOfWeekId < 1 || dayOfWeekId > 7) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "День недели должен быть от 1 до 7");
        }
        if (dishId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нужно выбрать блюдо");
        }

        Week week = getActiveWeekEntity(weekId);

        DayOfWeek day = dayOfWeekRepository.findById(dayOfWeekId)
                .orElseThrow(() -> new ResourceNotFoundException("DayOfWeek", "id", dayOfWeekId));

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Dish", "id", dishId));

        WeekDishKey key = new WeekDishKey(week.getId(), dish.getId(), day.getId());

        if (weekDishRepository.existsById(key)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Это блюдо уже добавлено на выбранный день"
            );
        }

        WeekDish weekDish = new WeekDish();
        weekDish.setId(key);
        weekDish.setWeek(week);
        weekDish.setDayOfWeek(day);
        weekDish.setDish(dish);

        return weekDishMapper.toDto(weekDishRepository.save(weekDish));
    }

    public void removeDishFromDay(Long weekId, Integer dayOfWeekId, Long dishId) {
        getActiveWeekEntity(weekId);

        if (dayOfWeekId == null || dayOfWeekId < 1 || dayOfWeekId > 7) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "День недели должен быть от 1 до 7");
        }

        if (dishId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dishId обязателен");
        }

        WeekDishKey key = new WeekDishKey(weekId, dishId, dayOfWeekId);

        if (!weekDishRepository.existsById(key)) {
            throw new ResourceNotFoundException("WeekDish", "id", key);
        }

        weekDishRepository.deleteByWeek_IdAndDayOfWeek_IdAndDish_Id(weekId, dayOfWeekId, dishId);
    }

    private Week getActiveWeekEntity(Long id) {
        Week week = weekRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Week", "id", id));

        if (!week.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неделя уже скрыта");
        }

        return week;
    }
}