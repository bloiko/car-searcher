# Index bootstrap — tasks

Small fix (no requirements.md/design.md — CLAUDE.md's SDD ceremony is for features with real design decisions; this is a single well-scoped gap with no open decisions, see the ticket description below).

This table is the tracker the `implement` skill reads. Status is only ever changed by the coordinator after an independent `reviewer` pass confirms `REVIEW_CLEAN` — not by any agent's self-report. `done` requires a merged PR, not just a passing gate. Status updates are pushed directly to `main` (bookkeeping, same as `docs/lessons/` — not code, doesn't go through PR review); the actual code for each task always does.

This file belongs to exactly one Linear issue, referenced once here:

Linear: [BOH-20](https://linear.app/bohdanloiko/issue/BOH-20)

## Acceptance criterion (from the BOH-20 ticket, standing in for requirements.md)

WHEN the backend application starts, THE SYSTEM SHALL ensure the `cars` OpenSearch index exists with `CarIndexMapping`'s schema — creating it if and only if it does not already exist (idempotent: a second startup against an already-bootstrapped cluster must not fail or attempt to recreate it).

| # | Task | Status | Reqs | PR |
|---|---|---|---|---|
| 1 | Add a startup step (`CommandLineRunner`/`ApplicationRunner`) that creates the `cars` index with `CarIndexMapping.mapping()` if it doesn't already exist | todo | (see acceptance criterion above) | |
