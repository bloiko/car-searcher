package dev.bloiko.carsearcher.car;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/cars/search}. See
 * docs/semantic-car-search/design.md — "API".
 *
 * @param query free-text search query, ranked by semantic similarity against
 *     each listing's {@code description_vector} (see
 *     docs/semantic-knn-search/design.md).
 * @param filters structured filters to combine with {@code query}, applied in
 *     {@code filter} context (unscored, exact) alongside the semantic ranking
 *     of {@code query} (BOH-14/BOH-22). May be {@code null}.
 * @param sort result ordering; {@code "price_asc"} or {@code "mileage_asc"}, or
 *     {@code null} for the default relevance ranking. See docs/search-sort/tasks.md.
 */
public record CarSearchRequest(@NotBlank String query, Filters filters, String sort) {

    public CarSearchRequest {
        if (sort != null && !sort.equals("price_asc") && !sort.equals("mileage_asc")) {
            throw new IllegalArgumentException("sort must be \"price_asc\" or \"mileage_asc\"");
        }
    }

    /**
     * Structured filters, applied in {@code filter} context alongside the
     * semantic ranking of {@code query} (see {@link CarSearchRequest}).
     *
     * @param priceMax maximum price, inclusive. May be {@code null}.
     * @param yearMin minimum model year, inclusive. May be {@code null}.
     * @param mileageMax maximum mileage, inclusive. May be {@code null}.
     * @param make exact match against {@link Car#make}. May be {@code null}.
     * @param model exact match against {@link Car#model}. May be {@code null}.
     * @param transmission exact match against {@link Car#transmission}. May be {@code null}.
     */
    public record Filters(
            Float priceMax, Integer yearMin, Integer mileageMax, String make, String model, String transmission) {

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
            this(priceMax, null, null, null, null, null);
        }
    }
}
