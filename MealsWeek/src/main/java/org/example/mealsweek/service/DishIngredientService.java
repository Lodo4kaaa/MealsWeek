package org.example.mealsweek.service;

import lombok.RequiredArgsConstructor;
import org.example.mealsweek.dto.DishIngredientDto;
import org.example.mealsweek.dto.mapper.DishIngredientMapper;
import org.example.mealsweek.entity.Dish;
import org.example.mealsweek.entity.DishIngredient;
import org.example.mealsweek.entity.Ingredient;
import org.example.mealsweek.entity.MeasurementUnit;
import org.example.mealsweek.entity.embeddable.DishIngredientKey;
import org.example.mealsweek.exception.ResourceNotFoundException;
import org.example.mealsweek.repository.DishIngredientRepository;
import org.example.mealsweek.repository.DishRepository;
import org.example.mealsweek.repository.IngredientRepository;
import org.example.mealsweek.repository.MeasurementUnitRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DishIngredientService {

    private final DishIngredientRepository dishIngredientRepository;
    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;
    private final MeasurementUnitRepository measurementUnitRepository;
    private final DishIngredientMapper dishIngredientMapper;

    @Transactional(readOnly = true)
    public List<DishIngredientDto> getAll() {
        return dishIngredientMapper.toDtoList(dishIngredientRepository.findAll());
    }

    @Transactional(readOnly = true)
    public DishIngredientDto getById(Long dishId, Long ingredientId) {
        DishIngredientKey key = new DishIngredientKey(dishId, ingredientId);

        DishIngredient entity = dishIngredientRepository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DishIngredient", "id", dishId + ":" + ingredientId
                ));

        return dishIngredientMapper.toDto(entity);
    }

    public DishIngredientDto create(DishIngredientDto dto) {
        validate(dto);

        Dish dish = dishRepository.findById(dto.dishId())
                .orElseThrow(() -> new ResourceNotFoundException("Dish", "id", dto.dishId()));

        Ingredient ingredient = ingredientRepository.findById(dto.ingredientId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", "id", dto.ingredientId()));

        MeasurementUnit measurementUnit = measurementUnitRepository.findById(dto.measurementUnitId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MeasurementUnit", "id", dto.measurementUnitId()
                ));

        DishIngredientKey key = new DishIngredientKey(dish.getId(), ingredient.getId());

        if (dishIngredientRepository.existsById(key)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Такой ингредиент уже добавлен в блюдо"
            );
        }

        DishIngredient entity = new DishIngredient();
        entity.setId(key);
        entity.setDish(dish);
        entity.setIngredient(ingredient);
        entity.setAmount(dto.amount());
        entity.setMeasurementUnit(measurementUnit);
        entity.setNote(normalize(dto.note()));

        return dishIngredientMapper.toDto(dishIngredientRepository.save(entity));
    }

    public DishIngredientDto update(Long dishId, Long ingredientId, DishIngredientDto dto) {
        validate(dto);

        DishIngredientKey key = new DishIngredientKey(dishId, ingredientId);

        DishIngredient entity = dishIngredientRepository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DishIngredient", "id", dishId + ":" + ingredientId
                ));

        MeasurementUnit measurementUnit = measurementUnitRepository.findById(dto.measurementUnitId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MeasurementUnit", "id", dto.measurementUnitId()
                ));

        entity.setAmount(dto.amount());
        entity.setMeasurementUnit(measurementUnit);
        entity.setNote(normalize(dto.note()));

        return dishIngredientMapper.toDto(dishIngredientRepository.save(entity));
    }

    public void delete(Long dishId, Long ingredientId) {
        DishIngredientKey key = new DishIngredientKey(dishId, ingredientId);

        DishIngredient entity = dishIngredientRepository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DishIngredient", "id", dishId + ":" + ingredientId
                ));

        dishIngredientRepository.delete(entity);
    }

    private void validate(DishIngredientDto dto) {
        if (dto.dishId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dishId обязателен");
        }

        if (dto.ingredientId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ingredientId обязателен");
        }

        if (dto.amount() == null || dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Количество должно быть больше 0"
            );
        }

        if (dto.measurementUnitId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "measurementUnitId обязателен"
            );
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}