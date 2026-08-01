package dev.bloiko.carsearcher.car;

import java.util.List;

/**
 * Car domain model. See docs/semantic-car-search/design.md for the field spec.
 */
public record Car(
        String id,
        String make,
        String model,
        int year,
        float price,
        int mileage,
        String description,
        List<String> photoUrls) {

    public Car {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (make == null || make.isBlank()) {
            throw new IllegalArgumentException("make must not be blank");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("model must not be blank");
        }
        if (price < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
        if (mileage < 0) {
            throw new IllegalArgumentException("mileage must not be negative");
        }
        if (photoUrls == null) {
            photoUrls = List.of();
        }
        for (String photoUrl : photoUrls) {
            if (photoUrl == null || photoUrl.isBlank()) {
                throw new IllegalArgumentException("photoUrls must not contain blank entries");
            }
        }
        photoUrls = List.copyOf(photoUrls);
    }
}
