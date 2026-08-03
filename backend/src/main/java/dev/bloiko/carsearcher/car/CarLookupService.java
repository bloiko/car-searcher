package dev.bloiko.carsearcher.car;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.GetRequest;
import org.opensearch.client.opensearch.core.GetResponse;
import org.springframework.stereotype.Service;

/**
 * Point lookup of a single {@link Car} by id in the "cars" OpenSearch index.
 * See docs/listing-detail-page/design.md — "Backend endpoint". Kept separate
 * from {@link CarSearchService}, which is specifically about *searching*, not
 * point lookups.
 */
@Service
public class CarLookupService {

    private static final String CARS_INDEX = "cars";

    private final OpenSearchClient openSearchClient;

    public CarLookupService(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    public Optional<Car> findById(String id) {
        GetRequest request = new GetRequest.Builder().index(CARS_INDEX).id(id).build();
        try {
            GetResponse<Car> response = openSearchClient.get(request, Car.class);
            return response.found() ? Optional.of(response.source()) : Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to look up car \"" + id + "\" in \"" + CARS_INDEX + "\" index", e);
        }
    }
}
