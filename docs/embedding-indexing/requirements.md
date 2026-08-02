# Embedding indexing — requirements

BOH-26, second step of the semantic-search-strategy roadmap. Depends on BOH-23 (embedding model decision, done: `paraphrase-multilingual-MiniLM-L12-v2`, 384-dim, hosted inside OpenSearch via ML Commons). This ticket makes every indexed car actually get a `description_vector` — BOH-27 (the k-NN search itself) depends on this.

## EARS pattern legend

| Pattern | Form | Use for |
|---|---|---|
| Ubiquitous | `THE SYSTEM SHALL <response>` | Always-true invariants |
| Event-driven | `WHEN <trigger> THE SYSTEM SHALL <response>` | Normal-flow behavior |
| State-driven | `WHILE <state> THE SYSTEM SHALL <response>` | Behavior only valid during a state |
| Unwanted behavior | `IF <condition> THEN THE SYSTEM SHALL <response>` | Error handling, edge cases |
| Optional feature | `WHERE <feature present> THE SYSTEM SHALL <response>` | Conditional/configurable behavior |

## User Story 1 — Every indexed car gets an embedding automatically

As the system, I want every car's description embedded into `description_vector` at index time, without any caller having to compute it, so that k-NN search (BOH-27) has something real to search against.

**Acceptance criteria:**
- R1.1 — WHEN a car is indexed via `POST /api/cars` THE SYSTEM SHALL populate `description_vector` on that document via an OpenSearch ingest pipeline, without the application code computing or sending the vector itself.
- R1.2 — THE SYSTEM SHALL declare `description_vector` as a `knn_vector` field with dimension 384 on the `cars` index mapping.

## User Story 2 — The ingest pipeline exists automatically, like the index does

As a developer running this locally, I want the ingest pipeline needed for embedding to be created automatically on backend startup (same as the index itself, BOH-20), so that I don't have to hand-run setup calls every time I stand up a fresh environment.

**Acceptance criteria:**
- R2.1 — WHEN the backend application starts THE SYSTEM SHALL ensure the embedding ingest pipeline exists, creating it if it doesn't already exist (idempotent, mirroring `CarIndexBootstrap`'s existing index-creation behavior).
- R2.2 — IF the configured embedding model ID is not yet available (e.g. the one-time model registration/deployment step hasn't been run) THEN THE SYSTEM SHALL fail startup with a clear error, not silently start with a broken/absent pipeline.

## Explicitly out of scope

- Registering and deploying the ML Commons model itself — that's a one-time, slow (model download/load), infra-level setup step, not something backend startup should block on every restart. Documented as a manual/scripted setup step (`backend/scripts/register-embedding-model.sh` + README), producing the model ID that R2.1/R2.2 consume via configuration.
- Backfilling `description_vector` onto the one existing dev-test document indexed before this feature — out of scope; re-indexing that single record manually after this ships is enough, not worth a migration task for 1 row.
- Actually using `description_vector` in search queries — that's BOH-27.
