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
import org.springframework.stereotype.Service;

/**
 * Keyword search over the "cars" index. See docs/semantic-car-search/design.md
 * — "Phase 1 scaffold vs. real implementation" and docs/semantic-car-search/tasks.md
 * task 3, and docs/search-filters/design.md — "Query approach" for the filter
 * combination added on top of it.
 *
 * <p>Runs a {@code bool} query: a {@code multi_match} over description/make/model
 * in {@code must} (drives relevance scoring), plus one {@code filter}-context
 * clause per provided {@link CarSearchRequest.Filters} field (constrains the
 * result set without affecting score). This is still the scaffold placeholder
 * for the real k-NN semantic search that replaces the {@code multi_match} leg
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

    public List<Car> search(String query, CarSearchRequest.Filters filters, String sort) {
        List<Query> filterClauses = buildFilterClauses(filters);
        List<SortOptions> sortClauses = buildSortClauses(sort);
        SearchRequest request = new SearchRequest.Builder()
                .index(CARS_INDEX)
                .query(q -> q.bool(b -> b
                        .must(m -> m.multiMatch(mm -> mm.query(query).fields(SEARCH_FIELDS)))
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
