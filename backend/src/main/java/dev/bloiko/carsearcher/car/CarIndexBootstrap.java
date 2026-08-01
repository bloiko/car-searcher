package dev.bloiko.carsearcher.car;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Ensures the "cars" OpenSearch index exists on application startup, creating it
 * from {@link CarIndexMapping#mapping()} if and only if it doesn't already exist.
 * See docs/index-bootstrap/tasks.md (BOH-20).
 */
@Component
public class CarIndexBootstrap implements CommandLineRunner {

    private static final String CARS_INDEX = "cars";

    private final OpenSearchClient openSearchClient;

    public CarIndexBootstrap(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    @Override
    public void run(String... args) {
        try {
            boolean exists = openSearchClient.indices()
                    .exists(new ExistsRequest.Builder().index(CARS_INDEX).build())
                    .value();
            if (!exists) {
                openSearchClient.indices().create(new CreateIndexRequest.Builder()
                        .index(CARS_INDEX)
                        .mappings(CarIndexMapping.mapping())
                        .build());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to bootstrap \"" + CARS_INDEX + "\" index", e);
        }
    }
}
