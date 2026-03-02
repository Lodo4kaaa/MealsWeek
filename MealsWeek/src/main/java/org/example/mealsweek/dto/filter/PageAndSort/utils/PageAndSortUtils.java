package org.example.mealsweek.dto.filter.PageAndSort.utils;

import org.example.mealsweek.dto.filter.PageAndSort.PageAndSortFilter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


public final class PageAndSortUtils {

    private PageAndSortUtils() {}

    public static Pageable toPageable(PageAndSortFilter pas){
        int page = (pas != null && pas.getPage() != null && pas.getPage() > 0)
                ? pas.getPage()
                : 0;

        int size = (pas != null && pas.getSize() != null && pas.getSize() > 0)
                ? pas.getSize()
                : 20;

        String sortField = (pas != null && pas.getSort() != null)
                ? pas.getSort()
                : "id";

        Sort.Direction dir = (pas != null && pas.getDirection() != null)
                ? pas.getDirection()
                : Sort.Direction.ASC;


        return PageRequest.of(page, size, Sort.by(dir, sortField));
    }
}
