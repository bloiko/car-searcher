# Search UI redesign — design

## Requirements traceability

| Requirement | Design element |
|---|---|
| R1.1 | Global CSS custom-property token set (color/type/spacing) added to `+page.svelte`'s `<style>` block (or a new `app.css` imported from `+layout.svelte` if shared across routes) — replaces the current zero-stylesheet page |
| R1.2 | Search input laid out larger/first in a sticky header; the four filter inputs moved into a secondary, initially-collapsed drawer |
| R2.1 | A chip is rendered per non-empty filter input's current `$state` value, derived reactively — no new state, just a derived view over the four existing `$state` variables |
| R2.2 | Chip's dismiss button sets the corresponding filter `$state` variable back to `''` |
| R3.1 | Result list markup changes from `<ul><li>` to a CSS grid of card elements; existing fields (`photoUrls[0]` / placeholder fallback, `price`, `make`/`model`/`year`, `description`) get card layout, `mileage` is added to the card (already present on `CarSearchResponse.CarSearchResult`, not currently rendered) |
| R4.1 | Existing `loading` branch gets `role="status"` + a styled indicator instead of a plain `<p>Searching…</p>` |
| R4.2 | Existing zero-results branch gets a styled empty-state block instead of a plain `<p>No results found.</p>` |
| R4.3 | Existing `role="alert"` error branch gets styled error-banner treatment (role already correct, only presentation changes) |
| R5.1 | A `@media (max-width: 720px)` rule collapses the card grid to `grid-template-columns: 1fr` |

## Component structure

Stays a single route (`+page.svelte`) — the app is one page and splitting into sub-components buys nothing here and would just add prop-plumbing for `$state` that Svelte 5 runes handle fine inline. Structural changes, in order of the file:

1. **Header**: `<h1>` + search `<input>` + a "Filters" toggle `<button>` that shows/hides a drawer containing the four existing filter inputs (unchanged `id`/`for`/label text, so existing `getByLabelText` test queries keep working).
2. **Chips row**: rendered when any filter `$state` is non-empty, one chip per set filter, each with a dismiss button.
3. **Results**: loading / error / empty / card-grid branches, same `{#if}` structure as today, restyled.

## Data model

No changes. `mileage` already exists on `CarSearchResponse.CarSearchResult` (backend) — the frontend's `CarResult` type in `+page.svelte` needs one field added (`mileage: number`) since it's currently omitted from the type and never rendered.

## Styling approach

CSS custom properties on `:root` for light mode, redefined under `@media (prefers-color-scheme: dark)` for dark mode — matches how the rest of this project's design artifacts (BOH-19's redesign proposal) are structured, so the shipped page matches what was reviewed. No CSS framework — the app is one page; a framework would be more removal-later cost than it's worth at this size.

## Testing strategy

- Existing `page.svelte.test.ts` suite (11 tests) must keep passing unmodified in behavior — it queries by role/label text, not CSS classes or DOM structure, so it acts as a regression guard that the redesign doesn't silently change form semantics or the request payload.
- New tests, one file per concern added to `page.svelte.test.ts`:
  - Chip rendering: set a filter, assert a chip with its value is present; dismiss it, assert the filter input's value is cleared and a re-submit omits it from the request body (reuses the existing "omits a filter... cleared" pattern).
  - Card content: assert `mileage` renders in the result card (currently untested and unrendered).
  - Loading state: assert `role="status"` element is present while a fetch is pending (mock a fetch that doesn't resolve immediately).
  - Empty state: assert a distinct empty-state element (e.g. `data-testid="empty-state"` or a stable text match) renders when `results` is `[]`.
- Responsive breakpoint (R5.1) is a pure CSS media-query rule — not meaningfully unit-testable under jsdom (no real layout engine); verified manually via the browser preview at a narrow viewport width instead, same as noted for CSS-only concerns in other `design.md` files in this repo.

## Open decisions

None — this redesign works entirely within the existing API contract; there's no ambiguous behavior to flag.
