package dev.bloiko.carsearcher.car;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Ensures the "cars" OpenSearch index exists on application startup, creating it
 * from {@link CarIndexMapping#mapping()} if and only if it doesn't already exist.
 * See docs/index-bootstrap/tasks.md (BOH-20).
 *
 * <p>Ordered to run after {@link EmbeddingPipelineBootstrap}: index creation
 * references {@link #CARS_EMBEDDING_PIPELINE} via {@code defaultPipeline}, so
 * the pipeline must already exist on the cluster.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class CarIndexBootstrap implements CommandLineRunner {

    private static final String CARS_INDEX = "cars";

    /**
     * Shared with {@link EmbeddingPipelineBootstrap}, which must create this
     * pipeline before this class's {@code CreateIndexRequest} references it via
     * {@code defaultPipeline}. Package-private so both bootstraps use one
     * literal instead of two independently-drifting copies.
     */
    static final String CARS_EMBEDDING_PIPELINE = "cars-embedding-pipeline";

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
                        .settings(s -> s.knn(true).defaultPipeline(CARS_EMBEDDING_PIPELINE))
                        .build());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to bootstrap \"" + CARS_INDEX + "\" index", e);
        }
    }
}
