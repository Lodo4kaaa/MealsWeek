package org.example.mealsweek.repository;

import org.example.mealsweek.entity.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DayOfWeekRepository extends JpaRepository<DayOfWeek, Integer> {

}
