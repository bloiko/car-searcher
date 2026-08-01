# A deliberately scoped-down tracker auto-closes the full ticket, not just its slice

**What happened:** BOH-17 ("Pagination and sorting for search results") was deliberately scoped down to a small first slice — sort only, pagination explicitly left as a future follow-up — tracked in `docs/search-sort/tasks.md` with its own 2-row tracker still referencing `Linear: [BOH-17](...)`. Once PR #12 merged and `workflow.mjs check-merged` ran, it moved BOH-17 straight to **Done** — even though pagination, half the ticket's actual title, was never implemented.

**Root cause:** `check-merged` (see `ai-workflow/skills/implement/SKILL.md`, "One Linear ticket per feature, not per tracker row") moves the shared Linear ticket to Done once **every row in that tracker file** is `done` — by design, since normally one tracker file *is* the full scope of its ticket. That assumption breaks the moment a tracker is deliberately scoped to less than the ticket's full stated scope (as `docs/search-sort/tasks.md`'s own header explicitly says: "Pagination... is explicitly NOT part of this pass"). The automation has no way to know the tracker is a partial slice; it only sees "all rows done" and treats that as "ticket done."

**Fix applied:** reopened BOH-17 to Todo, added a comment explaining sort is done/pagination isn't, and noting the process gap for next time.

**Why this matters beyond this one incident:** any time a ticket gets deliberately split into a smaller first-pass tracker (the same pattern used for BOH-20/BOH-21-style "small fix" trackers, but applied to a genuine subset of a larger ticket instead of the ticket's whole scope), `check-merged` will falsely mark the parent ticket Done the moment that subset's rows are all `done`. Two ways to avoid this going forward:
- Prefer NOT reusing the original ticket ID for a deliberately-partial tracker — file a new sub-scoped ticket (e.g. "BOH-17a: sort" as its own issue, or a proper Linear sub-issue) instead of pointing a partial tracker at the parent ticket's ID.
- If reusing the parent ticket ID is unavoidable, manually reopen it after `check-merged` and don't treat its auto-close as authoritative — check the tracker's own header for scoping caveats before trusting "Done."

**Tag:** `workflow-mjs`, `check-merged`, `linear`, `scoped-tracker`, `false-done`
