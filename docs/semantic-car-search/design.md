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
  description   text
  description_vector   knn_vector (dimension depends on the chosen embedding model)
```

## Open decision: which embedding model

Needs to run locally (no deployment yet, so no paid embedding API by default). Candidates to evaluate in the implementation ticket:
- A local ONNX sentence-transformer model served alongside the backend
- OpenSearch's own ML Commons model-serving, if the local cluster supports it without extra infra
- Punt to a hosted API (OpenAI/Cohere embeddings) later, once/if this gets deployed and cost is less of a concern

Not resolved here on purpose — it's implementation work, not a scaffolding decision.

## Phase 1 scaffold vs. real implementation

The initial scaffold ships a **placeholder**: a plain OpenSearch `multi_match` keyword query on `description`/`make`/`model`, wired through the same REST endpoint shape the real semantic version will use. That proves the plumbing (indexing, controller, frontend call) end-to-end before the harder embedding-model decision blocks anything. Swapping the placeholder for real k-NN search is the first real task in `tasks.md`, not part of the scaffold itself.

## API

`POST /api/cars/search`
```json
{ "query": "reliable family suv under 30k", "filters": { "priceMax": 30000 } }
```
→ `{ "results": [ { "id", "make", "model", "year", "price", "description" }, ... ] }`

`POST /api/cars` — index a car listing (used to seed data for local dev; no auth yet, local-only).
