package org.example.mealsweek.service;

import lombok.RequiredArgsConstructor;
import org.example.mealsweek.dto.DishDto;
import org.example.mealsweek.dto.mapper.DishMapper;
import org.example.mealsweek.entity.Dish;
import org.example.mealsweek.entity.DishIngredient;
import org.example.mealsweek.entity.Ingredient;
import org.example.mealsweek.entity.MeasurementUnit;
import org.example.mealsweek.entity.embeddable.DishIngredientKey;
import org.example.mealsweek.exception.ResourceNotFoundException;
import org.example.mealsweek.repository.DishRepository;
import org.example.mealsweek.repository.IngredientRepository;
import org.example.mealsweek.repository.MeasurementUnitRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class DishService {

    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;
    private final MeasurementUnitRepository measurementUnitRepository;
    private final DishMapper dishMapper;

    @Transactional(readOnly = true)
    public List<DishDto> getAll() {
        return dishRepository.findAll().stream()
                .map(dishMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public DishDto getById(Long id) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish", "id", id));
        return dishMapper.toDto(dish);
    }

    public DishDto create(DishDto dto) {
        String name = normalizeName(dto.name());
        validateDish(name, dto.ingredients());

        if (dishRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Блюдо с названием '" + name + "' уже существует"
            );
        }

        Dish dish = new Dish();
        dish.setName(name);
        dish.setDescription(normalizeText(dto.description()));

        Dish savedDish = dishRepository.save(dish);
        rebuildIngredients(savedDish, dto.ingredients());

        Dish result = dishRepository.save(savedDish);
        return dishMapper.toDto(result);
    }

    public DishDto update(Long id, DishDto dto) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish", "id", id));

        String name = normalizeName(dto.name());
        validateDish(name, dto.ingredients());

        Optional<Dish> existing = dishRepository.findByNameIgnoreCase(name);
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Блюдо с названием '" + name + "' уже существует"
            );
        }

        dish.setName(name);
        dish.setDescription(normalizeText(dto.description()));

        rebuildIngredients(dish, dto.ingredients());

        Dish result = dishRepository.save(dish);
        return dishMapper.toDto(result);
    }

    public void delete(Long id) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish", "id", id));
        dishRepository.delete(dish);
    }

    private void rebuildIngredients(Dish dish, List<DishDto.IngredientLineDto> lines) {
        dish.getIngredients().clear();

        if (lines == null || lines.isEmpty()) {
            return;
        }

        Set<String> uniqueIngredients = new HashSet<>();

        for (DishDto.IngredientLineDto line : lines) {
            validateIngredientLine(line);

            Ingredient ingredient = resolveIngredient(line);
            MeasurementUnit measurementUnit = measurementUnitRepository.findById(line.measurementUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "MeasurementUnit", "id", line.measurementUnitId()
                    ));

            String uniqueKey = ingredient.getId() != null
                    ? "ID:" + ingredient.getId()
                    : "NAME:" + ingredient.getName().toLowerCase(Locale.ROOT);

            if (!uniqueIngredients.add(uniqueKey)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ингредиент '" + ingredient.getName() + "' указан в блюде несколько раз"
                );
            }

            DishIngredient dishIngredient = new DishIngredient();
            dishIngredient.setDish(dish);
            dishIngredient.setIngredient(ingredient);
            dishIngredient.setId(new DishIngredientKey(dish.getId(), ingredient.getId()));
            dishIngredient.setAmount(line.amount());
            dishIngredient.setMeasurementUnit(measurementUnit);
            dishIngredient.setNote(normalizeText(line.note()));

            dish.getIngredients().add(dishIngredient);
        }
    }

    private Ingredient resolveIngredient(DishDto.IngredientLineDto line) {
        if (line.ingredientId() != null) {
            return ingredientRepository.findById(line.ingredientId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Ingredient", "id", line.ingredientId()
                    ));
        }

        String ingredientName = normalizeName(line.ingredientName());
        if (ingredientName == null || ingredientName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Нужно указать существующий ingredientId или название нового ингредиента"
            );
        }

        return ingredientRepository.findByNameIgnoreCase(ingredientName)
                .orElseGet(() -> {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setName(ingredientName);
                    return ingredientRepository.save(ingredient);
                });
    }

    private void validateDish(String name, List<DishDto.IngredientLineDto> lines) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Название блюда обязательно");
        }

        if (lines == null || lines.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "У блюда должен быть хотя бы один ингредиент"
            );
        }
    }

    private void validateIngredientLine(DishDto.IngredientLineDto line) {
        if (line.amount() == null || line.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Количество ингредиента должно быть больше 0"
            );
        }

        if (line.measurementUnitId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Единица измерения обязательна"
            );
        }

        if (line.ingredientId() == null) {
            String ingredientName = normalizeName(line.ingredientName());
            if (ingredientName == null || ingredientName.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Для нового ингредиента нужно заполнить название"
                );
            }
        }
    }

    private String normalizeName(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}