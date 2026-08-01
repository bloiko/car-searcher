# Semantic car search — tasks

This table is the tracker the `implement` skill reads. Status is only ever changed by the coordinator after an independent `reviewer` pass confirms `REVIEW_CLEAN` — not by any agent's self-report.

| # | Task | Status |
|---|---|---|
| 1 | `Car` domain model + OpenSearch index mapping (keyword search version) | todo |
| 2 | `POST /api/cars` — index a listing | todo |
| 3 | `POST /api/cars/search` — placeholder `multi_match` keyword search | todo |
| 4 | SvelteKit search page calling the above | todo |

Bootstrap already done directly (not through the TDD loop, since it's scaffolding rather than a task with a testable behavior): pom.xml, Spring Boot app skeleton, gate stack wired into the Maven build (PMD, SpotBugs, ArchUnit), local OpenSearch via `docker-compose.yml`.

## Backlog — real semantic search (not yet broken into tracker tasks)

Add these to the table above, one at a time, once tasks 1–4 are done:

- Pick the embedding model (see open decision in `design.md`)
- Add `description_vector` (knn_vector) to the index mapping
- Generate embeddings at index time
- Embed the query string at search time, run k-NN search
- Combine k-NN relevance with structured filters (price/year/mileage)
- Seed a realistic local dataset large enough to tell good ranking from bad
- Frontend: show relevance qualitatively (e.g. "why this matched") if the embedding approach supports it cheaply
