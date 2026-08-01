# Lessons learned

Flat, tagged index of things that broke non-obviously on this codebase, so the same mistake doesn't get repeated — by a human or an AI session. One entry per file in this directory; add a row here when you add one.

Write entries from evidence (the actual error, the actual fix), not a vague summary of what went wrong.

| Date | Tags | Summary | File |
|---|---|---|---|
| 2026-08-01 | subagent-self-report, process | Subagent reports disagreed about who wrote what in Car.java; the diff + an independent gate re-run resolved it, the reports never could have | [2026-08-01-subagent-self-report-mismatch.md](2026-08-01-subagent-self-report-mismatch.md) |
| 2026-08-01 | opensearch-java, mocked-test-blind-spot, search-broken | SearchResponse.documents() silently returned empty despite a genuine hit -- every test mocked the exact broken method, so 15/15 green tests shipped search that returned nothing against a real cluster | [2026-08-01-documents-silently-empty.md](2026-08-01-documents-silently-empty.md) |
| 2026-08-01 | cors, index-bootstrap, field-name-mismatch, first-live-run | First real run of backend+frontend+OpenSearch together surfaced 3 more gaps invisible to mocked tests: missing CORS (fixed), no index bootstrap (BOH-20), photo_urls mapping mismatch (BOH-21) | [2026-08-01-first-full-stack-run.md](2026-08-01-first-full-stack-run.md) |
| 2026-08-02 | workflow-mjs, git, claim, stale-branch, sdd-docs | `claim` branches from `origin/main`, not local `main` — a locally-committed-but-unpushed SDD-docs commit got silently dropped from the new feature branch until an implementer subagent reported the docs directory didn't exist | [2026-08-02-claim-branches-from-origin-not-local-main.md](2026-08-02-claim-branches-from-origin-not-local-main.md) |
