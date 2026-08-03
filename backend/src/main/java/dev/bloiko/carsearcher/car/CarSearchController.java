package dev.bloiko.carsearcher.car;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint for searching car listings and looking up a single listing by
 * id. See docs/semantic-car-search/design.md — {@code POST /api/cars/search}:
 * placeholder keyword search over the "cars" index (task 3), swapped for real
 * k-NN semantic search once the embedding-model decision is made. Structured
 * filter combination is described in docs/search-filters/design.md — "Query
 * approach". {@code GET /api/cars/{id}} is described in
 * docs/listing-detail-page/design.md — "Backend endpoint".
 */
@RestController
@RequestMapping("/api/cars")
public class CarSearchController {

    private final CarSearchService carSearchService;
    private final CarLookupService carLookupService;

    public CarSearchController(CarSearchService carSearchService, CarLookupService carLookupService) {
        this.carSearchService = carSearchService;
        this.carLookupService = carLookupService;
    }

    @PostMapping("/search")
    public ResponseEntity<CarSearchResponse> search(@Valid @RequestBody CarSearchRequest request) {
        CarSearchService.SearchResult result = carSearchService.search(
                request.query(), request.filters(), request.sort(), request.page(), request.pageSize());
        return ResponseEntity.ok(CarSearchResponse.of(result.cars(), result.total()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Car> get(@PathVariable String id) {
        return carLookupService
                .findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
