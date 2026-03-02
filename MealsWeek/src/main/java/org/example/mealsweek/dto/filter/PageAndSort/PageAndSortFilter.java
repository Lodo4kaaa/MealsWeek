package org.example.mealsweek.dto.filter.PageAndSort;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Sort;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class PageAndSortFilter {
    private Integer page;
    private Integer size;
    private String sort;
    private Sort.Direction direction;
}
