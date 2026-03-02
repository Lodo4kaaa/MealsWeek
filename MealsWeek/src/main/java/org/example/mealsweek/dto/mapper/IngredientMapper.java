package org.example.mealsweek.dto.mapper;

import org.example.mealsweek.dto.IngredientDto;
import org.example.mealsweek.entity.Ingredient;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(
        componentModel = "spring"
)
public interface IngredientMapper {

    IngredientDto toDto(Ingredient ingredient);

    Ingredient toEntity(IngredientDto dto);

    List<IngredientDto> toDtoList(List<Ingredient> ingredients);

    List<Ingredient> toEntityList(List<IngredientDto> dtos);

    default Page<IngredientDto> toIngredientDtoPage(Page<Ingredient> page) {
        return page.map(this::toDto);
    }
}
