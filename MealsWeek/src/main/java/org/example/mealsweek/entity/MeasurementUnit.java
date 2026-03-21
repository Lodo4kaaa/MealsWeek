package org.example.mealsweek.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "measurement_unit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "measurement_unit_id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;
}