# Car photos — design

## Requirements traceability

| Requirement | Design element |
|---|---|
| R1.1 | `Car.photoUrls` field, included wherever `Car`/search results are serialized |
| R1.2 | Frontend result card renders `photoUrls[0]` as an `<img>` |
| R1.3 | Frontend falls back to a placeholder image when `photoUrls` is empty |
| R2.1 | `Car` record gains `List<String> photoUrls`; `POST /api/cars` already accepts a full `Car` body, so no new endpoint needed |
| R2.2 | `Car`'s compact constructor rejects blank entries in `photoUrls`, same style as its existing id/make/model/price/mileage checks |

## Data model

`Car` (record, `dev.bloiko.carsearcher.car`) gains one field:
```
photoUrls   List<String>   -- may be empty, never null, no blank entries
```

`CarIndexMapping` gains `photo_urls` as a `keyword` field (not analyzed/searchable text — these are opaque URLs, not something a user should be able to full-text search). A `List<String>` maps to a multi-value keyword field in OpenSearch automatically; no special array handling needed in the mapping itself.

`CarSearchResult` (the response shape from `POST /api/cars/search`, currently id/make/model/year/price/description) gains `photoUrls` too, so the frontend has something to render without a second request — consistent with the existing "usable result payload" requirement (R3.1 in `semantic-car-search`).

## Frontend

The search result card (`frontend/src/routes/+page.svelte`) renders `result.photoUrls[0]` if present, otherwise a placeholder image (a static asset shipped with the app, not a network fetch to some placeholder service — keeps it working offline/local-only).

## Error handling

A blank string in `photoUrls` (e.g. `[""]`) is rejected at construction time by `Car`'s compact constructor, the same way a blank `make` already is — surfaces as a 400 from `POST /api/cars`, not a silently-stored broken image reference.

## Testing strategy

- `Car`'s validation: unit test, same pattern as the existing `CarTest` (rejects a blank entry in `photoUrls`).
- `CarSearchResult`/response mapping: unit test that `photoUrls` survives the mapping from `Car` through to the JSON response (mocked OpenSearch client, same pattern as `CarSearchServiceTest`).
- Frontend: component test (Vitest + Testing Library, same pattern as the existing search-page test) — a result with a photo renders an `<img>` with that URL; a result with no photos renders the placeholder image instead.

## Open decisions

None — this is a small, additive feature on top of an already-established pattern in every layer it touches.
