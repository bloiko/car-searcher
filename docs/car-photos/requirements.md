# Car photos — requirements

BOH-13: Car has no image field at all — a car search app with text-only result cards is a hard sell.

## EARS pattern legend

| Pattern | Form | Use for |
|---|---|---|
| Ubiquitous | `THE SYSTEM SHALL <response>` | Always-true invariants |
| Event-driven | `WHEN <trigger> THE SYSTEM SHALL <response>` | Normal-flow behavior |
| State-driven | `WHILE <state> THE SYSTEM SHALL <response>` | Behavior only valid during a state |
| Unwanted behavior | `IF <condition> THEN THE SYSTEM SHALL <response>` | Error handling, edge cases |
| Optional feature | `WHERE <feature present> THE SYSTEM SHALL <response>` | Conditional/configurable behavior |

## User Story 1 — See photos in search results

As a car shopper, I want to see a photo of a listing in search results, so that I can judge its condition and appeal without leaving the page.

**Acceptance criteria:**
- R1.1 — WHEN a car has one or more photo URLs THE SYSTEM SHALL include them in the listing's data returned by search.
- R1.2 — WHEN a search result is rendered THE SYSTEM SHALL show the listing's first photo as a thumbnail.
- R1.3 — IF a listing has no photos THEN THE SYSTEM SHALL show a placeholder image, not a broken image or blank space.

## User Story 2 — Attach photos when indexing a listing

As whatever indexes a car (today: manual `POST /api/cars`; later: the real ingestion pipeline, BOH-16), I want to attach photo URLs to a listing, so that the search index has imagery to serve.

**Acceptance criteria:**
- R2.1 — WHEN a car is indexed THE SYSTEM SHALL accept zero or more photo URLs as part of the payload.
- R2.2 — IF a photo URL entry is blank THEN THE SYSTEM SHALL reject the listing with a clear validation error — consistent with `Car`'s existing validation pattern for its other fields (see `docs/semantic-car-search/`).

## Explicitly out of scope

- File upload / image storage infrastructure. Photos are referenced by external URL only — where those URLs come from (a real data source, a CDN) is BOH-16's concern, not this one.
- A full photo gallery/carousel. That belongs on the listing detail page (BOH-15), which doesn't exist yet. This feature only covers the thumbnail shown in search results.
- Image optimization, resizing, or a CDN layer.
- Deep URL-format validation (scheme/host checks). Only "not blank" is enforced here.
