package com.seatomatic.student.model.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Generic container for paginated query results.
 */
public class Page<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<T> items;
    private final long totalItems;
    private final int currentPage;
    private final int pageSize;
    private final int totalPages;

    public Page(List<T> items, long totalItems, int currentPage, int pageSize) {
        this.items = items != null ? Collections.unmodifiableList(items) : Collections.emptyList();
        this.totalItems = Math.max(0, totalItems);
        this.pageSize = Math.max(1, pageSize);
        this.totalPages = (int) Math.ceil((double) this.totalItems / this.pageSize);
        this.currentPage = Math.max(1, currentPage);
    }

    public List<T> getItems() {
        return items;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages == 0 ? 1 : totalPages;
    }

    public boolean hasPrevious() {
        return currentPage > 1;
    }

    public boolean hasNext() {
        return currentPage < getTotalPages();
    }

    public long getStartIndex() {
        if (totalItems == 0) return 0;
        return (long) (currentPage - 1) * pageSize + 1;
    }

    public long getEndIndex() {
        if (totalItems == 0) return 0;
        return Math.min((long) currentPage * pageSize, totalItems);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

