package dev.bloiko.carsearcher.car;

import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;

/**
 * Declares the OpenSearch "cars" index mapping for the non-vector fields.
 * See docs/semantic-car-search/design.md for the full target mapping;
 * description_vector is added in a later task.
 */
public final class CarIndexMapping {

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
                .properties("description", text())
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
}
