# Listing detail page — requirements

BOH-15: "Search results are currently a dead end -- no destination to click into for the full description, photos, or to act on a listing (contact/save/etc)."

## EARS pattern legend

| Pattern | Form | Use for |
|---|---|---|
| Ubiquitous | `THE SYSTEM SHALL <response>` | Always-true invariants |
| Event-driven | `WHEN <trigger> THE SYSTEM SHALL <response>` | Normal-flow behavior |
| State-driven | `WHILE <state> THE SYSTEM SHALL <response>` | Behavior only valid during a state |
| Unwanted behavior | `IF <condition> THEN THE SYSTEM SHALL <response>` | Error handling, edge cases |
| Optional feature | `WHERE <feature present> THE SYSTEM SHALL <response>` | Conditional/configurable behavior |

## User Story 1 — View full listing details

As a car shopper, I want to click a search result and see its full details, so that I don't have to guess from the truncated card.

**Acceptance criteria:**
- R1.1 — WHEN a client requests `GET /api/cars/{id}` for an existing car THE SYSTEM SHALL return that car's full details with a 200 response.
- R1.2 — IF the requested `id` does not exist THEN THE SYSTEM SHALL return a 404 response.
- R1.3 — WHEN a user clicks a search result card THE SYSTEM SHALL navigate to that listing's detail page.

## User Story 2 — The detail page shows what the card doesn't

As a car shopper, I want the detail page to show everything the compact card leaves out, so clicking through is actually worth it.

**Acceptance criteria:**
- R2.1 — THE SYSTEM SHALL display, on the detail page: every photo in `photoUrls` (not just the first), make, model, year, price, mileage, transmission, and the full (untruncated) description.

## User Story 3 — A way to act on a listing, when one exists

As a car shopper, I want a way to reach the original listing when one exists, so a detail page isn't just a dead end with more text.

**Acceptance criteria:**
- R3.1 — WHERE a listing has a non-blank `sourceUrl` THE SYSTEM SHALL display a link on the detail page that opens it in a new tab.
- R3.2 — WHERE a listing has no `sourceUrl` THE SYSTEM SHALL omit the outbound link entirely — not render a broken, empty, or disabled one.

## Explicitly out of scope

- An in-app contact form / messaging the seller — no seller/contact data model exists, and building one is a separate, larger feature.
- Save/favorite a listing — needs a user-identity concept this app doesn't have yet.
- Populating real `sourceUrl` values — that's BOH-16/18/24 (real data ingestion); this ticket only builds the plumbing so it works once real data has it.
