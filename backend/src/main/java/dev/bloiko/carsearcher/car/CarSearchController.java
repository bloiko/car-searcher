package dev.bloiko.carsearcher.car;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint for searching car listings. See docs/semantic-car-search/design.md
 * — {@code POST /api/cars/search}: placeholder keyword search over the "cars"
 * index (task 3), swapped for real k-NN semantic search once the embedding-model
 * decision is made. Structured filter combination is described in
 * docs/search-filters/design.md — "Query approach".
 */
@RestController
@RequestMapping("/api/cars")
public class CarSearchController {

    private final CarSearchService carSearchService;

    public CarSearchController(CarSearchService carSearchService) {
        this.carSearchService = carSearchService;
    }

    @PostMapping("/search")
    public ResponseEntity<CarSearchResponse> search(@Valid @RequestBody CarSearchRequest request) {
        return ResponseEntity.ok(CarSearchResponse.of(
                carSearchService.search(request.query(), request.filters(), request.sort())));
    }
}
