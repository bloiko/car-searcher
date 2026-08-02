# `GetPipelineResponse` silently returned empty instead of throwing for a missing pipeline

**What happened:** `EmbeddingPipelineBootstrap.run()` was written to check for the
`cars-embedding-pipeline` ingest pipeline like this:

```java
try {
    openSearchClient.ingest().getPipeline(new GetPipelineRequest.Builder()
            .id(CarIndexBootstrap.CARS_EMBEDDING_PIPELINE)
            .build());
    // Pipeline already exists; nothing to do.
} catch (OpenSearchException e) {
    if (e.status() != 404) {
        throw e;
    }
    createPipeline();
} catch (IOException e) {
    throw new UncheckedIOException(...);
}
```

This mirrors the real `GET _ingest/pipeline/<id>` HTTP semantics — a raw curl
against a missing pipeline genuinely returns HTTP 404 — so modeling
"doesn't exist" as a thrown `OpenSearchException` with status 404 looked
correct, and `EmbeddingPipelineBootstrapTest` was written (and passed) with
that exact assumption mocked in.

Running the app fresh against a real OpenSearch 2.16.0 cluster with an empty
index proved otherwise: after startup, `GET _ingest/pipeline/cars-embedding-pipeline`
still returned 404 — `createPipeline()` was never called, meaning the try
block's "pipeline already exists" path was taken even though the pipeline
never existed.

**Diagnosis, evidence not guesswork:** a standalone diagnostic run directly
against the real typed client confirmed `getPipeline()` for a nonexistent
pipeline id returns *normally* — no exception — with a `GetPipelineResponse`
whose `result()` map is empty (`{}`). Concretely: `"NO EXCEPTION THROWN.
Response: GetPipelineResponse@... / Response result map: {}"`. So although the
underlying HTTP transport genuinely gets a 404, the opensearch-java 2.14.0
typed client's `getPipeline()` swallows that and hands back an empty
`DictionaryResponse` instead of propagating the error — the opposite of how
`GET _ingest/pipeline/<id>` behaves at the raw HTTP level, and the opposite of
what the exception-based code (and its mocked test) assumed. The `createPipeline()`
PUT logic itself was independently confirmed correct via raw curl — only the
existence check was wrong.

**Fix:** `run()` now inspects `response.result().isEmpty()` to decide whether
to create the pipeline, instead of relying on catching an exception for a case
that never throws. The `catch (IOException e)` handling (genuine I/O failures)
is unaffected. `EmbeddingPipelineBootstrapTest` was rewritten to build real
`GetPipelineResponse` instances via `GetPipelineResponse.Builder` (empty
`result()` map for "doesn't exist", a map containing the pipeline for "already
exists") instead of mocking a thrown `OpenSearchException` for the missing
case. Verified live: deleted the pipeline, restarted the backend fresh, and
confirmed `GET _ingest/pipeline/cars-embedding-pipeline` now returns 200 with
the real processor body.

**Why the mocked tests never caught it:** exactly like the `documents()` bug
below, the test mocked `getPipeline()` to throw an `OpenSearchException` with
status 404 for the "doesn't exist" case — encoding the same wrong assumption
the production code made, at the exact layer where the real bug lived. A
green test built on a mocked method that itself doesn't reflect the real
client's behavior can't catch a bug in how that behavior is interpreted.

**Why this matters beyond this one fix:** this is the *second* confirmed
instance of an opensearch-java 2.14.0 typed client convenience method
silently swallowing an error condition instead of throwing, after
`SearchResponse.documents()` (see
[2026-08-01-documents-silently-empty.md](2026-08-01-documents-silently-empty.md)).
Two different methods, two different response types, the same failure shape:
the client returns a technically-valid-looking empty/default response instead
of surfacing the error the raw HTTP API actually returned. This looks like a
systemic trait of this client version rather than an isolated method quirk —
any new code that assumes "opensearch-java throws for X because raw HTTP
returns an error status for X" should be verified against a real cluster
before being trusted, and any assumption baked into a mock should be treated
as unverified until it has been.

**Tag:** `opensearch-java`, `mocked-test-blind-spot`, `silent-empty-response`, `ingest-pipeline`
