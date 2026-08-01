# car-searcher

A car search app with semantic search over OpenSearch. Backend in Java/Spring Boot, frontend in SvelteKit. **Local-only for now — not deployed anywhere.**

This repo also doubles as the proving ground for a separate autonomous dev pipeline project (`ai-workflow`, a sibling directory) that will eventually pick up Linear tickets and open PRs against this repo unattended. That context matters for two conventions below (SDD docs, lessons) even before that pipeline exists — writing them by hand now keeps the repo in the shape the pipeline will expect later.

## Layout

```
car-searcher/
  backend/          Spring Boot (Java 21, Maven)
  frontend/         SvelteKit, talks to backend over REST
  docs/
    <feature>/       requirements.md, design.md, tasks.md per feature (SDD)
    lessons/         INDEX.md + one file per resolved blocker/failure
```

## Spec-driven development (SDD)

Before implementing a feature of any real size, write `docs/<feature-slug>/` from the templates in `ai-workflow/templates/` (sibling repo):
- `requirements.md` — acceptance criteria as EARS-pattern statements (`WHEN...THE SYSTEM SHALL...`, `IF...THEN THE SYSTEM SHALL...`, etc.), each with a stable ID like `R1.1` — not free-form prose. See `docs/semantic-car-search/requirements.md` for a worked example.
- `design.md` — technical approach, and a "Requirements traceability" table citing every `R#.#` from requirements.md. A requirement with no design element is a drafting gap to flag, not skip.
- `tasks.md` — the tracker `implement` reads. Each row's `Reqs` column cites which `R#.#` it implements, so review checks the diff against the actual acceptance criterion, not a paraphrase of the task title.

Small fixes don't need this ceremony. A new feature or anything with real design decisions does.

## Lessons learned

When something breaks in a non-obvious way, or a wrong assumption costs real time, add an entry to `docs/lessons/` and link it from `INDEX.md`, tagged by category. Write it from evidence (the actual error, the actual fix) — not a vague summary. The point is that neither a human nor an AI session repeats the same mistake on this codebase twice.

## Gate stack (must pass before anything is considered done)

- `mvn verify` — build + full test suite
- PMD, SpotBugs — static analysis, configured to fail the build on violation
- ArchUnit — architecture rules, runs as part of the test suite
- OWASP dependency-check — planned, not yet wired in

None of these are advisory. If one fails, the change isn't done, regardless of what wrote it.

## Running locally

```bash
docker compose -f backend/docker-compose.yml up -d   # local OpenSearch
cd backend && ./mvnw spring-boot:run                  # backend on :8080
cd frontend && npm install && npm run dev              # frontend on :5173
```

## Conventions

- Backend package root: `dev.bloiko.carsearcher`
- Tests live next to the code they cover (`src/test/java` mirroring `src/main/java`)
- No feature flags or backwards-compatibility shims for a project this size — change code directly
