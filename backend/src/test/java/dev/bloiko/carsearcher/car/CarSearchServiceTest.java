package dev.bloiko.carsearcher.car;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.MultiMatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.RangeQuery;
import org.opensearch.client.opensearch._types.query_dsl.TermQuery;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarSearchServiceTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @Test
    @SuppressWarnings("unchecked")
    void searchesCarsIndexWithMultiMatchOverDescriptionMakeModelAndMapsHitsToCars() throws IOException {
        Car matchedCar = new Car("car-1", "Toyota", "RAV4", 2021, 27_500f, 18_000, "A reliable family SUV",
                List.of());
        SearchResponse<Car> response = mock(SearchResponse.class);
        when(response.documents()).thenReturn(List.of(matchedCar));
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient);

        List<Car> results = service.search("reliable family suv", null);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(captor.capture(), eq(Car.class));
        SearchRequest request = captor.getValue();
        assertThat(request.index()).containsExactly("cars");
        assertThat(request.query().isBool()).isTrue();
        BoolQuery boolQuery = request.query().bool();
        assertThat(boolQuery.must()).hasSize(1);
        assertThat(boolQuery.must().get(0).isMultiMatch()).isTrue();
        MultiMatchQuery multiMatch = boolQuery.must().get(0).multiMatch();
        assertThat(multiMatch.query()).isEqualTo("reliable family suv");
        assertThat(multiMatch.fields()).containsExactlyInAnyOrder("description", "make", "model");
        assertThat(boolQuery.filter()).isEmpty();

        assertThat(results).containsExactly(matchedCar);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchWithNoFiltersProducesEmptyFilterList() throws IOException {
        SearchResponse<Car> response = mock(SearchResponse.class);
        when(response.documents()).thenReturn(List.of());
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient);
        CarSearchRequest.Filters filters = new CarSearchRequest.Filters(null, null, null, null);

        service.search("reliable family suv", filters);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(captor.capture(), eq(Car.class));
        BoolQuery boolQuery = captor.getValue().query().bool();

        assertThat(boolQuery.filter()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchWithYearMinFilterAddsRangeFilterClauseOnYear() throws IOException {
        SearchResponse<Car> response = mock(SearchResponse.class);
        when(response.documents()).thenReturn(List.of());
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient);
        CarSearchRequest.Filters filters = new CarSearchRequest.Filters(null, 2018, null, null);

        service.search("reliable family suv", filters);

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
        SearchResponse<Car> response = mock(SearchResponse.class);
        when(response.documents()).thenReturn(List.of());
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient);
        CarSearchRequest.Filters filters = new CarSearchRequest.Filters(null, null, 50_000, null);

        service.search("reliable family suv", filters);

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
        SearchResponse<Car> response = mock(SearchResponse.class);
        when(response.documents()).thenReturn(List.of());
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient);
        CarSearchRequest.Filters filters = new CarSearchRequest.Filters(null, null, null, "Toyota");

        service.search("reliable family suv", filters);

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
    void searchWithPriceMaxFilterAddsRangeFilterClauseOnPriceWithoutAffectingMustClause() throws IOException {
        SearchResponse<Car> response = mock(SearchResponse.class);
        when(response.documents()).thenReturn(List.of());
        when(openSearchClient.search(any(SearchRequest.class), eq(Car.class))).thenReturn(response);
        CarSearchService service = new CarSearchService(openSearchClient);
        CarSearchRequest.Filters filters = new CarSearchRequest.Filters(30_000f, null, null, null);

        service.search("reliable family suv", filters);

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(captor.capture(), eq(Car.class));
        SearchRequest request = captor.getValue();

        assertThat(request.query().isBool()).isTrue();
        BoolQuery boolQuery = request.query().bool();

        assertThat(boolQuery.must()).hasSize(1);
        assertThat(boolQuery.must().get(0).isMultiMatch()).isTrue();
        MultiMatchQuery multiMatch = boolQuery.must().get(0).multiMatch();
        assertThat(multiMatch.query()).isEqualTo("reliable family suv");
        assertThat(multiMatch.fields()).containsExactlyInAnyOrder("description", "make", "model");

        assertThat(boolQuery.filter()).hasSize(1);
        Query filterClause = boolQuery.filter().get(0);
        assertThat(filterClause.isRange()).isTrue();
        RangeQuery priceRange = filterClause.range();
        assertThat(priceRange.field()).isEqualTo("price");
        assertThat(priceRange.lte().to(Float.class)).isEqualTo(30_000f);
    }
}
