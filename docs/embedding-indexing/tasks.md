# Embedding indexing — tasks

This table is the tracker the `implement` skill reads. Status is only ever changed by the coordinator after an independent `reviewer` pass confirms `REVIEW_CLEAN` — not by any agent's self-report. `done` requires a merged PR, not just a passing gate. `Reqs` cites the `R#.#` IDs from `requirements.md` this task implements — `reviewer` checks the diff against those, not against a paraphrase of the task title. Status updates are pushed directly to `main` (bookkeeping, same as `docs/lessons/` — not code, doesn't go through PR review); the actual code for each task always does.

This file belongs to exactly one Linear issue, referenced once here:

Linear: [BOH-26](https://linear.app/bohdanloiko/issue/BOH-26)

| # | Task | Status | Reqs | PR |
|---|---|---|---|---|
| 1 | `CarIndexMapping` gains `description_vector` (knn_vector, dim 384); `CarIndexBootstrap`'s index creation gains `index.knn: true` and `default_pipeline` settings | in_progress | R1.2 |  |
| 2 | New `EmbeddingPipelineBootstrap` (`CommandLineRunner`, ordered before `CarIndexBootstrap`) idempotently creates the `text_embedding` ingest pipeline; fails fast if the model ID config is blank | in_progress | R1.1, R2.1, R2.2 |  |

Done directly (not through the TDD loop, since it's infra scaffolding rather than testable application behavior — same category as `docker-compose.yml`/pom.xml bootstrap in the original `semantic-car-search` tracker): `backend/scripts/register-embedding-model.sh` + README documentation for the one-time model registration/deployment step.
