# Structured filters v2 (model & transmission) — design

## Requirements traceability

| Requirement | Design element |
|---|---|
| R1.1 | `CarSearchRequest.Filters` gains `model` (String); `CarSearchService` adds a `term` filter clause on `model`, same pattern as the existing `make` clause |
| R2.1 | `CarSearchRequest.Filters` gains `transmission` (String); `CarSearchService` adds a `term` filter clause on `transmission` |
| R2.2 | `Car`'s compact constructor gains a non-blank check for `transmission`, matching `make`/`model`'s existing validation; `CarIndexMapping` gains `transmission` as a `keyword` field |
| R3.1 | Frontend: classic filter inputs (make, model, year, mileage, transmission) move out of the collapsible `.filters-drawer` into the always-visible form body |
| R4.1 | Frontend: the search `<label>`/placeholder text changes from generic "Search" to something indicating residual/descriptive intent (e.g. "Additional details" label, placeholder like "e.g. Sportline, Laurin & Klement, leather seats") |

## Data model

`Car` gains a required field:
```
transmission  String  -- non-blank, e.g. "Automatic"/"Manual"/"CVT" — free string,
                          no backend enum (see requirements.md "Explicitly out of scope")
```
Added the same way `make`/`model` are validated today — a non-blank check in `Car`'s compact constructor. This is a **required** field, not optional, matching the existing convention that `make`/`model`/`price`/`mileage` are all required and only `photoUrls` defaults to empty. Every existing call site that constructs a `Car` (production and test fixtures) needs a `transmission` value added — this is the one place this task has real blast radius, and it's deliberate: a car listing without a transmission type is incomplete data, the same way a listing without a make would be.

`CarIndexMapping` gains `transmission` as a `keyword` field (exact-match term filter, same as `make`/`model`).

`CarSearchRequest.Filters` gains two fields, both optional (`null` = not applied), same style as the existing four:
```
model         String   -- exact match against Car.model
transmission  String   -- exact match against Car.transmission
```

## Query approach

`CarSearchService.buildFilterClauses` gains two more `if (filters.X() != null)` branches, each adding a `term` filter clause — identical shape to the existing `make` clause. No change to the `must`/relevance-scoring side of the query in this task.

## Frontend

Two changes to `+page.svelte`:

1. **Classic filters become primary, not drawer-hidden.** The `.filters-drawer` `{#if filtersOpen}` block and the "Filters" toggle button are removed; `price-max-input`, `year-min-input`, `mileage-max-input`, `make-input`, and the two new inputs (`model-input`, `transmission-input` — a `<select>` with a small fixed option set for transmission, since that's a small closed-ish domain even though the backend doesn't enforce it) render unconditionally in the form body, above or alongside the search row. This is a direct reversal of the BOH-19 redesign's earlier choice to make the free-text box the single most prominent control — that choice was right for a keyword-search-only product; it's wrong for this hybrid-filter strategy, which is the whole point of BOH-22.
2. **Free text reframed.** `<label for="search-input">Search</label>` becomes something like `<label for="search-input">Additional details</label>`, and the input gets a `placeholder` attribute with concrete examples (e.g. `"e.g. Sportline, Laurin & Klement, leather seats"`) so its purpose reads clearly as residual/descriptive, not "type your whole query here."

The filter-chips behavior (dismissible chips per set filter) stays — it now covers six filters (`model`, `transmission` added) instead of four, and continues to work unmodified in principle since it's already data-driven over the filter `$state` values (see `frontend/src/routes/+page.svelte`'s `filterChips` `$derived`).

## Testing strategy

- `Car`: one more non-blank-rejection test for `transmission`, mirroring the existing `make`/`model` tests.
- `CarSearchService`: filter-clause tests for `model` and `transmission`, mirroring the existing `make` test — assert the built `SearchRequest`'s `filter` list contains the right `term` clause when the field is set, and that it's absent when unset.
- Every existing test that constructs a `Car` fixture (`CarIndexingServiceTest`, `CarSearchServiceTest`, `CarSearchResponseTest`, `CarTest`'s non-rejection cases, `CarIndexingService` integration-shaped tests) needs a `transmission` value added to compile — this is expected, mechanical fallout from making the field required, not scope creep.
- Frontend: component tests asserting `model`/`transmission` are included in the request body when set (mirroring the existing `make` test), and that the classic filter inputs are queryable via `getByLabelText` without any prior drawer-opening step (since the drawer no longer exists) — several existing tests that currently do `fireEvent.click(screen.getByRole('button', { name: /filters/i }))` before touching a filter input will need that click removed, since the button won't exist anymore.

## Open decisions

None — the transmission-enum question is resolved in requirements.md's "Explicitly out of scope" (no backend enum, frontend-only fixed option set).
