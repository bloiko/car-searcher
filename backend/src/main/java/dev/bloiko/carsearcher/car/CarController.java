package dev.bloiko.carsearcher.car;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint for indexing car listings. See docs/semantic-car-search/design.md
 * — {@code POST /api/cars}: no auth, local-only, used to seed data for local dev.
 */
@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarIndexingService carIndexingService;

    public CarController(CarIndexingService carIndexingService) {
        this.carIndexingService = carIndexingService;
    }

    @PostMapping
    public ResponseEntity<Car> indexCar(@Valid @RequestBody Car car) {
        carIndexingService.index(car);
        return ResponseEntity.status(HttpStatus.CREATED).body(car);
    }
}
