# Semantic car search — tasks

This file belongs to exactly one Linear issue, referenced once here — every row below is a subtask of this one feature, not a separate ticket:

Linear: [BOH-11](https://linear.app/bohdanloiko/issue/BOH-11)

(BOH-5..8 still exist as historical sub-issues from before this convention — harmless to leave, just no longer what tasks.md rows link to.)

This table is the tracker the `implement` skill reads. Status is only ever changed by the coordinator after an independent `reviewer` pass confirms `REVIEW_CLEAN` — not by any agent's self-report. `done` requires a merged PR, not just a passing gate. `Reqs` cites the `R#.#` acceptance criteria from `requirements.md` this task implements — `reviewer` checks the diff against those, not against a paraphrase of the task title. Status updates here are pushed directly to `main` (bookkeeping, same as `docs/lessons/` — not code, doesn't go through PR review); the actual code for each task always does. Because the Linear issue above is shared by every row, it only moves to Done once every row here is `done` — not on the first row's merge.

| # | Task | Status | Reqs | PR |
|---|---|---|---|---|
| 1 | `Car` domain model + OpenSearch index mapping (keyword search version) | done | R2.1, R3.1 | [#1](https://github.com/bloiko/car-searcher/pull/1) (merged) |
| 2 | `POST /api/cars` — index a listing | done | R1.1 (enables it) | [#3](https://github.com/bloiko/car-searcher/pull/3) (merged) |
| 3 | `POST /api/cars/search` — placeholder `multi_match` keyword search | done | R1.1, R1.2, R4.1 | [#4](https://github.com/bloiko/car-searcher/pull/4) (merged) |
| 4 | SvelteKit search page calling the above | done | R3.1 | [#4](https://github.com/bloiko/car-searcher/pull/4) (merged) |

Bootstrap already done directly (not through the TDD loop, since it's scaffolding rather than a task with a testable behavior): pom.xml, Spring Boot app skeleton, gate stack wired into the Maven build (PMD, SpotBugs, ArchUnit), local OpenSearch via `docker-compose.yml`.

## Backlog — real semantic search

Now tracked as real Linear tickets/trackers instead of untracked bullets (see BOH-19's semantic-search-strategy thread for the full reasoning):

- ~~Pick the embedding model~~ — **resolved**, see "Embedding model & vector architecture" in `design.md` (BOH-23, done).
- Structured filters (model/transmission) + UI split between classic filters and residual search — BOH-22, done (`docs/structured-filters-v2/`).
- Generate and index `description_vector` embeddings via the chosen model — BOH-26, blocked on BOH-23 (done, so unblocked).
- k-NN semantic search over the residual query, combined with the structured filters from BOH-22 — BOH-27, blocked on BOH-22 (done) and BOH-26.
- Seed a realistic local dataset large enough to tell good ranking from bad — BOH-24 (seed real inventory), separately tracked, not blocking BOH-26/27's implementation but needed to actually evaluate ranking quality.
- Frontend: show relevance qualitatively (e.g. "why this matched") — deliberately not ticketed, see BOH-27's own scope note (not part of this round).
