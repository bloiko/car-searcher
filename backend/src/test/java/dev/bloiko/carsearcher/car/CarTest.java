package dev.bloiko.carsearcher.car;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CarTest {

    @Test
    void rejectsNegativePrice() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Car("id-1", "Toyota", "Corolla", 2020, -1f, 50_000, "A reliable sedan"))
                .withMessageContaining("price");
    }
}
