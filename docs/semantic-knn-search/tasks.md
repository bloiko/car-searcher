# Semantic k-NN search — tasks

This table is the tracker the `implement` skill reads. Status is only ever changed by the coordinator after an independent `reviewer` pass confirms `REVIEW_CLEAN` — not by any agent's self-report. `done` requires a merged PR, not just a passing gate. `Reqs` cites the `R#.#` IDs from `requirements.md` this task implements — `reviewer` checks the diff against those, not against a paraphrase of the task title. Status updates are pushed directly to `main` (bookkeeping, same as `docs/lessons/` — not code, doesn't go through PR review); the actual code for each task always does.

This file belongs to exactly one Linear issue, referenced once here:

Linear: [BOH-27](https://linear.app/bohdanloiko/issue/BOH-27)

| # | Task | Status | Reqs | PR |
|---|---|---|---|---|
| 1 | `CarSearchService`'s `must` clause becomes a `neural` query against `description_vector` (replacing `multi_match`), using the configured embedding model ID; `CarSearchRequest`'s stale javadoc updated | todo | R1.1, R1.2, R2.1, R2.2 | |

**Manual live-cluster verification required before this is considered done** (same standard as BOH-26) — this machine has hit real memory limits running the embedding model locally; budget for that check to happen separately from implementation if needed, rather than forcing it immediately.
