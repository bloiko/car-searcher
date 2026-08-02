# Listing detail page — design

## Requirements traceability

| Requirement | Design element |
|---|---|
| R1.1 | New `GET /api/cars/{id}` endpoint on `CarSearchController` (or a new `CarController` if that fits the existing package shape better), backed by a new `CarLookupService`/`OpenSearchClient.get(...)` call — returns `Car` directly, 200 |
| R1.2 | 404 via Spring's standard `ResponseEntity.notFound()` when the OpenSearch `get` returns not-found |
| R1.3 | Frontend: each result card wrapped in a link to `/cars/{id}` |
| R2.1 | Frontend: new `/cars/[id]/+page.svelte` route rendering all fields, all photos |
| R3.1 | `Car` gains an optional `sourceUrl` field; detail page renders an `<a target="_blank" rel="noopener noreferrer">` when present |
| R3.2 | Same conditional rendering — absent when `sourceUrl` is null/blank |

## Data model

`Car` gains one optional field:
```
sourceUrl   String  -- nullable, the original listing's URL (e.g. an AutoRia page).
                        No format validation beyond "not blank if present" -- a
                        malformed URL from a future ingestion pipeline is that
                        pipeline's bug to catch, not this record's job to police.
```
Added via a **convenience delegating constructor** (`Car(id, make, model, year, price, mileage, transmission, description, photoUrls)` → delegates to the canonical 9-arg... now 10-arg constructor with `sourceUrl = null`), the same pattern `CarSearchRequest.Filters` already uses for its own optional-field convenience constructor. This means **no existing `Car`-constructing test fixture needs to change** — the blast radius stays at zero for this field, unlike `transmission`, which was made required and touched every fixture.

`CarIndexMapping` gains `sourceUrl` as a `keyword` field (exact value storage; not intended to be searched/analyzed, matching `photoUrls`' treatment).

## Backend endpoint

`GET /api/cars/{id}`:
- Look up the document by id via `OpenSearchClient.get(GetRequest, Car.class)` (a new, small `CarLookupService` or a method added to an existing service — check whether `CarIndexingService`/`CarSearchService` is the more natural home, or whether a new class is warranted given single-responsibility; lean toward a new small service given `CarSearchService` is specifically about *searching*, not point lookups).
- `GetResponse.found()` → `false` maps to `ResponseEntity.notFound().build()`; `true` maps to `ResponseEntity.ok(response.source())`.
- Returns `Car` directly (not a narrower DTO like `CarSearchResult`) — a detail page is explicitly the place that wants *everything*, so there's no "narrower than Car" contract to maintain here the way there is for search results.

## Frontend

- New route `frontend/src/routes/cars/[id]/+page.svelte` (SvelteKit dynamic route). On load, `fetch(`http://localhost:8080/api/cars/${id}`)`; 404 renders a simple "Listing not found" state (reuse the existing empty-state styling pattern); network/other errors reuse the existing error-banner styling pattern.
- Displays: photo gallery (all `photoUrls`, simple grid/stack — no carousel library, matching this project's "no dependency for something CSS can do" bias), price (prominent, same visual weight as the search-result card), make/model/year, mileage, transmission, full description.
- `sourceUrl` link, rendered only when present, styled as a primary call-to-action (this is the ticket's "act on a listing" requirement — it should look like the main thing to do on the page, not a footnote).
- Each result card in `+page.svelte` (the search page) becomes a link (`<a href="/cars/{result.id}">`) wrapping the existing card markup — reuses all existing card styling, only adds navigation.

## Testing strategy

- Backend: new service/lookup tests (mocked `OpenSearchClient.get(...)`, found/not-found cases), controller test for 200/404, `Car`'s convenience constructor (defaults `sourceUrl` to null, matches `Filters`' pattern — one test mirroring how `Filters(Float priceMax)`'s convenience constructor might already be tested, or a fresh one if it isn't).
- `CarIndexMapping`: extend the existing mapping test to assert `sourceUrl` is a keyword field.
- Frontend: component test for the detail route rendering all fields from a mocked fetch response, a 404 test, a test that `sourceUrl` renders a link when present and doesn't when absent, and a test that clicking/navigating from a search-result card links to the right `/cars/{id}` href.

## Open decisions

None.
