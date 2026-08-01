package dev.bloiko.carsearcher.car;

import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CarIndexMappingTest {

    @Test
    void mappingDeclaresExactlyTheNonVectorFieldsWithSpecTypes() {
        TypeMapping mapping = CarIndexMapping.mapping();
        Map<String, Property> properties = mapping.properties();

        // Matches docs/semantic-car-search/design.md's target "cars" index mapping,
        // minus description_vector (that field is a later task, not this one).
        assertThat(properties.keySet())
                .containsExactlyInAnyOrder("id", "make", "model", "year", "price", "mileage", "description",
                        "photoUrls");

        assertThat(properties.get("id").isKeyword()).isTrue();
        assertThat(properties.get("make").isKeyword()).isTrue();
        assertThat(properties.get("model").isKeyword()).isTrue();
        assertThat(properties.get("year").isInteger()).isTrue();
        assertThat(properties.get("price").isFloat()).isTrue();
        assertThat(properties.get("mileage").isInteger()).isTrue();
        assertThat(properties.get("description").isText()).isTrue();
        assertThat(properties.get("photoUrls").isKeyword()).isTrue();
    }
}
