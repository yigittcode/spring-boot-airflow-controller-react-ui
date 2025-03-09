package com.yigit.airflow_spring_rest_controller.dto.common;

import lombok.Data;

@Data
public class PageResponse {
    private int currentPage;
    private int totalPages;
    private int pageSize;
    private long totalElements;
    
    public static PageResponse of(int page, int size, long totalElements) {
        PageResponse pageResponse = new PageResponse();
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        pageResponse.setTotalElements(totalElements);
        pageResponse.setTotalPages((int) Math.ceil((double) totalElements / size));
        return pageResponse;
    }
} 