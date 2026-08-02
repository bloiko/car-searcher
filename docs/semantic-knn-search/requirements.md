# Semantic k-NN search — requirements

BOH-27, final step of the semantic-search-strategy roadmap. Depends on BOH-22 (structured filters, done) and BOH-26 (embedding indexing, done: every car has `description_vector` populated automatically via the ingest pipeline). This ticket replaces the placeholder keyword `multi_match` on the residual free-text query with real k-NN semantic search, while leaving the structured filters exactly as they already work today.

## EARS pattern legend

| Pattern | Form | Use for |
|---|---|---|
| Ubiquitous | `THE SYSTEM SHALL <response>` | Always-true invariants |
| Event-driven | `WHEN <trigger> THE SYSTEM SHALL <response>` | Normal-flow behavior |
| State-driven | `WHILE <state> THE SYSTEM SHALL <response>` | Behavior only valid during a state |
| Unwanted behavior | `IF <condition> THEN THE SYSTEM SHALL <response>` | Error handling, edge cases |
| Optional feature | `WHERE <feature present> THE SYSTEM SHALL <response>` | Conditional/configurable behavior |

## User Story 1 — Free-text search understands meaning, not just keywords

As a car shopper, I want the residual free-text box (trim, features, vibe — "Sportline", "Laurin & Klement", "leather seats") to match on meaning, so that a listing describing "top trim, leather, sport-tuned suspension" surfaces for a query like "Sportline" even without that literal word.

**Acceptance criteria:**
- R1.1 — WHEN a search request includes a non-blank `query` THE SYSTEM SHALL rank results by semantic similarity between the query and each listing's `description_vector`, using the same embedding model that indexed the vectors (BOH-23/BOH-26).
- R1.2 — IF `query` matches no listings within the configured similarity threshold THEN THE SYSTEM SHALL return an empty result list with a 200 response, not an error (unchanged from today's behavior).

## User Story 2 — Structured filters keep working exactly as before

As a car shopper, I want my classic filters (make, model, year, mileage, transmission, price) to keep narrowing results exactly like they do today, unaffected by the search box becoming semantic.

**Acceptance criteria:**
- R2.1 — WHEN a search request includes both `query` and one or more structured filters THE SYSTEM SHALL apply the filters in `filter` context (unscored, exact) exactly as today — this ticket does not change `buildFilterClauses` or its behavior.
- R2.2 — THE SYSTEM SHALL continue rejecting a blank `query` with a validation error (`CarSearchRequest`'s existing `@NotBlank` constraint) — unaffected by this ticket, confirmed already true and not to be weakened.

## Explicitly out of scope

- Hybrid scoring (BM25 + vector combined) — Option C from the original strategy discussion, deliberately deferred until real usage (post BOH-24, seed real inventory) shows plain k-NN ranking isn't good enough.
- Surfacing "why this matched" to the user — not part of this round.
- Tuning `k` (how many nearest neighbors considered) or the similarity threshold beyond a reasonable default — this is exactly the kind of thing that needs real inventory (BOH-24) to tune sensibly; ship a reasonable default, revisit with real data.
