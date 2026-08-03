package dev.bloiko.carsearcher.car;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.GetRequest;
import org.opensearch.client.opensearch.core.GetResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * See docs/listing-detail-page/design.md — "Backend endpoint" and
 * requirements.md R1.1/R1.2. {@link CarLookupService#findById(String)} maps
 * an OpenSearch {@code GetResponse.found() == true} to
 * {@code Optional.of(response.source())} and {@code false} to
 * {@link Optional#empty()} -- the controller then maps that Optional to
 * 200/404 (see CarSearchControllerTest).
 */
@ExtendWith(MockitoExtension.class)
class CarLookupServiceTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @Test
    @SuppressWarnings("unchecked")
    void findByIdReturnsCarWhenDocumentFound() throws IOException {
        Car car = new Car("car-1", "Toyota", "RAV4", 2021, 27_500f, 18_000, "Automatic",
                "A reliable family SUV", List.of());
        GetResponse<Car> response = mock(GetResponse.class);
        when(response.found()).thenReturn(true);
        when(response.source()).thenReturn(car);
        when(openSearchClient.get(any(GetRequest.class), eq(Car.class))).thenReturn(response);
        CarLookupService service = new CarLookupService(openSearchClient);

        Optional<Car> result = service.findById("car-1");

        assertThat(result).contains(car);
        ArgumentCaptor<GetRequest> captor = ArgumentCaptor.forClass(GetRequest.class);
        verify(openSearchClient).get(captor.capture(), eq(Car.class));
        assertThat(captor.getValue().index()).isEqualTo("cars");
        assertThat(captor.getValue().id()).isEqualTo("car-1");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByIdReturnsEmptyWhenDocumentNotFound() throws IOException {
        GetResponse<Car> response = mock(GetResponse.class);
        when(response.found()).thenReturn(false);
        when(openSearchClient.get(any(GetRequest.class), eq(Car.class))).thenReturn(response);
        CarLookupService service = new CarLookupService(openSearchClient);

        Optional<Car> result = service.findById("missing-id");

        assertThat(result).isEmpty();
    }
}
