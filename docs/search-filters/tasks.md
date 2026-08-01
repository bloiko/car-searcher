# Search filters — tasks

This table is the tracker the `implement` skill reads. Status is only ever changed by the coordinator after an independent `reviewer` pass confirms `REVIEW_CLEAN` — not by any agent's self-report. `done` requires a merged PR, not just a passing gate. `Reqs` cites the `R#.#` IDs from `requirements.md` this task implements — `reviewer` checks the diff against those, not against a paraphrase of the task title. Status updates are pushed directly to `main` (bookkeeping, same as `docs/lessons/` — not code, doesn't go through PR review); the actual code for each task always does.

This file belongs to exactly one Linear issue, referenced once here:

Linear: [BOH-14](https://linear.app/bohdanloiko/issue/BOH-14)

| # | Task | Status | Reqs | PR |
|---|---|---|---|---|
| 1 | Expand `CarSearchRequest.Filters` (yearMin, mileageMax, make) + validation (reject negative priceMax/mileageMax) | todo | R1.2, R3.2 | — |
| 2 | `CarSearchService` builds a `bool` query (multi_match in `must`, filter clauses in `filter`); `CarSearchController` passes filters through | todo | R1.1, R2.1, R3.1, R4.1, R5.1, R5.2 | — |
| 3 | Frontend: filter inputs on the search form, included in the request | todo | R6.1 | — |
