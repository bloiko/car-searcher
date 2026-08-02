package dev.bloiko.carsearcher.car;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.ingest.GetPipelineRequest;
import org.opensearch.client.opensearch.ingest.GetPipelineResponse;
import org.opensearch.client.opensearch.ingest.PutPipelineRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Ensures the {@value CarIndexBootstrap#CARS_EMBEDDING_PIPELINE} OpenSearch
 * ingest pipeline exists on application startup, creating it if and only if
 * it doesn't already exist. See docs/embedding-indexing/tasks.md (BOH-26),
 * requirements R1.1, R2.1, R2.2.
 *
 * <p>Ordered to run before {@link CarIndexBootstrap}: index creation
 * references this pipeline via {@code defaultPipeline}, so the pipeline must
 * exist first.
 *
 * <p><b>Existence check:</b> although a raw {@code GET _ingest/pipeline/<id>}
 * against OpenSearch itself returns HTTP 404 for an unknown pipeline id, the
 * opensearch-java 2.14.0 typed client's {@code getPipeline()} does NOT
 * surface that as a thrown exception — it returns normally with a
 * {@link GetPipelineResponse} whose {@code result()} map is empty. Existence
 * must therefore be checked via {@code response.result().isEmpty()}, not via
 * catching an exception. See docs/lessons/ for the verified live-cluster
 * diagnosis (a second, distinct instance of this client silently swallowing
 * an error condition instead of throwing).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class EmbeddingPipelineBootstrap implements CommandLineRunner {

    private final OpenSearchClient openSearchClient;
    private final String modelId;

    public EmbeddingPipelineBootstrap(
            OpenSearchClient openSearchClient,
            @Value("${car-searcher.embedding.model-id:}") String modelId) {
        if (modelId == null || modelId.isBlank()) {
            throw new IllegalStateException(
                    "car-searcher.embedding.model-id must be configured (non-blank) to bootstrap the "
                            + CarIndexBootstrap.CARS_EMBEDDING_PIPELINE + " ingest pipeline");
        }
        this.openSearchClient = openSearchClient;
        this.modelId = modelId;
    }

    @Override
    public void run(String... args) {
        try {
            GetPipelineResponse response = openSearchClient.ingest().getPipeline(new GetPipelineRequest.Builder()
                    .id(CarIndexBootstrap.CARS_EMBEDDING_PIPELINE)
                    .build());
            if (response.result().isEmpty()) {
                createPipeline();
            }
            // Otherwise the pipeline already exists; nothing to do.
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to check for \"" + CarIndexBootstrap.CARS_EMBEDDING_PIPELINE + "\" ingest pipeline", e);
        }
    }

    private void createPipeline() {
        try {
            openSearchClient.ingest().putPipeline(new PutPipelineRequest.Builder()
                    .id(CarIndexBootstrap.CARS_EMBEDDING_PIPELINE)
                    .description("Populates description_vector from description via the registered text embedding model.")
                    .processors(p -> p.textEmbedding(te -> te
                            .modelId(modelId)
                            .fieldMap("description", "description_vector")))
                    .build());
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to create \"" + CarIndexBootstrap.CARS_EMBEDDING_PIPELINE + "\" ingest pipeline", e);
        }
    }
}
