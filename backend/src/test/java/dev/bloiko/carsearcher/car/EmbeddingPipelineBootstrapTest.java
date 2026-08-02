package dev.bloiko.carsearcher.car;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.ingest.GetPipelineRequest;
import org.opensearch.client.opensearch.ingest.GetPipelineResponse;
import org.opensearch.client.opensearch.ingest.OpenSearchIngestClient;
import org.opensearch.client.opensearch.ingest.Pipeline;
import org.opensearch.client.opensearch.ingest.Processor;
import org.opensearch.client.opensearch.ingest.PutPipelineRequest;
import org.opensearch.client.opensearch.ingest.PutPipelineResponse;
import org.opensearch.client.opensearch.ingest.TextEmbeddingProcessor;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * RED-phase test for BOH-26 task 2: startup bootstrap of the
 * "cars-embedding-pipeline" OpenSearch ingest pipeline.
 * See docs/embedding-indexing/tasks.md / design.md ("Ingest pipeline",
 * "Testing strategy") for the acceptance criteria (R1.1, R2.1, R2.2).
 *
 * <p>Mocking style mirrors {@link CarIndexBootstrapTest}. Verified directly
 * against a real local OpenSearch 2.16.0 cluster: unlike a raw
 * {@code GET _ingest/pipeline/<id>}, which genuinely returns HTTP 404 for an
 * unknown pipeline id, the opensearch-java 2.14.0 typed client's
 * {@code getPipeline()} does NOT throw for that case — it returns normally
 * with a {@link GetPipelineResponse} whose {@code result()} map is empty. So
 * "doesn't exist" is modeled here as an empty-map response, not a thrown
 * exception (see docs/lessons/ for the write-up — this is the second
 * instance of an opensearch-java convenience method silently swallowing an
 * error condition instead of throwing, after {@code SearchResponse.documents()}).
 */
@ExtendWith(MockitoExtension.class)
class EmbeddingPipelineBootstrapTest {

    private static final String MODEL_ID = "abc123model";
    private static final String PIPELINE_ID = "cars-embedding-pipeline";

    @Mock
    private OpenSearchClient openSearchClient;

    @Mock
    private OpenSearchIngestClient ingestClient;

    @Test
    @SuppressWarnings("unchecked")
    void createsCarsEmbeddingPipelineWithConfiguredModelIdWhenItDoesNotAlreadyExist() throws IOException {
        when(openSearchClient.ingest()).thenReturn(ingestClient);
        when(ingestClient.getPipeline(any(GetPipelineRequest.class))).thenReturn(emptyGetPipelineResponse());
        when(ingestClient.putPipeline(any(PutPipelineRequest.class))).thenReturn(mock(PutPipelineResponse.class));
        EmbeddingPipelineBootstrap bootstrap = new EmbeddingPipelineBootstrap(openSearchClient, MODEL_ID);

        bootstrap.run();

        ArgumentCaptor<GetPipelineRequest> getCaptor = ArgumentCaptor.forClass(GetPipelineRequest.class);
        verify(ingestClient).getPipeline(getCaptor.capture());
        assertThat(getCaptor.getValue().id()).isEqualTo(PIPELINE_ID);

        ArgumentCaptor<PutPipelineRequest> putCaptor = ArgumentCaptor.forClass(PutPipelineRequest.class);
        verify(ingestClient).putPipeline(putCaptor.capture());
        PutPipelineRequest putRequest = putCaptor.getValue();
        assertThat(putRequest.id()).isEqualTo(PIPELINE_ID);
        assertThat(putRequest.processors()).hasSize(1);

        Processor processor = putRequest.processors().get(0);
        assertThat(processor.isTextEmbedding()).isTrue();
        TextEmbeddingProcessor textEmbedding = processor.textEmbedding();
        assertThat(textEmbedding.modelId()).isEqualTo(MODEL_ID);
        assertThat(textEmbedding.fieldMap()).containsExactly(java.util.Map.entry("description", "description_vector"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void doesNotCreateCarsEmbeddingPipelineWhenItAlreadyExists() throws IOException {
        when(openSearchClient.ingest()).thenReturn(ingestClient);
        when(ingestClient.getPipeline(any(GetPipelineRequest.class))).thenReturn(nonEmptyGetPipelineResponse());
        EmbeddingPipelineBootstrap bootstrap = new EmbeddingPipelineBootstrap(openSearchClient, MODEL_ID);

        bootstrap.run();

        verify(ingestClient, never()).putPipeline(any(PutPipelineRequest.class));
    }

    @Test
    void throwsBeforeTouchingClientWhenModelIdIsBlank() {
        assertThatThrownBy(() -> new EmbeddingPipelineBootstrap(openSearchClient, ""))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(openSearchClient);
    }

    /**
     * Mirrors the real client's response for an unknown pipeline id: no
     * exception, just an empty result map. Built via the real
     * {@link GetPipelineResponse.Builder} (rather than mocking
     * {@code result()} directly) since {@code result()} is inherited from
     * the generic {@code DictionaryResponse} base class and Mockito's
     * default answer machinery doesn't reliably intercept it when stubbed
     * in isolation.
     */
    private static GetPipelineResponse emptyGetPipelineResponse() {
        return new GetPipelineResponse.Builder().result(Map.of()).build();
    }

    private static GetPipelineResponse nonEmptyGetPipelineResponse() {
        Pipeline pipeline = new Pipeline.Builder().description("existing pipeline").build();
        return new GetPipelineResponse.Builder().putResult(PIPELINE_ID, pipeline).build();
    }
}
