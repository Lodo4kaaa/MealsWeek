package org.example.mealsweek.repository;

import org.example.mealsweek.entity.Ingredient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    @Query("""
select i from Ingredient i
where lower(i.name) like lower(concat('%', coalesce(:name,  ''), '%'))
""")
    Page<Ingredient> findByFilter(@Param("name") String name, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    Optional<Ingredient> findByNameIgnoreCase(String name);

    @Query("""
    select i
    from Ingredient i
    where lower(i.name) in :namesLower
""")
    List<Ingredient> findAllByNameLowerIn(@Param("namesLower") List<String> namesLower);
}
