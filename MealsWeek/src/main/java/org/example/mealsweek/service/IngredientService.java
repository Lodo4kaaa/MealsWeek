package org.example.mealsweek.service;

import lombok.RequiredArgsConstructor;
import org.example.mealsweek.dto.IngredientBulkSaveResult;
import org.example.mealsweek.dto.IngredientDto;
import org.example.mealsweek.dto.filter.IngredientFilter;
import org.example.mealsweek.dto.mapper.IngredientMapper;
import org.example.mealsweek.entity.Ingredient;
import org.example.mealsweek.exception.ResourceNotFoundException;
import org.example.mealsweek.repository.DishIngredientRepository;
import org.example.mealsweek.repository.IngredientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

import static org.example.mealsweek.dto.filter.PageAndSort.utils.PageAndSortUtils.toPageable;

@Service
@RequiredArgsConstructor
@Transactional
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;
    private final DishIngredientRepository dishIngredientRepository;

    @Transactional(readOnly = true)
    public Page<IngredientDto> getAllByFilter(IngredientFilter filter) {
        Pageable pageable = toPageable(filter);
        Page<Ingredient> page = ingredientRepository.findByFilter(filter.getName(), pageable);
        return ingredientMapper.toIngredientDtoPage(page);
    }

    @Transactional(readOnly = true)
    public IngredientDto getById(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", "id", id));
        return ingredientMapper.toDto(ingredient);
    }

    public IngredientDto create(IngredientDto dto) {
        String name = normalizeName(dto.name());
        validateNameRequired(name);

        if (ingredientRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ингредиент с названием '" + name + "' уже существует"
            );
        }

        Ingredient entity = new Ingredient();
        entity.setName(name);

        Ingredient saved = ingredientRepository.save(entity);
        return ingredientMapper.toDto(saved);
    }

    public IngredientDto update(Long id, IngredientDto dto) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", "id", id));

        String name = normalizeName(dto.name());
        validateNameRequired(name);

        // ✅ корректная проверка уникальности:
        // если нашли ингредиент с таким именем и это НЕ текущий id — конфликт
        Optional<Ingredient> byName = ingredientRepository.findByNameIgnoreCase(name);
        if (byName.isPresent() && !byName.get().getId().equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ингредиент с названием '" + name + "' уже существует"
            );
        }

        ingredient.setName(name);
        Ingredient saved = ingredientRepository.save(ingredient);
        return ingredientMapper.toDto(saved);
    }

    public void delete(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient", "id", id));

        // сначала удаляем связи dish_ingredient -> ingredient
        dishIngredientRepository.deleteByIngredient_Id(id);

        ingredientRepository.delete(ingredient);
    }

    public IngredientBulkSaveResult saveAllBulk(List<IngredientDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return new IngredientBulkSaveResult(List.of(), 0, 0, 0);
        }

        // 1) нормализуем и считаем дубли внутри запроса
        Map<String, String> lowerToOriginal = new LinkedHashMap<>(); // сохранить порядок
        int duplicatesInRequest = 0;

        for (IngredientDto dto : dtos) {
            String name = normalizeName(dto.name());
            validateNameRequired(name);

            String key = name.toLowerCase(Locale.ROOT);
            if (lowerToOriginal.containsKey(key)) {
                duplicatesInRequest++;
                continue;
            }
            lowerToOriginal.put(key, name);
        }

        List<String> namesLower = new ArrayList<>(lowerToOriginal.keySet());

        // 2) одним запросом узнаём, что уже есть в БД
        List<Ingredient> existing = ingredientRepository.findAllByNameLowerIn(namesLower);
        Map<String, Ingredient> existingMap = existing.stream()
                .collect(Collectors.toMap(i -> i.getName().toLowerCase(Locale.ROOT), i -> i));

        int existed = existingMap.size();

        // 3) создаём только отсутствующие
        List<IngredientDto> result = new ArrayList<>();
        int created = 0;

        for (String key : namesLower) {
            if (existingMap.containsKey(key)) {
                result.add(ingredientMapper.toDto(existingMap.get(key)));
                continue;
            }

            Ingredient createdEntity = new Ingredient();
            createdEntity.setName(lowerToOriginal.get(key));
            Ingredient saved = ingredientRepository.save(createdEntity);
            result.add(ingredientMapper.toDto(saved));
            created++;
        }

        return new IngredientBulkSaveResult(result, created, existed, duplicatesInRequest);
    }

// ===== helpers =====

    private void validateNameRequired(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Название ингредиента обязательно"
            );
        }
    }

    private String normalizeName(String name) {
        if (name == null) return null;
        return name.trim();
    }

}
