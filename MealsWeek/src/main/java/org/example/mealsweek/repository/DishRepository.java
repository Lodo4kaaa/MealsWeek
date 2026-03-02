package org.example.mealsweek.repository;

import org.example.mealsweek.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    Optional<Dish> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}