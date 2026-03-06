package com.rcd.movierecommender.backend.dto;

import java.util.List;

public class PagedRatingResponse {
    private List<RatingDto> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;

    public PagedRatingResponse() {
    }

    public PagedRatingResponse(List<RatingDto> content, long totalElements, int totalPages, int page, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.page = page;
        this.size = size;
    }

    public List<RatingDto> getContent() {
        return content;
    }

    public void setContent(List<RatingDto> content) {
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
