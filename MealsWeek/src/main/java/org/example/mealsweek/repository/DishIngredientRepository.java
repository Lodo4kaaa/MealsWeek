package org.example.mealsweek.repository;

import org.example.mealsweek.entity.DishIngredient;
import org.example.mealsweek.entity.embeddable.DishIngredientKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DishIngredientRepository extends JpaRepository<DishIngredient, DishIngredientKey> {

}
