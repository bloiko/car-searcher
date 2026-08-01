# photo_urls mapping fix — tasks

Small fix (no requirements.md/design.md — CLAUDE.md's SDD ceremony is for features with real design decisions; this is a one-line field-name correction with a chosen approach, no open design space).

This table is the tracker the `implement` skill reads. Status is only ever changed by the coordinator after an independent `reviewer` pass confirms `REVIEW_CLEAN` — not by any agent's self-report. `done` requires a merged PR, not just a passing gate. Status updates are pushed directly to `main` (bookkeeping, same as `docs/lessons/` — not code, doesn't go through PR review); the actual code for each task always does.

This file belongs to exactly one Linear issue, referenced once here:

Linear: [BOH-21](https://linear.app/bohdanloiko/issue/BOH-21)

## Acceptance criterion (from the BOH-21 ticket, standing in for requirements.md)

`CarIndexMapping` declares `photo_urls` (snake_case) as a keyword field, but Jackson serializes `Car.photoUrls` (and `CarSearchResponse.CarSearchResult.photoUrls`) as `photoUrls` (camelCase) everywhere else in the app — no `@JsonNaming` override exists anywhere in the codebase. OpenSearch's dynamic mapping silently created a second, real `photoUrls` field to hold the actual indexed data, while the explicit `photo_urls` mapping field sits empty and unused.

**Chosen fix (of the two the ticket offers):** rename the explicit mapping field from `photo_urls` to `photoUrls`, matching what's actually serialized and indexed everywhere else — not "configure Jackson to serialize snake_case consistently," which would also change `CarSearchResponse`'s JSON shape and require a matching frontend change (the frontend's `CarResult` type already expects camelCase `photoUrls` from the API — see `frontend/src/routes/+page.svelte`). Renaming the mapping is the minimal, contained fix; switching Jackson's naming strategy is a much larger blast radius for the same outcome.

THE SYSTEM SHALL declare the `cars` index's photo-URLs field as `photoUrls` (keyword) in `CarIndexMapping`, matching the field name Jackson actually serializes `Car.photoUrls`/`CarSearchResponse.CarSearchResult.photoUrls` as.

| # | Task | Status | Reqs | PR |
|---|---|---|---|---|
| 1 | Rename `CarIndexMapping`'s `photo_urls` property to `photoUrls` | done | (see acceptance criterion above) | [#11](https://github.com/bloiko/car-searcher/pull/11) (merged) |
