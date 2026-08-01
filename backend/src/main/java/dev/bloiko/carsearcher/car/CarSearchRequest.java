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
     * Structured filters. Not yet applied to the query (see {@link CarSearchRequest});
     * wiring these into search behavior is task 2 per docs/search-filters/tasks.md.
     *
     * @param priceMax maximum price, inclusive. May be {@code null}.
     * @param yearMin minimum model year, inclusive. May be {@code null}.
     * @param mileageMax maximum mileage, inclusive. May be {@code null}.
     * @param make exact match against {@link Car#make}. May be {@code null}.
     */
    public record Filters(Float priceMax, Integer yearMin, Integer mileageMax, String make) {

        public Filters {
            if (priceMax != null && priceMax < 0) {
                throw new IllegalArgumentException("priceMax must not be negative");
            }
            if (mileageMax != null && mileageMax < 0) {
                throw new IllegalArgumentException("mileageMax must not be negative");
            }
        }

        /**
         * Convenience constructor for the common case of filtering on price alone.
         *
         * @param priceMax maximum price, inclusive. May be {@code null}.
         */
        public Filters(Float priceMax) {
            this(priceMax, null, null, null);
        }
    }
}
