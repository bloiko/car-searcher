package dev.bloiko.carsearcher.car;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.query_dsl.MultiMatchQuery;
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

        List<Car> results = service.search("reliable family suv");

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(openSearchClient).search(captor.capture(), eq(Car.class));
        SearchRequest request = captor.getValue();
        assertThat(request.index()).containsExactly("cars");
        assertThat(request.query().isMultiMatch()).isTrue();
        MultiMatchQuery multiMatch = request.query().multiMatch();
        assertThat(multiMatch.query()).isEqualTo("reliable family suv");
        assertThat(multiMatch.fields()).containsExactlyInAnyOrder("description", "make", "model");

        assertThat(results).containsExactly(matchedCar);
    }
}
