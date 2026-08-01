# Semantic car search — tasks

This file belongs to exactly one Linear issue, referenced once here — every row below is a subtask of this one feature, not a separate ticket:

Linear: [BOH-11](https://linear.app/bohdanloiko/issue/BOH-11)

(BOH-5..8 still exist as historical sub-issues from before this convention — harmless to leave, just no longer what tasks.md rows link to.)

This table is the tracker the `implement` skill reads. Status is only ever changed by the coordinator after an independent `reviewer` pass confirms `REVIEW_CLEAN` — not by any agent's self-report. `done` requires a merged PR, not just a passing gate. `Reqs` cites the `R#.#` acceptance criteria from `requirements.md` this task implements — `reviewer` checks the diff against those, not against a paraphrase of the task title. Status updates here are pushed directly to `main` (bookkeeping, same as `docs/lessons/` — not code, doesn't go through PR review); the actual code for each task always does. Because the Linear issue above is shared by every row, it only moves to Done once every row here is `done` — not on the first row's merge.

| # | Task | Status | Reqs | PR |
|---|---|---|---|---|
| 1 | `Car` domain model + OpenSearch index mapping (keyword search version) | done | R2.1, R3.1 | [#1](https://github.com/bloiko/car-searcher/pull/1) (merged) |
| 2 | `POST /api/cars` — index a listing | done | R1.1 (enables it) | [#3](https://github.com/bloiko/car-searcher/pull/3) (merged) |
| 3 | `POST /api/cars/search` — placeholder `multi_match` keyword search | in_review | R1.1, R1.2, R4.1 | [#4](https://github.com/bloiko/car-searcher/pull/4) |
| 4 | SvelteKit search page calling the above | in_review | R3.1 | [#4](https://github.com/bloiko/car-searcher/pull/4) |

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
