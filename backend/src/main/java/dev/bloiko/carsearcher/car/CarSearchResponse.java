package dev.bloiko.carsearcher.car;

import java.util.List;

/**
 * Response body for {@code POST /api/cars/search}. See
 * docs/semantic-car-search/design.md — "API": {@code { "results": [...] } }.
 *
 * <p>Wraps the results in an object (rather than a bare array) per the documented
 * contract, and an empty {@code results} list is a normal 200 response, not an
 * error — see requirements.md R1.2.
 */
public record CarSearchResponse(List<CarSearchResult> results) {

    public CarSearchResponse {
        results = List.copyOf(results);
    }

    static CarSearchResponse of(List<Car> cars) {
        return new CarSearchResponse(cars.stream().map(CarSearchResult::from).toList());
    }

    /**
     * A single search result. Per design.md's "API" section and requirements.md
     * R3.1, this is deliberately narrower than {@link Car}.
     */
    public record CarSearchResult(
            String id, String make, String model, int year, float price, int mileage, String description,
            List<String> photoUrls) {

        public CarSearchResult {
            photoUrls = List.copyOf(photoUrls);
        }

        static CarSearchResult from(Car car) {
            return new CarSearchResult(
                    car.id(), car.make(), car.model(), car.year(), car.price(), car.mileage(), car.description(),
                    car.photoUrls());
        }
    }
}
