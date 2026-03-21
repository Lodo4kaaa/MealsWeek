package org.example.mealsweek.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mealsweek.entity.embeddable.DishIngredientKey;

import java.math.BigDecimal;

@Entity
@Table(name = "dish_ingredient")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DishIngredient {
    @EmbeddedId
    @EqualsAndHashCode.Include
    private DishIngredientKey id;

    @MapsId("dishId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dish_id" , nullable = false,
            foreignKey = @ForeignKey(name = "fk_dish_ingredient_dish"))
    @ToString.Exclude
    private Dish dish;

    @MapsId("ingredientId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_dish_ingredient_ingredient"))
    @ToString.Exclude
    private Ingredient ingredient;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "measurement_unit_id", nullable = false)
    private MeasurementUnit measurementUnit;;

    @Column(name = "note")
    private String note;

    @PrePersist
    @PreUpdate
    private void syncId() {
        if (id == null && dish != null && ingredient != null) {
            id = new DishIngredientKey(dish.getId(), ingredient.getId());
        }
    }
}
