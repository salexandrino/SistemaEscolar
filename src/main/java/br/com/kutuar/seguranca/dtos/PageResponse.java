package br.com.kutuar.seguranca.dtos;

import java.util.List;

/** Resultado imutável. Page e size já chegam normalizados pelo service. */
public record PageResponse<T>(int page, int size, long totalItems, long totalPages, List<T> items) {
    public PageResponse {
        if (page < 1 || size < 1 || size > 100 || totalItems < 0) {
            throw new IllegalArgumentException("Metadados de paginação inválidos.");
        }
        // Divisão inteira com teto, sem perda de precisão ou overflow na soma.
        totalPages = totalItems / size + (totalItems % size == 0 ? 0 : 1);
        items = items == null ? List.of() : List.copyOf(items);
    }

    public PageResponse(int page, int size, long totalItems, List<T> items) {
        this(page, size, totalItems, 0, items);
    }

    public boolean hasPrevious() { return page > 1; }
    public boolean hasNext() { return page < totalPages; }
}
