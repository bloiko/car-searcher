# Search pagination — requirements

BOH-17, second half (sort already shipped — `docs/search-sort/`). "Results currently return everything, ranked one way... unbounded result sets don't scale past a toy dataset."

## EARS pattern legend

| Pattern | Form | Use for |
|---|---|---|
| Ubiquitous | `THE SYSTEM SHALL <response>` | Always-true invariants |
| Event-driven | `WHEN <trigger> THE SYSTEM SHALL <response>` | Normal-flow behavior |
| State-driven | `WHILE <state> THE SYSTEM SHALL <response>` | Behavior only valid during a state |
| Unwanted behavior | `IF <condition> THEN THE SYSTEM SHALL <response>` | Error handling, edge cases |
| Optional feature | `WHERE <feature present> THE SYSTEM SHALL <response>` | Conditional/configurable behavior |

## User Story 1 — Results come back in bounded pages

As a car shopper, I want search results returned in manageable pages instead of everything at once, so that the app stays fast and usable as inventory grows.

**Acceptance criteria:**
- R1.1 — WHEN a search request includes `page` and/or `pageSize` THE SYSTEM SHALL return at most `pageSize` results, starting at `page * pageSize`.
- R1.2 — WHILE `page`/`pageSize` are absent THE SYSTEM SHALL default to page 0, page size 20 (unaffected behavior for a caller that doesn't opt in, other than the implicit cap — see R1.3).
- R1.3 — IF `pageSize` is present and outside `1..100` THEN THE SYSTEM SHALL reject the request with a validation error (same style as `Filters`' existing negative-value rejection) — bounds the "everything at once" problem even for a caller that tries to opt out.
- R1.4 — IF `page` is present and negative THEN THE SYSTEM SHALL reject the request with a validation error.

## User Story 2 — The caller knows how many results exist in total

As the frontend, I want the total match count alongside a page of results, so that I can show "page 2 of 5" / disable "next" at the end, without a separate request.

**Acceptance criteria:**
- R2.1 — WHEN search returns results THE SYSTEM SHALL include the total number of matching listings (not just the current page's count) in the response.

## User Story 3 — Frontend can move between pages

As a car shopper, I want next/previous controls, so that I can browse beyond the first page.

**Acceptance criteria:**
- R3.1 — WHEN a user advances to another page and submits (or the UI auto-refetches) THE SYSTEM SHALL request that page and render its results.
- R3.2 — WHERE there is no next/previous page (already at the last/first page) THE SYSTEM SHALL disable the corresponding control rather than let the user request an out-of-range page.

## Explicitly out of scope

- Infinite scroll / "load more" (appending to the existing list) — this ships classic page-number navigation (replace the list each page), matching the simplest reading of "pagination" the ticket asks for. Revisit if real usage shows infinite scroll is wanted.
- Changing `pageSize` from the UI — fixed at 20 for this round; a page-size selector is a separate, smaller follow-up if wanted later.
- Cursor/search-after pagination for deep result sets — `from`/`size` is fine at this app's real scale (hundreds of listings, per BOH-24); revisit only if that assumption stops holding.
