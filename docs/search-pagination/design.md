# Search pagination — design

## Requirements traceability

| Requirement | Design element |
|---|---|
| R1.1 | `CarSearchService` adds `.from(page * pageSize).size(pageSize)` to the `SearchRequest.Builder`, alongside the existing `must`/`filter`/`sort` |
| R1.2 | `CarSearchRequest` fields `page`/`pageSize` are `Integer`, nullable; `CarSearchService` substitutes defaults (`page=0`, `pageSize=20`) when null, same pattern as `sort` defaulting to no-op when absent |
| R1.3 | `CarSearchRequest`'s compact constructor rejects `pageSize` outside `1..100` |
| R1.4 | `CarSearchRequest`'s compact constructor rejects negative `page` |
| R2.1 | `CarSearchService.search(...)` return type changes from `List<Car>` to a small `SearchResult(List<Car> cars, long total)` record; `total` comes from `SearchResponse.hits().total().value()` |
| R3.1 | Frontend: `page` `$state`, included in the request; a page-change handler re-submits with the new page |
| R3.2 | Frontend: Previous disabled at page 0; Next disabled once `(page + 1) * pageSize >= total` |

## Data model

`CarSearchRequest` gains two top-level fields, siblings to `filters`/`sort` (pagination is a request-shape concern, not a filter):
```
page       Integer  -- 0-indexed, null = 0
pageSize   Integer  -- null = 20, validated to 1..100 when present
```

`CarSearchService.search(...)`'s return type changes from `List<Car>` to a new small record:
```java
record SearchResult(List<Car> cars, long total) {}
```
`CarSearchController` unpacks this into the response.

`CarSearchResponse` gains a `total` field alongside `results`:
```
{ "results": [...], "total": 137 }
```

## Query approach

`SearchRequest.Builder` gains `.from(page * pageSize).size(pageSize)`, computed after defaulting. No change to `must`/`filter`/`sort` construction — pagination is orthogonal, same as sort was to filters.

Total count: `SearchResponse<Car>.hits().total().value()` — OpenSearch 2.x tracks total hits accurately by default (no `track_total_hits` override needed at this app's scale; revisit only if real usage shows the 10,000-hit default cap matters, which won't happen before this app has vastly more inventory than planned).

## Frontend

- `page` `$state`, defaulting to `0`, reset to `0` whenever the query/filters/sort change (a fresh search should start at page 1, not resume wherever the user last was).
- Pagination controls (Previous / "Page N of M" / Next) rendered below the result grid, only when `total > pageSize` (no controls needed for a single page).
- Next/Previous re-submit the search with the new `page` value — same `handleSubmit`-shaped fetch, not a separate code path.

## Testing strategy

- `CarSearchRequest`: validation tests for `pageSize` outside `1..100`, negative `page`, mirroring `Filters`' existing negative-value rejection tests.
- `CarSearchService`: test that `page`/`pageSize` map to the right `from`/`size` on the built `SearchRequest`; test that null `page`/`pageSize` default to `0`/`20`; test that `total` is read from the mocked response and returned via `SearchResult`.
- `CarSearchController`/response mapping: existing tests likely need updating for the `search(...)` signature/return-type change — check what exists.
- Frontend: page-change re-submits with the right `page`; Next/Previous disabled at the boundaries; page resets to 0 on a new query.

## Open decisions

None.
