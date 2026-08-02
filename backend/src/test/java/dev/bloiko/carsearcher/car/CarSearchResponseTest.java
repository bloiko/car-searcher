package dev.bloiko.carsearcher.car;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CarSearchResponseTest {

    @Test
    void carSearchResultIncludesPhotoUrlsFromCar() {
        Car car = new Car("car-1", "Toyota", "RAV4", 2021, 27_500f, 18_000, "Automatic", "A reliable family SUV",
                List.of("https://example.com/1.jpg", "https://example.com/2.jpg"));

        CarSearchResponse response = CarSearchResponse.of(List.of(car), 1);

        assertThat(response.results()).hasSize(1);
        assertThat(response.results().get(0).photoUrls())
                .containsExactly("https://example.com/1.jpg", "https://example.com/2.jpg");
    }

    @Test
    void carSearchResultIncludesMileageFromCar() {
        Car car = new Car("car-1", "Toyota", "RAV4", 2021, 27_500f, 42_000, "Automatic", "A reliable family SUV",
                List.of("https://example.com/1.jpg"));

        CarSearchResponse response = CarSearchResponse.of(List.of(car), 1);

        assertThat(response.results()).hasSize(1);
        assertThat(response.results().get(0).mileage()).isEqualTo(42_000);
    }
}
