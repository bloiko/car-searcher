package dev.bloiko.carsearcher.car;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.stereotype.Service;

/**
 * Placeholder keyword search over the "cars" index. See
 * docs/semantic-car-search/design.md — "Phase 1 scaffold vs. real implementation"
 * and docs/semantic-car-search/tasks.md task 3.
 *
 * <p>Runs a plain {@code multi_match} query over description/make/model. This is
 * the scaffold placeholder for the real k-NN semantic search that replaces it
 * once the embedding-model decision (see design.md "Open decision") is made.
 */
@Service
public class CarSearchService {

    private static final String CARS_INDEX = "cars";
    private static final List<String> SEARCH_FIELDS = List.of("description", "make", "model");

    private final OpenSearchClient openSearchClient;

    public CarSearchService(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    public List<Car> search(String query) {
        SearchRequest request = new SearchRequest.Builder()
                .index(CARS_INDEX)
                .query(q -> q.multiMatch(m -> m.query(query).fields(SEARCH_FIELDS)))
                .build();
        try {
            SearchResponse<Car> response = openSearchClient.search(request, Car.class);
            return response.documents();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to search cars index for query \"" + query + "\"", e);
        }
    }
}
