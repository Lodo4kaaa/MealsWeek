package org.example.mealsweek.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mealsweek.entity.embeddable.WeekDishKey;

@Entity
@Table(name = "week_dish")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WeekDish {
    @EmbeddedId
    @EqualsAndHashCode.Include
    private WeekDishKey id;

    @MapsId("weekId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "week_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_week_dish_week"))
    @ToString.Exclude
    private Week week;

    @MapsId("dishId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dish_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_week_dish_dish"))
    @ToString.Exclude
    private Dish dish;

    @MapsId("dayOfWeekId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "day_of_week_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_week_dish_day"))
    @ToString.Exclude
    private DayOfWeek dayOfWeek;

    @PrePersist @PreUpdate
    private void syncId() {
        if (id == null && week != null && dish != null && dayOfWeek != null) {
            id = new WeekDishKey(week.getId(), dish.getId(), dayOfWeek.getId());
        }
    }
}
