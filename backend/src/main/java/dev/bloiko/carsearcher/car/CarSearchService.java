package dev.bloiko.carsearcher.car;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Semantic search over the "cars" index. See docs/semantic-knn-search/design.md
 * — "Query approach" and docs/semantic-knn-search/tasks.md task 1, and
 * docs/search-filters/design.md — "Query approach" for the filter combination
 * added on top of it.
 *
 * <p>Runs a {@code bool} query: a {@code neural} query against
 * {@code description_vector} in {@code must} (ranks results by semantic
 * similarity to the free-text query using the configured embedding model),
 * plus one {@code filter}-context clause per provided
 * {@link CarSearchRequest.Filters} field (constrains the result set without
 * affecting score).
 */
@Service
public class CarSearchService {

    private static final String CARS_INDEX = "cars";
    private static final int K = 50;

    private final OpenSearchClient openSearchClient;
    private final String modelId;

    public CarSearchService(
            OpenSearchClient openSearchClient, @Value("${car-searcher.embedding.model-id:}") String modelId) {
        this.openSearchClient = openSearchClient;
        this.modelId = modelId;
    }

    public List<Car> search(String query, CarSearchRequest.Filters filters, String sort) {
        List<Query> filterClauses = buildFilterClauses(filters);
        List<SortOptions> sortClauses = buildSortClauses(sort);
        SearchRequest request = new SearchRequest.Builder()
                .index(CARS_INDEX)
                .query(q -> q.bool(b -> b
                        .must(m -> m.neural(
                                n -> n.field("description_vector").queryText(query).modelId(modelId).k(K)))
                        .filter(filterClauses)))
                .sort(sortClauses)
                .build();
        try {
            SearchResponse<Car> response = openSearchClient.search(request, Car.class);
            // Deliberately not response.documents(): confirmed against a real cluster
            // (no mock can catch this) that it returns an empty list even when hits
            // genuinely have a non-null source -- see docs/lessons/. Map hits by hand.
            return response.hits().hits().stream().map(Hit::source).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to search cars index for query \"" + query + "\"", e);
        }
    }

    private static List<SortOptions> buildSortClauses(String sort) {
        if (sort == null) {
            return List.of();
        }
        String field = switch (sort) {
            case "price_asc" -> "price";
            case "mileage_asc" -> "mileage";
            default -> throw new IllegalArgumentException("sort must be \"price_asc\" or \"mileage_asc\"");
        };
        return List.of(SortOptions.of(s -> s.field(f -> f.field(field).order(SortOrder.Asc))));
    }

    private static List<Query> buildFilterClauses(CarSearchRequest.Filters filters) {
        if (filters == null) {
            return List.of();
        }
        List<Query> clauses = new ArrayList<>();
        if (filters.priceMax() != null) {
            clauses.add(Query.of(q -> q.range(r -> r.field("price").lte(JsonData.of(filters.priceMax())))));
        }
        if (filters.yearMin() != null) {
            clauses.add(Query.of(q -> q.range(r -> r.field("year").gte(JsonData.of(filters.yearMin())))));
        }
        if (filters.mileageMax() != null) {
            clauses.add(Query.of(q -> q.range(r -> r.field("mileage").lte(JsonData.of(filters.mileageMax())))));
        }
        if (filters.make() != null) {
            clauses.add(Query.of(q -> q.term(t -> t.field("make").value(FieldValue.of(filters.make())))));
        }
        if (filters.model() != null) {
            clauses.add(Query.of(q -> q.term(t -> t.field("model").value(FieldValue.of(filters.model())))));
        }
        if (filters.transmission() != null) {
            clauses.add(
                    Query.of(q -> q.term(t -> t.field("transmission").value(FieldValue.of(filters.transmission())))));
        }
        return clauses;
    }
}
