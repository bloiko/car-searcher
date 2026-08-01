# Search filters — design

## Requirements traceability

| Requirement | Design element |
|---|---|
| R1.1 | `CarSearchService` adds a `range` filter clause on `price` (`lte: priceMax`) |
| R1.2 | `CarSearchRequest.Filters` compact constructor rejects negative `priceMax` |
| R2.1 | `range` filter clause on `year` (`gte: yearMin`) |
| R3.1 | `range` filter clause on `mileage` (`lte: mileageMax`) |
| R3.2 | `Filters` compact constructor rejects negative `mileageMax` |
| R4.1 | `term` filter clause on `make` (exact match against the existing `keyword` field) |
| R5.1 | All provided filters + the `multi_match` query combined in one `bool` query |
| R5.2 | No filters provided → `bool` query's `filter` clause list is empty, behavior identical to today's plain `multi_match` |
| R6.1 | Frontend adds filter inputs to the search form, included in the POST body |

## Data model

`CarSearchRequest.Filters` (currently `priceMax` only) gains three fields:
```
priceMax    Float     -- existing; max price, inclusive
yearMin     Integer   -- min model year, inclusive
mileageMax  Integer   -- max mileage, inclusive
make        String    -- exact match against Car.make
```
All remain optional (`null` = not applied). Validation (non-negative `priceMax`/`mileageMax`) lives in `Filters`' own compact constructor, same style as `Car`'s.

## Query approach

`CarSearchService.search` changes signature to accept `CarSearchRequest.Filters` alongside the query string, and builds an OpenSearch `bool` query instead of a bare `multi_match`:
- `must`: the existing `multi_match` over description/make/model — this is what drives relevance *scoring*.
- `filter`: one clause per provided filter (`range` for price/year/mileage, `term` for make) — filter-context clauses constrain the result set **without affecting relevance score**, which is the correct semantics here: a cheaper car within budget shouldn't outrank a better-matching one just because the filter happened to run in `must` instead. Only clauses for filters that are actually present are added — an absent filter contributes nothing, which is what makes R5.2 (no filters = today's behavior) fall out for free rather than needing a special case.

`CarSearchController` passes `request.filters()` through to the service call.

## Open decision: make-filter case sensitivity

`make` is indexed as a `keyword` field (exact, case-sensitive match) — a `term` filter on `"toyota"` will not match a stored `"Toyota"`. Two ways to fix this properly, neither done here:
- A `normalizer` on the `make` field's OpenSearch mapping (lowercases at index time and query time) — the "correct" fix, but requires re-indexing existing data.
- Lowercase both the filter input and add a parallel `make.lowercase` sub-field.

For this task: exact case-sensitive match, matching whatever case the data was ingested with. Flagged here rather than silently shipping confusing behavior — worth a follow-up if it turns out to actually bite users (depends on how consistent the eventual real ingestion pipeline, BOH-16, is about casing).

## Frontend

Add three inputs (max price, min year, max mileage) and a make field to the existing search form in `+page.svelte`, included in the `POST /api/cars/search` body's `filters` object alongside `query`. Empty/unset inputs are omitted from the request (not sent as `null` or empty string) so `R5.2` holds from the client side too.

## Testing strategy

- `Filters` validation: unit test per negative-value rejection, same pattern as `Car`'s.
- `CarSearchService`: mocked-client tests (no Docker) asserting the built `SearchRequest`'s `bool` query has the right `filter` clauses for a given `Filters` value, and that an empty `Filters` produces no `filter` clauses at all (proving R5.2, not just asserting it by inspection).
- Frontend: component test that submitting with filter values set includes them in the mocked `fetch` call's body.

## Open decisions

Just the make-filter case-sensitivity one above.
