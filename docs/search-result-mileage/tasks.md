# Search result mileage — tasks

Small fix (no requirements.md/design.md — CLAUDE.md's SDD ceremony is for features with real design decisions; this is a one-field contract fix with no open design space).

This table is the tracker the `implement` skill reads. Status is only ever changed by the coordinator after an independent `reviewer` pass confirms `REVIEW_CLEAN` — not by any agent's self-report. `done` requires a merged PR, not just a passing gate. Status updates are pushed directly to `main` (bookkeeping, same as `docs/lessons/` — not code, doesn't go through PR review); the actual code for each task always does.

This file belongs to exactly one Linear issue, referenced once here:

Linear: [BOH-28](https://linear.app/bohdanloiko/issue/BOH-28)

## Acceptance criterion (standing in for requirements.md — small, contained fix)

THE SYSTEM SHALL include `mileage` on every `CarSearchResponse.CarSearchResult`, matching `Car.mileage`, so the API response actually carries the field the frontend (BOH-19) already expects and renders on every result card.

| # | Task | Status | Reqs | PR |
|---|---|---|---|---|
| 1 | Add `mileage` to `CarSearchResponse.CarSearchResult` and its `.from(Car)` factory; update the stale javadoc that says it's deliberately omitted | done | (see acceptance criterion above) | [#16](https://github.com/bloiko/car-searcher/pull/16) (merged) |
