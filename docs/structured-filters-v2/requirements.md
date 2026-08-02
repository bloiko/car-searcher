# Structured filters v2 (model & transmission) — requirements

BOH-22, first step of the semantic-search-strategy roadmap (see the BOH-19 Linear thread for the full discussion). The insight driving this: dense embeddings are bad at exact discrimination on proper nouns and structured attributes ("Skoda Octavia" vs. "Skoda Superb" sit uncomfortably close together in vector space), so classic attributes the user knows exactly (make, model, year, mileage, transmission) should be hard filters, not routed through fuzzy free-text matching. The free-text box is reframed as being for the *residual* — trim names, features, vibe ("Laurin & Klement", "Sportline") — the part that genuinely benefits from fuzzy/semantic matching later (BOH-27, blocked on this task).

## EARS pattern legend

| Pattern | Form | Use for |
|---|---|---|
| Ubiquitous | `THE SYSTEM SHALL <response>` | Always-true invariants |
| Event-driven | `WHEN <trigger> THE SYSTEM SHALL <response>` | Normal-flow behavior |
| State-driven | `WHILE <state> THE SYSTEM SHALL <response>` | Behavior only valid during a state |
| Unwanted behavior | `IF <condition> THEN THE SYSTEM SHALL <response>` | Error handling, edge cases |
| Optional feature | `WHERE <feature present> THE SYSTEM SHALL <response>` | Conditional/configurable behavior |

## User Story 1 — Filter by exact model

As a car shopper, I want to filter by exact model, so that I can narrow to the specific model I'm interested in without typing it into a fuzzy search box.

**Acceptance criteria:**
- R1.1 — WHEN a search request includes `filters.model` THE SYSTEM SHALL exclude listings whose `model` does not match it exactly.

## User Story 2 — Filter by transmission

As a car shopper, I want to filter by transmission type, so that I only see cars with the transmission I actually want to drive.

**Acceptance criteria:**
- R2.1 — WHEN a search request includes `filters.transmission` THE SYSTEM SHALL exclude listings whose `transmission` does not match it exactly.
- R2.2 — THE SYSTEM SHALL require every `Car` to have a non-blank `transmission` value, matching the existing required-field convention for `make`/`model`.

## User Story 3 — Classic filters are the primary way to narrow results

As a car shopper, I want the concrete, well-known attributes (make, model, year, mileage, transmission) to be immediately visible and easy to set, so that I don't have to dig through a collapsed drawer to specify things I already know exactly.

**Acceptance criteria:**
- R3.1 — THE SYSTEM SHALL present the classic filter controls (make, model, year, mileage, transmission) as immediately visible on page load, not hidden behind a collapsed drawer/toggle.

## User Story 4 — Free text is reframed as residual/descriptive search

As a car shopper, I want the free-text box to be clearly for descriptive details rather than a general search box, so that I understand it's for trim names or features, not for specifying make/model again.

**Acceptance criteria:**
- R4.1 — THE SYSTEM SHALL label the free-text search input to indicate it is for descriptive/unstructured details, distinct from the classic attribute filters.

## Explicitly out of scope

- Actual semantic/k-NN matching for the free-text box — it stays today's `multi_match` keyword scaffold; real semantic matching is BOH-27, which depends on this task's filter split existing first.
- Range filters for model-year maximum or mileage minimum (symmetric ranges) — one bound per dimension remains the pattern, matching the existing `search-filters` feature's own scope decision.
- Backend enum/allowed-value validation on `transmission` — kept as an unconstrained non-blank string, exact-match `term` filter, same pattern as `make` (no backend enum today either). The frontend offers a fixed dropdown as the practical constraint; not duplicated as a backend enum to avoid a backend deploy every time a new transmission variant shows up in real data.
