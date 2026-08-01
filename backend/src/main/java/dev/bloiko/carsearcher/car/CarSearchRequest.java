package dev.bloiko.carsearcher.car;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/cars/search}. See
 * docs/semantic-car-search/design.md — "API".
 *
 * @param query free-text search query.
 * @param filters structured filters to combine with {@code query}. Accepted but
 *     not yet applied — wiring real filter behavior (R2.1) is not part of the
 *     placeholder keyword-search task (task 3, Reqs R1.1/R1.2/R4.1); it's left
 *     for the k-NN backlog item "Combine k-NN relevance with structured filters"
 *     in docs/semantic-car-search/tasks.md. May be {@code null}.
 */
public record CarSearchRequest(@NotBlank String query, Filters filters) {

    /**
     * Structured filters. Only {@code priceMax} is defined per design.md's
     * example payload; not yet applied to the query (see {@link CarSearchRequest}).
     *
     * @param priceMax maximum price, inclusive. May be {@code null}.
     */
    public record Filters(Float priceMax) {
    }
}
