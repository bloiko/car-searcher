# Embedding indexing — design

## Requirements traceability

| Requirement | Design element |
|---|---|
| R1.1 | `CarIndexMapping`'s index creation references an ingest pipeline as `default_pipeline` in index settings; `CarIndexingService` is untouched — it still just indexes a `Car` document, the pipeline does the embedding transparently |
| R1.2 | `CarIndexMapping.mapping()` gains `description_vector` as a `knn_vector` field, dimension 384; index settings gain `index.knn: true` |
| R2.1 | New `EmbeddingPipelineBootstrap` (`CommandLineRunner`, same pattern as `CarIndexBootstrap`) checks whether the ingest pipeline exists (`GET _ingest/pipeline/<name>`) and creates it if not, using a `text_embedding` processor referencing the configured model ID |
| R2.2 | `EmbeddingPipelineBootstrap` fails fast (throws, preventing successful startup) if the configured model ID property is blank/unset, or if pipeline creation fails because the model isn't deployed |

## One-time setup (out of scope for R1.1/R2.1's automation, but required before either works)

`backend/scripts/register-embedding-model.sh` — a plain shell script (curl calls against the local OpenSearch cluster), run once per environment, not part of application startup:
1. `PUT _cluster/settings` — enable `plugins.ml_commons.only_run_on_ml_node: false` and `plugins.ml_commons.model_access_control_enabled: true` (local single-node dev cluster has no dedicated ML node).
2. `POST _plugins/_ml/model_groups/_register` — create a model group, capture `model_group_id`.
3. `POST _plugins/_ml/models/_register` — register `huggingface/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2` as a custom local model in that group (async — poll the returned `task_id` via `GET _plugins/_ml/tasks/<task_id>` until `COMPLETED`), capture `model_id`.
4. `POST _plugins/_ml/models/<model_id>/_deploy` (async — poll again), confirm `DEPLOYED`.
5. Print the final `model_id` and the exact `application.properties` line to set it (`car-searcher.embedding.model-id=<id>`).

Documented in the project README as a required one-time local setup step, same tier as installing OpenSearch itself.

## Data model

`CarIndexMapping.mapping()` gains:
```
description_vector   knn_vector, dimension: 384
```
Index-level settings (new — `CarIndexMapping`/`CarIndexBootstrap` don't set any today beyond the default) gain:
```
index.knn: true
index.default_pipeline: "cars-embedding-pipeline"   -- see below
```

## Ingest pipeline

Name: `cars-embedding-pipeline`. One processor:
```json
{
  "description": "Embeds Car.description into description_vector using the multilingual model from BOH-23",
  "processors": [
    {
      "text_embedding": {
        "model_id": "<configured model ID>",
        "field_map": { "description": "description_vector" }
      }
    }
  ]
}
```
`EmbeddingPipelineBootstrap` builds and `PUT`s this via the OpenSearch Java client's low-level/generic request support (the typed `opensearch-java` client doesn't have first-class ingest-pipeline builders for `text_embedding` as of 2.14.0 — falls back to the client's raw JSON request path, same tier of "drop to the generic API" as `CarIndexBootstrap` already does for `indices().exists()`/`.create()`).

## Configuration

New `application.properties` entry: `car-searcher.embedding.model-id` (blank by default — intentionally, so a fresh environment fails loudly per R2.2 rather than silently running without embeddings). Bound via a small `@ConfigurationProperties`-style read or a direct `@Value` injection into `EmbeddingPipelineBootstrap` — match whatever pattern (if any) already exists elsewhere in the app for config properties; if none exists yet, `@Value("${car-searcher.embedding.model-id:}")` is the minimal addition.

## Startup ordering

`EmbeddingPipelineBootstrap` must run *before* `CarIndexBootstrap` creates the index (since the index references the pipeline via `default_pipeline` at creation time) — use Spring's `@Order` (or `CommandLineRunner` ordering) to sequence them, pipeline first.

## Testing strategy

- `EmbeddingPipelineBootstrap`: mocked-client tests mirroring `CarIndexBootstrapTest`'s style — pipeline-doesn't-exist → creation called with the right processor/model ID; pipeline-already-exists → creation not called (idempotency); blank model ID → throws before attempting any client call.
- `CarIndexMapping`: extend the existing mapping test to assert `description_vector` is present with the right type/dimension, and that index settings include `index.knn: true` and the correct `default_pipeline` name (this is new — no test today checks index *settings*, only mappings; `CarIndexBootstrap`'s existing tests may need a settings-assertion added too, since the change to `index.knn`/`default_pipeline` most likely lands in the same `CreateIndexRequest` builder call it already makes).
- No test can verify the pipeline actually produces a non-zero vector against a real model — that's exactly the class of gap this repo's own lessons (`docs/lessons/2026-08-01-*`) warn about. Manual verification against the real local cluster (register the model via the script, restart the backend, index a car, confirm `description_vector` is populated with 384 non-zero floats) is required before this is considered actually done, not just gate-green.

## Open decisions

None — this design resolves everything needed to implement; the model-registration script's exact `curl` sequence may need minor adjustment against the real cluster during implementation (ML Commons cluster-settings prerequisites can vary slightly by OpenSearch security-plugin configuration), but the overall shape is settled.
