package dev.bloiko.carsearcher.car;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CarTest {

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
