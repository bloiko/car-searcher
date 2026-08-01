package dev.bloiko.carsearcher.car;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.CreateIndexResponse;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.opensearch.indices.OpenSearchIndicesClient;
import org.opensearch.client.transport.endpoints.BooleanResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RED-phase test for BOH-20: startup bootstrap of the "cars" OpenSearch index.
 * See docs/index-bootstrap/tasks.md for the acceptance criterion.
 */
@ExtendWith(MockitoExtension.class)
class CarIndexBootstrapTest {

    @Mock
    private OpenSearchClient openSearchClient;

    @Mock
    private OpenSearchIndicesClient indicesClient;

    @Test
    @SuppressWarnings("unchecked")
    void createsCarsIndexWithCarIndexMappingWhenItDoesNotAlreadyExist() throws IOException {
        when(openSearchClient.indices()).thenReturn(indicesClient);
        when(indicesClient.exists(any(ExistsRequest.class))).thenReturn(new BooleanResponse(false));
        when(indicesClient.create(any(CreateIndexRequest.class))).thenReturn(mock(CreateIndexResponse.class));
        CarIndexBootstrap bootstrap = new CarIndexBootstrap(openSearchClient);

        bootstrap.run();

        ArgumentCaptor<ExistsRequest> existsCaptor = ArgumentCaptor.forClass(ExistsRequest.class);
        verify(indicesClient).exists(existsCaptor.capture());
        assertThat(existsCaptor.getValue().index()).containsExactly("cars");

        ArgumentCaptor<CreateIndexRequest> createCaptor = ArgumentCaptor.forClass(CreateIndexRequest.class);
        verify(indicesClient).create(createCaptor.capture());
        CreateIndexRequest createRequest = createCaptor.getValue();
        assertThat(createRequest.index()).isEqualTo("cars");
        assertThat(createRequest.mappings()).isNotNull();
        assertThat(createRequest.mappings().properties().keySet())
                .containsExactlyInAnyOrderElementsOf(CarIndexMapping.mapping().properties().keySet());
    }

    @Test
    @SuppressWarnings("unchecked")
    void doesNotCreateCarsIndexWhenItAlreadyExists() throws IOException {
        when(openSearchClient.indices()).thenReturn(indicesClient);
        when(indicesClient.exists(any(ExistsRequest.class))).thenReturn(new BooleanResponse(true));
        CarIndexBootstrap bootstrap = new CarIndexBootstrap(openSearchClient);

        bootstrap.run();

        verify(indicesClient, never()).create(any(CreateIndexRequest.class));
    }
}
