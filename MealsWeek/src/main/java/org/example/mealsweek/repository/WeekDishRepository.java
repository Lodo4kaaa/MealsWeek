package org.example.mealsweek.repository;

import org.example.mealsweek.entity.WeekDish;
import org.example.mealsweek.entity.embeddable.WeekDishKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeekDishRepository extends JpaRepository<WeekDish, WeekDishKey> {

}
