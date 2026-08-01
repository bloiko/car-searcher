# Search UI redesign — tasks

This table is the tracker the `implement` skill reads. Status is only ever changed by the coordinator after an independent `reviewer` pass confirms `REVIEW_CLEAN` — not by any agent's self-report. `done` requires a merged PR, not just a passing gate. `Reqs` cites the `R#.#` IDs from `requirements.md` this task implements — `reviewer` checks the diff against those, not against a paraphrase of the task title. Status updates are pushed directly to `main` (bookkeeping, same as `docs/lessons/` — not code, doesn't go through PR review); the actual code for each task always does.

This file belongs to exactly one Linear issue, referenced once here:

Linear: [BOH-19](https://linear.app/bohdanloiko/issue/BOH-19)

| # | Task | Status | Reqs | PR |
|---|---|---|---|---|
| 1 | Add design-token CSS (light/dark custom properties, type scale, spacing) to `+page.svelte`; restyle `<h1>` and the existing form elements against the tokens — no structural changes yet | in_progress | R1.1 |  |
| 2 | Restructure header: search input first and visually primary, filter inputs moved into a collapsible drawer behind a "Filters" toggle button | todo | R1.2 | |
| 3 | Add dismissible filter chips derived from the four filter `$state` values; dismissing a chip clears that filter | todo | R2.1, R2.2 | |
| 4 | Replace the `<ul><li>` result list with a card grid; add the missing `mileage` field to the frontend `CarResult` type and render it | todo | R3.1 | |
| 5 | Restyle the loading/empty/error branches as distinct, styled states (`role="status"` for loading, styled empty-state block, styled error banner) | todo | R4.1, R4.2, R4.3 | |
| 6 | Add the `max-width: 720px` responsive rule collapsing the card grid to one column; verify manually in the browser preview | todo | R5.1 | |
