# Search UI redesign — requirements

BOH-19: the search page currently renders with zero stylesheet — browser-default Times New Roman, five flat unlabeled-hierarchy inputs, no card layout, no visually distinct system-status feedback. Competitor benchmarking (CarGurus, AutoTrader, Carvana) and the redesign proposal are recorded on the BOH-19 Linear issue; this file scopes what actually gets built against the existing `/api/cars/search` contract, with no backend changes.

## EARS pattern legend

| Pattern | Form | Use for |
|---|---|---|
| Ubiquitous | `THE SYSTEM SHALL <response>` | Always-true invariants |
| Event-driven | `WHEN <trigger> THE SYSTEM SHALL <response>` | Normal-flow behavior |
| State-driven | `WHILE <state> THE SYSTEM SHALL <response>` | Behavior only valid during a state |
| Unwanted behavior | `IF <condition> THEN THE SYSTEM SHALL <response>` | Error handling, edge cases |
| Optional feature | `WHERE <feature present> THE SYSTEM SHALL <response>` | Conditional/configurable behavior |

## User Story 1 — A designed page, not browser defaults

As a car shopper, I want the search page to look and feel like a real product, so that I can read and use it comfortably.

**Acceptance criteria:**
- R1.1 — THE SYSTEM SHALL render the search page using a defined color, typography, and spacing system (no unstyled browser-default rendering).
- R1.2 — THE SYSTEM SHALL present the free-text search input as the primary, most visually prominent control on the page, with the price/year/mileage/make filter inputs visually secondary to it.

## User Story 2 — Active filters are visible and removable

As a car shopper, I want to see which filters are currently applied and clear them individually, so that I don't have to hunt through the form to figure out why my results are narrow.

**Acceptance criteria:**
- R2.1 — WHEN a user sets one or more filter values THE SYSTEM SHALL display each set filter as a labeled, dismissible chip.
- R2.2 — WHEN a user dismisses a filter's chip THE SYSTEM SHALL clear that filter's input value.

## User Story 3 — Scannable result cards

As a car shopper, I want search results shown as clear cards, so that I can compare listings at a glance.

**Acceptance criteria:**
- R3.1 — WHEN search results are returned THE SYSTEM SHALL render each result as a card containing its photo (or a fallback graphic when `photoUrls` is empty), price, make/model/year, and mileage, with price visually the most prominent figure on the card.

## User Story 4 — System status is visible

As a car shopper, I want to see when the app is searching, found nothing, or failed, so that I'm not left staring at a blank area guessing what happened.

**Acceptance criteria:**
- R4.1 — WHILE a search request is in flight THE SYSTEM SHALL display a visually distinct loading indicator (`role="status"`) in place of stale results.
- R4.2 — IF a search returns zero results THEN THE SYSTEM SHALL display a visually distinct empty-state message, not a blank area.
- R4.3 — IF a search request fails THEN THE SYSTEM SHALL display a visually distinct error message (`role="alert"`), not a blank area or a silent failure.

## User Story 5 — Works on a phone

As a car shopper on a phone, I want the layout to adapt to a small screen, so the app is usable without a desktop.

**Acceptance criteria:**
- R5.1 — WHERE the viewport is narrower than 720px THE SYSTEM SHALL render the result cards in a single column.

## Explicitly out of scope

- Per-result "why this matched" explanation text — would require a backend match-reasoning feature that doesn't exist; today's search is a keyword `multi_match` scaffold (see `docs/semantic-car-search/design.md`), not true semantic search yet, so there's no real signal to explain.
- Deal-quality / market-pricing badges (CarGurus pattern) — no market pricing data source exists.
- Photo carousels or a 360° viewer (Carvana pattern) — `Car.photoUrls` already exists but multi-photo browsing UI is a separate feature.
- Any change to `CarSearchRequest`/`CarSearchResponse` or backend query logic — this is a frontend-only presentation change against the existing contract.
- Saving/persisting filter or theme preferences between sessions.
