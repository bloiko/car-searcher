package dev.bloiko.carsearcher.car;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.springframework.stereotype.Service;

/**
 * Indexes {@link Car} documents into the OpenSearch "cars" index.
 * See docs/semantic-car-search/design.md — POST /api/cars.
 */
@Service
public class CarIndexingService {

    private static final String CARS_INDEX = "cars";

    private final OpenSearchClient openSearchClient;

    public CarIndexingService(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    public void index(Car car) {
        IndexRequest<Car> request = new IndexRequest.Builder<Car>()
                .index(CARS_INDEX)
                .id(car.id())
                .document(car)
                .build();
        try {
            openSearchClient.index(request);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to index car " + car.id(), e);
        }
    }
}
