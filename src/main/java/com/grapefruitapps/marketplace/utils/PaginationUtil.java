package com.grapefruitapps.marketplace.utils;

import org.springframework.data.domain.Pageable;

public class PaginationUtil {
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_PAGE_NUMBER = 0;

    private PaginationUtil() {
    }

    public static Pageable getPageable(Integer filterPageSize, Integer filterPageNumber) {
        int pageSize = filterPageSize != null ? filterPageSize : DEFAULT_PAGE_SIZE;
        int pageNumber = filterPageNumber != null ? filterPageNumber : DEFAULT_PAGE_NUMBER;
        return Pageable.ofSize(pageSize).withPage(pageNumber);
    }
}
