# Premium redesign — tasks

This table is the tracker the `implement` skill reads. Status is only ever changed by the coordinator after an independent `reviewer` pass confirms `REVIEW_CLEAN` — not by any agent's self-report. `done` requires a merged PR, not just a passing gate. `Reqs` cites the `R#.#` IDs from `requirements.md` this task implements — `reviewer` checks the diff against those, not against a paraphrase of the task title. Status updates are pushed directly to `main` (bookkeeping, same as `docs/lessons/` — not code, doesn't go through PR review); the actual code for each task always does.

This file belongs to exactly one Linear issue, referenced once here:

Linear: [BOH-29](https://linear.app/bohdanloiko/issue/BOH-29)

| # | Task | Status | Reqs | PR |
|---|---|---|---|---|
| 1 | Replace design tokens (palette, hero type scale, monospace numeric token) and restructure the search page's header into a hero section, preserving every existing form control's id/label/behavior | done | R1.1, R1.2, R1.3, R2.1, R2.2 | [#19](https://github.com/bloiko/car-searcher/pull/19) (merged) |
| 2 | Result cards: hover lift/shadow motion (reduced-motion aware), apply monospace treatment to price/mileage | done | R3.1, R3.2 | [#19](https://github.com/bloiko/car-searcher/pull/19) (merged) |
| 3 | Apply the same tokens and numeric treatment to the `/cars/[id]` detail page | done | R4.1 | [#19](https://github.com/bloiko/car-searcher/pull/19) (merged) |
