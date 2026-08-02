package dev.bloiko.carsearcher.car;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CarTest {

    @Test
    void convenienceConstructorDefaultsSourceUrlToNull() {
        // BOH-15: the 9-arg convenience constructor must keep working unchanged so
        // no existing Car-constructing fixture needs to be touched.
        Car car = new Car("id-1", "Toyota", "Corolla", 2020, 18_999f, 32_000, "Automatic",
                "A reliable sedan", List.of());

        assertThat(car.sourceUrl()).isNull();
    }

    @Test
    void canonicalConstructorStoresProvidedSourceUrl() {
        Car car = new Car("id-1", "Toyota", "Corolla", 2020, 18_999f, 32_000, "Automatic",
                "A reliable sedan", List.of(), "https://example.com/listings/1");

        assertThat(car.sourceUrl()).isEqualTo("https://example.com/listings/1");
    }

    @Test
    void rejectsBlankSourceUrl() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Car("id-1", "Toyota", "Corolla", 2020, 18_999f, 32_000, "Automatic",
                        "A reliable sedan", List.of(), "   "))
                .withMessageContaining("sourceUrl");
    }

    @Test
    void rejectsNegativePrice() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Car("id-1", "Toyota", "Corolla", 2020, -1f, 50_000, "Automatic",
                        "A reliable sedan", List.of()))
                .withMessageContaining("price");
    }

    @Test
    void rejectsBlankPhotoUrl() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Car("id-1", "Toyota", "Corolla", 2020, 18_999f, 32_000, "Automatic",
                        "A reliable sedan", List.of("https://example.com/1.jpg", "")))
                .withMessageContaining("photoUrls");
    }

    @Test
    void rejectsBlankTransmission() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Car("id-1", "Toyota", "Corolla", 2020, 18_999f, 32_000, "  ",
                        "A reliable sedan", List.of()))
                .withMessageContaining("transmission");
    }
}
