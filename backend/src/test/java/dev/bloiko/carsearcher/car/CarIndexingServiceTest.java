package dev.bloiko.carsearcher.car;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarIndexingServiceTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @Test
    @SuppressWarnings("unchecked")
    void indexesCarUnderCarsIndexWithCarIdAsDocumentId() throws IOException {
        when(openSearchClient.index(any(IndexRequest.class))).thenReturn(mock(IndexResponse.class));
        Car car = new Car("car-1", "Toyota", "Corolla", 2020, 18_999f, 32_000, "A reliable sedan");
        CarIndexingService service = new CarIndexingService(openSearchClient);

        service.index(car);

        ArgumentCaptor<IndexRequest<Car>> captor = ArgumentCaptor.forClass(IndexRequest.class);
        verify(openSearchClient).index(captor.capture());
        IndexRequest<Car> request = captor.getValue();
        assertThat(request.index()).isEqualTo("cars");
        assertThat(request.id()).isEqualTo("car-1");
        assertThat(request.document()).isEqualTo(car);
    }
}
