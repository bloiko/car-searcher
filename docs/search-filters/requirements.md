# Search filters — requirements

BOH-14: filters exist in the request DTO (`priceMax` only) but aren't applied yet. Budget/year/mileage are non-negotiable constraints for a car buyer, not just relevance signals.

## EARS pattern legend

| Pattern | Form | Use for |
|---|---|---|
| Ubiquitous | `THE SYSTEM SHALL <response>` | Always-true invariants |
| Event-driven | `WHEN <trigger> THE SYSTEM SHALL <response>` | Normal-flow behavior |
| State-driven | `WHILE <state> THE SYSTEM SHALL <response>` | Behavior only valid during a state |
| Unwanted behavior | `IF <condition> THEN THE SYSTEM SHALL <response>` | Error handling, edge cases |
| Optional feature | `WHERE <feature present> THE SYSTEM SHALL <response>` | Conditional/configurable behavior |

## User Story 1 — Price ceiling

As a car shopper, I want to set a maximum price, so that I only see listings I can afford.

**Acceptance criteria:**
- R1.1 — WHEN a search request includes `filters.priceMax` THE SYSTEM SHALL exclude listings priced above that value from the results.
- R1.2 — IF `filters.priceMax` is negative THEN THE SYSTEM SHALL reject the request with a validation error.

## User Story 2 — Minimum model year

As a car shopper, I want to set a minimum model year, so that I don't see cars older than I'm willing to consider.

**Acceptance criteria:**
- R2.1 — WHEN a search request includes `filters.yearMin` THE SYSTEM SHALL exclude listings with `year` below that value.

## User Story 3 — Mileage ceiling

As a car shopper, I want to set a maximum mileage, so that I avoid high-mileage cars.

**Acceptance criteria:**
- R3.1 — WHEN a search request includes `filters.mileageMax` THE SYSTEM SHALL exclude listings with `mileage` above that value.
- R3.2 — IF `filters.mileageMax` is negative THEN THE SYSTEM SHALL reject the request with a validation error.

## User Story 4 — Make filter

As a car shopper, I want to filter to a specific make, so that I only see cars from manufacturers I'm interested in.

**Acceptance criteria:**
- R4.1 — WHEN a search request includes `filters.make` THE SYSTEM SHALL exclude listings whose `make` does not match it exactly.

## User Story 5 — Filters combine with the query and with each other

As a car shopper, I want to combine a free-text query with one or more filters in one request, so that I can narrow results precisely without losing relevance ranking.

**Acceptance criteria:**
- R5.1 — WHEN a search request includes both a query and one or more filters THE SYSTEM SHALL apply all of them together (a result must satisfy the query AND every provided filter — AND semantics, not OR).
- R5.2 — WHILE no filters are provided THE SYSTEM SHALL behave exactly as it does today (query-only search, unaffected by this feature).

## User Story 6 — Filter controls in the UI

As a car shopper using the search page, I want visible controls for these filters, so that I don't need to know the API shape to use them.

**Acceptance criteria:**
- R6.1 — WHEN a user sets one or more filter values and submits a search THE SYSTEM SHALL include them in the request sent to the backend.

## Explicitly out of scope

- Filters not named above (price minimum, year maximum, mileage minimum) — one bound per dimension is the MVP; symmetric ranges are a future extension if the single bound proves insufficient.
- Make-filter case sensitivity is an open decision, not silently resolved here — see `design.md`.
- Saving/persisting a user's filter preferences between sessions.
