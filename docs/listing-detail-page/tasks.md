# Listing detail page — tasks

This table is the tracker the `implement` skill reads. Status is only ever changed by the coordinator after an independent `reviewer` pass confirms `REVIEW_CLEAN` — not by any agent's self-report. `done` requires a merged PR, not just a passing gate. `Reqs` cites the `R#.#` IDs from `requirements.md` this task implements — `reviewer` checks the diff against those, not against a paraphrase of the task title. Status updates are pushed directly to `main` (bookkeeping, same as `docs/lessons/` — not code, doesn't go through PR review); the actual code for each task always does.

This file belongs to exactly one Linear issue, referenced once here:

Linear: [BOH-15](https://linear.app/bohdanloiko/issue/BOH-15)

| # | Task | Status | Reqs | PR |
|---|---|---|---|---|
| 1 | Add optional `sourceUrl` to `Car` (via a convenience delegating constructor, zero fixture blast radius) and `CarIndexMapping` (keyword) | todo | (foundation for R3.1/R3.2) | |
| 2 | `GET /api/cars/{id}` — new lookup service + controller endpoint, 200 with full `Car` or 404 | todo | R1.1, R1.2 | |
| 3 | Frontend: `/cars/[id]` detail route (all photos, all fields, conditional outbound `sourceUrl` link), search-result cards link to it | todo | R1.3, R2.1, R3.1, R3.2 | |
