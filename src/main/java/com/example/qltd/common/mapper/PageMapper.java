package com.example.qltd.common.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.example.qltd.common.dto.PagedResponse;

import java.util.function.Function;

@Component
public class PageMapper {

    public <T, R> PagedResponse<R> toPagedResponse(
            Page<T> page,
            Function<T, R> mapper) {

        return PagedResponse.<R>builder()
                .items(page.getContent().stream().map(mapper).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();

    }

}
