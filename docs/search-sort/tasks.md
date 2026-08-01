# Search sort — tasks

Small fix (no requirements.md/design.md — CLAUDE.md's SDD ceremony is for features with real design decisions; this is a scoped-down first slice of BOH-17, sort only, with the design decisions below already made). Pagination (the other half of BOH-17) is explicitly NOT part of this pass — left as a future row once picked up again.

This table is the tracker the `implement` skill reads. Status is only ever changed by the coordinator after an independent `reviewer` pass confirms `REVIEW_CLEAN` — not by any agent's self-report. `done` requires a merged PR, not just a passing gate. Status updates are pushed directly to `main` (bookkeeping, same as `docs/lessons/` — not code, doesn't go through PR review); the actual code for each task always does.

This file belongs to exactly one Linear issue, referenced once here:

Linear: [BOH-17](https://linear.app/bohdanloiko/issue/BOH-17)

## Acceptance criteria (standing in for requirements.md — small, scoped slice)

- WHEN a search request includes `sort: "price_asc"` THE SYSTEM SHALL return results ordered by ascending price, overriding relevance ranking.
- WHEN a search request includes `sort: "mileage_asc"` THE SYSTEM SHALL return results ordered by ascending mileage, overriding relevance ranking.
- WHILE `sort` is absent or `null` THE SYSTEM SHALL behave exactly as today (relevance-ranked, unaffected by this feature).
- IF `sort` is present but not one of `"price_asc"`/`"mileage_asc"` THEN THE SYSTEM SHALL reject the request with a validation error (same style as `Filters`' existing negative-value rejection).
- WHEN a user changes the sort control in the UI and submits a search THE SYSTEM SHALL include the corresponding `sort` value in the request.

**Design decisions already made, not open:**
- `sort` is a new top-level field on `CarSearchRequest` (sibling to `filters`), not nested inside `Filters` — it changes result *ordering*, not the result *set*, a different concern from filters.
- Only `price_asc` and `mileage_asc` for this slice (matches the ticket's own examples: "Sort-by-price and sort-by-mileage are basic car-shopping expectations"). Descending variants and relevance-explicit are out of scope here — add later if actually needed.

| # | Task | Status | Reqs | PR |
|---|---|---|---|---|
| 1 | Add `sort` field to `CarSearchRequest`; `CarSearchService` applies an OpenSearch sort clause for `price_asc`/`mileage_asc`, rejects any other non-null value, unaffected when absent | in_progress | (see acceptance criteria above) |  |
| 2 | Frontend: sort `<select>` in `+page.svelte` (Best match / Price: low to high / Mileage: low to high), included in the request when set | in_progress | (see acceptance criteria above) |  |
