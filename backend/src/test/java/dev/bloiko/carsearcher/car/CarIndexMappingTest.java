package dev.bloiko.carsearcher.car;

import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CarIndexMappingTest {

    @Test
    void mappingDeclaresExactlyTheSpecFieldsWithSpecTypes() {
        TypeMapping mapping = CarIndexMapping.mapping();
        Map<String, Property> properties = mapping.properties();

        // Matches docs/embedding-indexing/design.md's target "cars" index mapping,
        // including description_vector (BOH-26).
        assertThat(properties.keySet())
                .containsExactlyInAnyOrder("id", "make", "model", "year", "price", "mileage", "transmission",
                        "description", "photoUrls", "description_vector");

        assertThat(properties.get("id").isKeyword()).isTrue();
        assertThat(properties.get("make").isKeyword()).isTrue();
        assertThat(properties.get("model").isKeyword()).isTrue();
        assertThat(properties.get("year").isInteger()).isTrue();
        assertThat(properties.get("price").isFloat()).isTrue();
        assertThat(properties.get("mileage").isInteger()).isTrue();
        assertThat(properties.get("transmission").isKeyword()).isTrue();
        assertThat(properties.get("description").isText()).isTrue();
        assertThat(properties.get("photoUrls").isKeyword()).isTrue();
    }

    @Test
    void descriptionVectorIsAKnnVectorFieldWithDimension384() {
        // R1.2: "THE SYSTEM SHALL declare `description_vector` as a `knn_vector`
        // field with dimension 384 on the `cars` index mapping."
        TypeMapping mapping = CarIndexMapping.mapping();
        Map<String, Property> properties = mapping.properties();

        Property descriptionVector = properties.get("description_vector");
        assertThat(descriptionVector).isNotNull();
        assertThat(descriptionVector.isKnnVector()).isTrue();
        assertThat(descriptionVector.knnVector().dimension()).isEqualTo(384);
    }
}
