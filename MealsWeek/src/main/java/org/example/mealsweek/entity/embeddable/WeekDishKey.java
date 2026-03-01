package org.example.mealsweek.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeekDishKey implements Serializable{
    @Column(name = "week_id")
    private Long weekId;

    @Column(name = "dish_id")
    private Long dishId;

    @Column(name = "day_of_week_id")
    private Integer dayOfWeekId;
}
