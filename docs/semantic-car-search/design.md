# Semantic car search — design

## Requirements traceability

| Requirement | Design element |
|---|---|
| R1.1 | "Approach" — k-NN vector search for meaning-based relevance |
| R1.2 | "API" — empty `results` array, still a 200, no special-case error path |
| R2.1 | "Approach" — filters applied alongside the k-NN/keyword query, not as a separate step |
| R3.1 | "API" — response shape includes id/make/model/year/price/description per result |
| R4.1 | **Not yet addressed.** No indexing/query performance work has been designed — flagging this explicitly rather than silently dropping it, per the "unwanted behavior" principle: an unaddressed requirement is a drafting gap, not something to paper over. Needs a design pass once real k-NN search (not the placeholder) is in place, since that's what will actually determine latency. |

## Approach

OpenSearch's k-NN plugin, storing a dense vector embedding of each listing's descriptive text (make + model + year + free-text description) alongside its structured fields. A query embeds the search string with the same model, then runs a k-NN search, optionally combined with the structured filters as a post-filter or a hybrid (keyword + vector) query.

## Index mapping (target shape)

```
cars
  id            keyword
  make          keyword
  model         keyword
  year          integer
  price         float
  mileage       integer
  transmission  keyword
  description   text
  description_vector   knn_vector (dimension 384, see "Embedding model & vector architecture" below)
```

## Embedding model & vector architecture (BOH-23 — resolved)

**Model: `paraphrase-multilingual-MiniLM-L12-v2` (384 dimensions), hosted as an OpenSearch ML Commons custom local model — not called from the Java backend at all.**

Why this shape, not a Java-side embedding call:
- OpenSearch's Neural Search plugin (present since 2.9, this project runs 2.16.0) can host a sentence-transformer model directly inside the cluster and generate embeddings via an **ingest pipeline** at index time and a **`neural` query** at search time. `CarIndexingService` and `CarSearchService` then never touch embedding generation directly — they index/query `description` as plain text, same as today, and the pipeline/query type does the vector work. This is the smallest actual code change to `backend/` of the options considered (no new HTTP client, no ONNX runtime dependency, no separate model-serving process to run alongside the JVM), which matters given "local-only, not deployed anywhere" — one fewer moving part to keep running.
- Confirmed via OpenSearch's own docs that multilingual local models (this one by name, `huggingface/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2`) are a supported "custom local model" path, distinct from the small officially-pretrained list (`all-MiniLM-L6-v2`, `msmarco-distilbert-base-tas-b`, `all-distilroberta-v1`) which only covers English.

Why multilingual, not the simpler English-only pretrained option (`all-MiniLM-L6-v2`, same 384 dimensions, zero-upload registration): BOH-16/18 (real data ingestion, AutoRia) means real listing text will be Ukrainian/Russian, not English. An English-only model would produce poor embeddings for the actual target data — picking the easier-to-register English model now would just mean redoing this decision once real data lands. The multilingual model costs one extra one-time setup step (packaging + uploading the model via ML Commons' custom-local-model registration, rather than registering an already-pretrained one by name) — worth it to not build the wrong thing.

**Consequence for the implementation ticket (BOH-26):** the one-time model upload/registration is infra setup work, not application code — it's a `curl`/script step run once against the local OpenSearch cluster (register model group → register model → deploy model → create the ingest pipeline that maps `description` → `description_vector`), not something `CarIndexingService` needs to orchestrate at runtime. `CarIndexMapping` gains the `description_vector` field with the pipeline referenced as the index's default pipeline.

**Consequence for BOH-27 (k-NN search combined with structured filters):** the search query becomes a `neural` query (auto-embeds the free-text residual query using the same hosted model) inside the existing `bool` query's `must` clause, with the structured filters from BOH-22 staying exactly where they are today — in `filter` context, unscored. No change to how filters combine; only the `must` clause's query type changes from `multi_match` to `neural`.

Deliberately not chosen: a local ONNX model called from the Java backend directly (more code, an ML runtime dependency in a Spring Boot app that has none today) or a hosted embedding API like OpenAI/Cohere (real per-call cost, network dependency, works against "local-only for now" — worth revisiting once/if BOH-25 deployment happens and a hosted API's operational simplicity might outweigh the cost).

## Phase 1 scaffold vs. real implementation

The initial scaffold ships a **placeholder**: a plain OpenSearch `multi_match` keyword query on `description`/`make`/`model`, wired through the same REST endpoint shape the real semantic version will use. That proves the plumbing (indexing, controller, frontend call) end-to-end before the harder embedding-model decision blocks anything. Swapping the placeholder for real k-NN search is the first real task in `tasks.md`, not part of the scaffold itself.

## API

`POST /api/cars/search`
```json
{ "query": "reliable family suv under 30k", "filters": { "priceMax": 30000 } }
```
→ `{ "results": [ { "id", "make", "model", "year", "price", "mileage", "description" }, ... ], "total": 137 }`

`POST /api/cars` — index a car listing (used to seed data for local dev; no auth yet, local-only).
