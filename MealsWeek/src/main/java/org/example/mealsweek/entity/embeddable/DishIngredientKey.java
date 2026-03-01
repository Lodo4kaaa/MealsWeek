package org.example.mealsweek.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishIngredientKey implements Serializable {
    @Column(name = "dish_id")
    private Long dishId;

    @Column(name = "ingredient_id")
    private Long ingredientId;
}
