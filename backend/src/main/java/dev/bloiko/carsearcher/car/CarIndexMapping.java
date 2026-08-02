package dev.bloiko.carsearcher.car;

import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;

/**
 * Declares the OpenSearch "cars" index mapping, including the description_vector
 * knn_vector field used for semantic search.
 * See docs/semantic-car-search/design.md for the full target mapping.
 */
public final class CarIndexMapping {

    private static final int DESCRIPTION_VECTOR_DIMENSION = 384;

    private CarIndexMapping() {
    }

    public static TypeMapping mapping() {
        return new TypeMapping.Builder()
                .properties("id", keyword())
                .properties("make", keyword())
                .properties("model", keyword())
                .properties("year", integer())
                .properties("price", floatType())
                .properties("mileage", integer())
                .properties("transmission", keyword())
                .properties("description", text())
                .properties("photoUrls", keyword())
                .properties("description_vector", knnVector(DESCRIPTION_VECTOR_DIMENSION))
                .build();
    }

    private static Property keyword() {
        return Property.of(p -> p.keyword(k -> k));
    }

    private static Property integer() {
        return Property.of(p -> p.integer(i -> i));
    }

    private static Property floatType() {
        return Property.of(p -> p.float_(f -> f));
    }

    private static Property text() {
        return Property.of(p -> p.text(t -> t));
    }

    private static Property knnVector(int dimension) {
        return Property.of(p -> p.knnVector(k -> k.dimension(dimension)));
    }
}
