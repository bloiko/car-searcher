# Search pagination — tasks

This table is the tracker the `implement` skill reads. Status is only ever changed by the coordinator after an independent `reviewer` pass confirms `REVIEW_CLEAN` — not by any agent's self-report. `done` requires a merged PR, not just a passing gate. `Reqs` cites the `R#.#` IDs from `requirements.md` this task implements — `reviewer` checks the diff against those, not against a paraphrase of the task title. Status updates are pushed directly to `main` (bookkeeping, same as `docs/lessons/` — not code, doesn't go through PR review); the actual code for each task always does.

This file belongs to exactly one Linear issue, referenced once here:

Linear: [BOH-17](https://linear.app/bohdanloiko/issue/BOH-17)

This is the second and final tracker for BOH-17 — the first (`docs/search-sort/`) shipped sort. Once both rows below are `done`, BOH-17's full original scope ("Pagination and sorting for search results") is actually complete.

| # | Task | Status | Reqs | PR |
|---|---|---|---|---|
| 1 | `CarSearchRequest` gains validated `page`/`pageSize`; `CarSearchService.search(...)` returns a new `SearchResult(cars, total)` with `from`/`size` applied; `CarSearchController`/`CarSearchResponse` carry `total` through | done | R1.1, R1.2, R1.3, R1.4, R2.1 | [#17](https://github.com/bloiko/car-searcher/pull/17) (merged) |
| 2 | Frontend: page `$state`, Previous/Next controls disabled at boundaries, page resets to 0 on a new query/filter/sort change | done | R3.1, R3.2 | [#17](https://github.com/bloiko/car-searcher/pull/17) (merged) |
