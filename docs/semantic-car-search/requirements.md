# Semantic car search — requirements

## Problem

Keyword search fails on queries like "reliable family SUV under 30k" or "fuel efficient commuter car" — none of those words may appear in a listing that's actually a great match. The search needs to understand intent, not just match tokens.

## EARS pattern legend

Every acceptance criterion below is one of these five patterns, each with a stable ID (`R<story>.<criterion>`) so `design.md` and `tasks.md` can cite it.

| Pattern | Form | Use for |
|---|---|---|
| Ubiquitous | `THE SYSTEM SHALL <response>` | Always-true invariants |
| Event-driven | `WHEN <trigger> THE SYSTEM SHALL <response>` | Normal-flow behavior |
| State-driven | `WHILE <state> THE SYSTEM SHALL <response>` | Behavior only valid during a state |
| Unwanted behavior | `IF <condition> THEN THE SYSTEM SHALL <response>` | Error handling, edge cases |
| Optional feature | `WHERE <feature present> THE SYSTEM SHALL <response>` | Conditional/configurable behavior |

## User Story 1 — Semantic relevance

As a car shopper, I want free-text search to understand intent, not just keywords, so that queries like "reliable family SUV under 30k" surface relevant listings even without exact word matches.

**Acceptance criteria:**
- R1.1 — WHEN a user submits a free-text query THE SYSTEM SHALL return cars ranked by relevance to the query's meaning, not just literal keyword overlap.
- R1.2 — IF a query matches no listings THEN THE SYSTEM SHALL return an empty result list with a 200 response, not an error.

## User Story 2 — Combined filtering

As a car shopper, I want to combine a free-text query with structured filters, so that I can narrow semantic results by concrete constraints like price or year.

**Acceptance criteria:**
- R2.1 — WHEN a search request includes both a free-text query and structured filters (make, model, year range, price range, mileage range) THE SYSTEM SHALL apply both together, not just one.

## User Story 3 — Usable result payload

As the frontend rendering search results, I want each result to carry enough of the listing's data, so that I can render a result card without a second request.

**Acceptance criteria:**
- R3.1 — WHEN search returns results THE SYSTEM SHALL include, for each result, make, model, year, price, and a short description.

## User Story 4 — Acceptable latency at dev scale

As a car shopper, I want search to feel instant, so that I don't hesitate to refine my query.

**Acceptance criteria:**
- R4.1 — WHILE the catalog is at local-dev scale (low thousands of listings) THE SYSTEM SHALL respond to a search request in well under one second. Not a performance target worth over-engineering past that scale yet.

## Explicitly out of scope for now

- Personalization / search history
- Autocomplete or query suggestions
- Any ranking signal beyond text relevance (e.g. popularity, recency) — revisit once there's real usage data
