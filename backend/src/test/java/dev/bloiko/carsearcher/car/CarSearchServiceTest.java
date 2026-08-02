package dev.bloiko.carsearcher.car;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldSort;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.NeuralQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.RangeQuery;
import org.opensearch.client.opensearch._types.query_dsl.TermQuery;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.opensearch.client.opensearch.core.search.TotalHits;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarSearchServiceTest {

    private static final String MODEL_ID = "test-model-id";

    @Mock
    private OpenSearchClient openSearchClient;

    @Test
    @SuppressWarnings("unchecked")
    void searchUsesNeuralQueryAgainstDescriptionVectorWithConfiguredModelIdAndK() throws IOException {
        Car matchedCar = new Car("car-1", "Toyota", "RAV4", 2021, 27_500f, 18_000, "Automatic",
                "A reliable family SUV", List.of());
        SearchResponse<Car> response = mockResponseWithHits(List.of(matchedCar));
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient, MODEL_ID);

        CarSearchService.SearchResult result = service.search("reliable family suv", null, null, null, null);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(captor.capture(), eq(Car.class));
        SearchRequest request = captor.getValue();
        assertThat(request.index()).containsExactly("cars");
        assertThat(request.query().isBool()).isTrue();
        BoolQuery boolQuery = request.query().bool();
        assertThat(boolQuery.must()).hasSize(1);
        assertThat(boolQuery.must().get(0).isNeural()).isTrue();
        NeuralQuery neuralQuery = boolQuery.must().get(0).neural();
        assertThat(neuralQuery.field()).isEqualTo("description_vector");
        assertThat(neuralQuery.queryText()).isEqualTo("reliable family suv");
        assertThat(neuralQuery.modelId()).isEqualTo(MODEL_ID);
        assertThat(neuralQuery.k()).isEqualTo(50);
        assertThat(boolQuery.filter()).isEmpty();

        assertThat(result.cars()).containsExactly(matchedCar);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchWithNoFiltersProducesEmptyFilterList() throws IOException {
        SearchResponse<Car> response = mockResponseWithHits(List.of());
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient, MODEL_ID);
        CarSearchRequest.Filters filters = new CarSearchRequest.Filters(null, null, null, null, null, null);

        service.search("reliable family suv", filters, null, null, null);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(captor.capture(), eq(Car.class));
        BoolQuery boolQuery = captor.getValue().query().bool();

        assertThat(boolQuery.filter()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchWithYearMinFilterAddsRangeFilterClauseOnYear() throws IOException {
        SearchResponse<Car> response = mockResponseWithHits(List.of());
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient, MODEL_ID);
        CarSearchRequest.Filters filters = new CarSearchRequest.Filters(null, 2018, null, null, null, null);

        service.search("reliable family suv", filters, null, null, null);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(captor.capture(), eq(Car.class));
        BoolQuery boolQuery = captor.getValue().query().bool();

        assertThat(boolQuery.filter()).hasSize(1);
        Query filterClause = boolQuery.filter().get(0);
        assertThat(filterClause.isRange()).isTrue();
        RangeQuery yearRange = filterClause.range();
        assertThat(yearRange.field()).isEqualTo("year");
        assertThat(yearRange.gte().to(Integer.class)).isEqualTo(2018);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchWithMileageMaxFilterAddsRangeFilterClauseOnMileage() throws IOException {
        SearchResponse<Car> response = mockResponseWithHits(List.of());
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient, MODEL_ID);
        CarSearchRequest.Filters filters = new CarSearchRequest.Filters(null, null, 50_000, null, null, null);

        service.search("reliable family suv", filters, null, null, null);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(captor.capture(), eq(Car.class));
        BoolQuery boolQuery = captor.getValue().query().bool();

        assertThat(boolQuery.filter()).hasSize(1);
        Query filterClause = boolQuery.filter().get(0);
        assertThat(filterClause.isRange()).isTrue();
        RangeQuery mileageRange = filterClause.range();
        assertThat(mileageRange.field()).isEqualTo("mileage");
        assertThat(mileageRange.lte().to(Integer.class)).isEqualTo(50_000);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchWithMakeFilterAddsTermFilterClauseOnMake() throws IOException {
        SearchResponse<Car> response = mockResponseWithHits(List.of());
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient, MODEL_ID);
        CarSearchRequest.Filters filters = new CarSearchRequest.Filters(null, null, null, "Toyota", null, null);

        service.search("reliable family suv", filters, null, null, null);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(captor.capture(), eq(Car.class));
        BoolQuery boolQuery = captor.getValue().query().bool();

        assertThat(boolQuery.filter()).hasSize(1);
        Query filterClause = boolQuery.filter().get(0);
        assertThat(filterClause.isTerm()).isTrue();
        TermQuery makeTerm = filterClause.term();
        assertThat(makeTerm.field()).isEqualTo("make");
        assertThat(makeTerm.value().stringValue()).isEqualTo("Toyota");
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchWithModelFilterAddsTermFilterClauseOnModel() throws IOException {
        SearchResponse<Car> response = mockResponseWithHits(List.of());
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient, MODEL_ID);
        CarSearchRequest.Filters filters = new CarSearchRequest.Filters(null, null, null, null, "RAV4", null);

        service.search("reliable family suv", filters, null, null, null);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(captor.capture(), eq(Car.class));
        BoolQuery boolQuery = captor.getValue().query().bool();

        assertThat(boolQuery.filter()).hasSize(1);
        Query filterClause = boolQuery.filter().get(0);
        assertThat(filterClause.isTerm()).isTrue();
        TermQuery modelTerm = filterClause.term();
        assertThat(modelTerm.field()).isEqualTo("model");
        assertThat(modelTerm.value().stringValue()).isEqualTo("RAV4");
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchWithTransmissionFilterAddsTermFilterClauseOnTransmission() throws IOException {
        SearchResponse<Car> response = mockResponseWithHits(List.of());
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient, MODEL_ID);
        CarSearchRequest.Filters filters = new CarSearchRequest.Filters(null, null, null, null, null, "Automatic");

        service.search("reliable family suv", filters, null, null, null);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(captor.capture(), eq(Car.class));
        BoolQuery boolQuery = captor.getValue().query().bool();

        assertThat(boolQuery.filter()).hasSize(1);
        Query filterClause = boolQuery.filter().get(0);
        assertThat(filterClause.isTerm()).isTrue();
        TermQuery transmissionTerm = filterClause.term();
        assertThat(transmissionTerm.field()).isEqualTo("transmission");
        assertThat(transmissionTerm.value().stringValue()).isEqualTo("Automatic");
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchWithPriceMaxFilterAddsRangeFilterClauseOnPriceWithoutAffectingMustClause() throws IOException {
        SearchResponse<Car> response = mockResponseWithHits(List.of());
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient, MODEL_ID);
        CarSearchRequest.Filters filters = new CarSearchRequest.Filters(30_000f, null, null, null, null, null);

        service.search("reliable family suv", filters, null, null, null);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(captor.capture(), eq(Car.class));
        SearchRequest request = captor.getValue();

        assertThat(request.query().isBool()).isTrue();
        BoolQuery boolQuery = request.query().bool();

        assertThat(boolQuery.must()).hasSize(1);
        assertThat(boolQuery.must().get(0).isNeural()).isTrue();
        NeuralQuery neuralQuery = boolQuery.must().get(0).neural();
        assertThat(neuralQuery.field()).isEqualTo("description_vector");
        assertThat(neuralQuery.queryText()).isEqualTo("reliable family suv");
        assertThat(neuralQuery.modelId()).isEqualTo(MODEL_ID);
        assertThat(neuralQuery.k()).isEqualTo(50);

        assertThat(boolQuery.filter()).hasSize(1);
        Query filterClause = boolQuery.filter().get(0);
        assertThat(filterClause.isRange()).isTrue();
        RangeQuery priceRange = filterClause.range();
        assertThat(priceRange.field()).isEqualTo("price");
        assertThat(priceRange.lte().to(Float.class)).isEqualTo(30_000f);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchWithPriceAscSortAddsAscendingPriceSortClause() throws IOException {
        SearchResponse<Car> response = mockResponseWithHits(List.of());
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient, MODEL_ID);

        service.search("reliable family suv", null, "price_asc", null, null);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(captor.capture(), eq(Car.class));
        SearchRequest request = captor.getValue();

        assertThat(request.sort()).hasSize(1);
        SortOptions sortOption = request.sort().get(0);
        assertThat(sortOption.isField()).isTrue();
        FieldSort fieldSort = sortOption.field();
        assertThat(fieldSort.field()).isEqualTo("price");
        assertThat(fieldSort.order()).isEqualTo(SortOrder.Asc);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchWithMileageAscSortAddsAscendingMileageSortClause() throws IOException {
        SearchResponse<Car> response = mockResponseWithHits(List.of());
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient, MODEL_ID);

        service.search("reliable family suv", null, "mileage_asc", null, null);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(captor.capture(), eq(Car.class));
        SearchRequest request = captor.getValue();

        assertThat(request.sort()).hasSize(1);
        SortOptions sortOption = request.sort().get(0);
        assertThat(sortOption.isField()).isTrue();
        FieldSort fieldSort = sortOption.field();
        assertThat(fieldSort.field()).isEqualTo("mileage");
        assertThat(fieldSort.order()).isEqualTo(SortOrder.Asc);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchWithNullSortProducesNoSortClause() throws IOException {
        SearchResponse<Car> response = mockResponseWithHits(List.of());
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient, MODEL_ID);

        service.search("reliable family suv", null, null, null, null);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(captor.capture(), eq(Car.class));

        assertThat(captor.getValue().sort()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchAppliesFromAndSizeComputedFromPageAndPageSize() throws IOException {
        SearchResponse<Car> response = mockResponseWithHits(List.of());
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient, MODEL_ID);

        service.search("reliable family suv", null, null, 2, 10);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(captor.capture(), eq(Car.class));
        SearchRequest request = captor.getValue();

        assertThat(request.from()).isEqualTo(20);
        assertThat(request.size()).isEqualTo(10);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchWithNullPageAndPageSizeDefaultsFromToZeroAndSizeToTwenty() throws IOException {
        SearchResponse<Car> response = mockResponseWithHits(List.of());
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient, MODEL_ID);

        service.search("reliable family suv", null, null, null, null);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(captor.capture(), eq(Car.class));
        SearchRequest request = captor.getValue();

        assertThat(request.from()).isEqualTo(0);
        assertThat(request.size()).isEqualTo(20);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchReturnsTotalFromResponseHitsTotal() throws IOException {
        SearchResponse<Car> response = mockResponseWithHits(List.of(), 137);
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient, MODEL_ID);

        CarSearchService.SearchResult result = service.search("reliable family suv", null, null, null, null);

        assertThat(result.total()).isEqualTo(137L);
    }

    /**
     * CarSearchService deliberately does NOT call {@link SearchResponse#documents()}
     * -- confirmed against a real OpenSearch cluster that it silently returns an
     * empty list even when hits have a genuinely non-null source (a real bug/gotcha
     * in this client version, invisible to any mock). Mocks must match that: stub
     * {@code hits().hits()} with {@link Hit#source()}, never {@code documents()}
     * directly, or a test can pass while the mocked shape no longer matches what
     * the production code actually calls.
     */
    @SuppressWarnings("unchecked")
    private static SearchResponse<Car> mockResponseWithHits(List<Car> cars) {
        return mockResponseWithHits(cars, 0);
    }

    /**
     * Overload that also stubs {@code hits().total().value()} for tests asserting on
     * the {@code total} extraction. The {@code total()}/{@code value()} stubs are
     * {@code lenient} because most callers of this helper don't care about {@code total}
     * -- without {@code lenient}, MockitoExtension's default strict-stubbing would fail
     * every other test sharing this helper with {@code UnnecessaryStubbingException} the
     * moment those tests don't exercise {@code total()}.
     */
    @SuppressWarnings("unchecked")
    private static SearchResponse<Car> mockResponseWithHits(List<Car> cars, long total) {
        SearchResponse<Car> response = mock(SearchResponse.class);
        HitsMetadata<Car> hitsMetadata = mock(HitsMetadata.class);
        List<Hit<Car>> hits = cars.stream()
                .map(car -> {
                    Hit<Car> hit = mock(Hit.class);
                    when(hit.source()).thenReturn(car);
                    return hit;
                })
                .toList();
        when(hitsMetadata.hits()).thenReturn(hits);
        TotalHits totalHits = mock(TotalHits.class);
        lenient().when(totalHits.value()).thenReturn(total);
        lenient().when(hitsMetadata.total()).thenReturn(totalHits);
        when(response.hits()).thenReturn(hitsMetadata);
        return response;
    }
}
