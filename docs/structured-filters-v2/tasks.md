# Structured filters v2 (model & transmission) — tasks

This table is the tracker the `implement` skill reads. Status is only ever changed by the coordinator after an independent `reviewer` pass confirms `REVIEW_CLEAN` — not by any agent's self-report. `done` requires a merged PR, not just a passing gate. `Reqs` cites the `R#.#` IDs from `requirements.md` this task implements — `reviewer` checks the diff against those, not against a paraphrase of the task title. Status updates are pushed directly to `main` (bookkeeping, same as `docs/lessons/` — not code, doesn't go through PR review); the actual code for each task always does.

This file belongs to exactly one Linear issue, referenced once here:

Linear: [BOH-22](https://linear.app/bohdanloiko/issue/BOH-22)

| # | Task | Status | Reqs | PR |
|---|---|---|---|---|
| 1 | Add required `transmission` field to `Car` (non-blank validation) and `CarIndexMapping` (keyword); update every existing `Car`-constructing test fixture to compile | in_review | R2.2 | [#13](https://github.com/bloiko/car-searcher/pull/13) |
| 2 | Add `model`/`transmission` to `CarSearchRequest.Filters`; `CarSearchService` adds matching `term` filter clauses | in_review | R1.1, R2.1 | [#13](https://github.com/bloiko/car-searcher/pull/13) |
| 3 | Frontend: remove the filters drawer/toggle, make classic filters (make/model/year/mileage/transmission) always visible, add model/transmission inputs, reframe the search label/placeholder as residual/descriptive | in_review | R3.1, R4.1 | [#13](https://github.com/bloiko/car-searcher/pull/13) |
