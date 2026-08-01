# Car photos — tasks

This table is the tracker the `implement` skill reads. Status is only ever changed by the coordinator after an independent `reviewer` pass confirms `REVIEW_CLEAN` — not by any agent's self-report. `done` requires a merged PR, not just a passing gate. `Reqs` cites the `R#.#` IDs from `requirements.md` this task implements — `reviewer` checks the diff against those, not against a paraphrase of the task title. Status updates are pushed directly to `main` (bookkeeping, same as `docs/lessons/` — not code, doesn't go through PR review); the actual code for each task always does.

This file belongs to exactly one Linear issue, referenced once here:

Linear: [BOH-13](https://linear.app/bohdanloiko/issue/BOH-13)

| # | Task | Status | Reqs | PR |
|---|---|---|---|---|
| 1 | `Car.photoUrls` field + validation (reject blank entries) + `CarIndexMapping` gets `photo_urls` | done | R2.1, R2.2 | [#6](https://github.com/bloiko/car-searcher/pull/6) (merged) |
| 2 | `CarSearchResult`/search response includes `photoUrls` | done | R1.1 | [#6](https://github.com/bloiko/car-searcher/pull/6) (merged) |
| 3 | Frontend: render first photo (or placeholder) on each result card | done | R1.2, R1.3 | [#6](https://github.com/bloiko/car-searcher/pull/6) (merged) |
