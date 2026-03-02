package org.example.mealsweek.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "day_of_week", uniqueConstraints = {
        @UniqueConstraint(name = "uk_day_of_week_name", columnNames = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayOfWeek {
    @Id
    @Column(name = "day_of_week_id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;
}
