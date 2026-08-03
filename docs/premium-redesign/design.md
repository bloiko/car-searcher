# Premium redesign — design

## Requirements traceability

| Requirement | Design element |
|---|---|
| R1.1 | New `:root` custom properties in `+page.svelte`'s `<style>` block, replacing every existing color token 1:1 with the artifact's exact values (light + dark) |
| R1.2 | New `--text-hero` token (`clamp(2.6rem, 6vw, 4.6rem)`); existing `--text-2xl`/etc. kept for anything not part of the hero |
| R1.3 | New `--font-mono` token; a `.tabular` utility class (`font-family: var(--font-mono); font-variant-numeric: tabular-nums;`) applied to price/mileage elements |
| R2.1 | `+page.svelte`'s header markup restructured: hero wrapper (headline, subhead, search-as-centerpiece) replaces the current bare `<h1>` + form opening |
| R2.2 | All filter/sort/pagination markup (ids, labels, `$state`, submit logic) moved into the new hero structure unchanged — a layout/style change, not a rebuild |
| R3.1 | `.result-card:hover { transform: translateY(-6px); box-shadow: var(--shadow-card-hover); }` gated by `@media (prefers-reduced-motion: no-preference)` |
| R3.2 | `.price`/`.mileage` elements gain the `.tabular` class from R1.3 |
| R4.1 | Same `:root` token block + `.tabular` utility reused in `frontend/src/routes/cars/[id]/+page.svelte` (already duplicates the token block per an earlier flagged, unresolved follow-up — this task doesn't fix the duplication, just keeps both copies' values in sync) |

## Color tokens (exact values from the chosen artifact)

```css
/* light */
--color-bg: #FAF8F4;        --color-surface: #FFFFFF;   --color-surface-sunken: #F2EFE7;
--color-text: #14110B;      --color-text-muted: #6B665C; --color-border: #E6E1D6;
--color-accent: #9C6F1F;    --color-accent-strong: #7C5714; --color-accent-soft: #F3E6CC; --color-accent-ink: #FFFFFF;

/* dark */
--color-bg: #0A0A0B;        --color-surface: #16161A;   --color-surface-sunken: #1E1E23;
--color-text: #F5F3EE;      --color-text-muted: #9C978C; --color-border: #2A2A2E;
--color-accent: #D4A857;    --color-accent-strong: #E8BE6E; --color-accent-soft: #2A2213; --color-accent-ink: #1A1305;
```

## Typography

One system sans family (`-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif`) carries both display and body — display distinguished by size/weight/tracking (`--text-hero`, weight 800, `letter-spacing: -0.035em`), not a different font. Numeric figures (price, mileage) use `--font-mono: ui-monospace, "SF Mono", "Cascadia Code", "Roboto Mono", monospace` with `font-variant-numeric: tabular-nums`, applied via a `.tabular` utility class — reused verbatim on both routes.

## Resolved deviation from the artifact: no fabricated stats row

The artifact's hero includes a "1,000+ listings indexed / 384-d / <80ms" stats row — decorative, and dishonest to ship as-is: there's no real inventory yet (BOH-18 still blocked) and no latency measurement exists. **This implementation omits the stats row entirely** rather than ship placeholder numbers presented as real. If real aggregate stats become cheap to compute later (e.g. total indexed count via a lightweight endpoint), a future task can add a real version — not scoped here. The hero's visual weight without the stats row still holds up: headline + subhead + search shell is the actual "confidence" lever, the stats row was a secondary flourish.

## Layout

Hero structure in `+page.svelte`, replacing the current `<h1>` + immediate form:
```
<header class="top">...</header>          -- unchanged structurally, restyled
<section class="hero">
  <eyebrow>                                -- small label, e.g. "Semantic search"
  <h1>headline + accent-colored second line</h1>
  <p class="sub">subheading</p>
  <search-shell>                           -- existing search input + submit, restyled as the centerpiece
  <examples row>                           -- existing sort/filter controls relocate here, restyled
</section>
<section class="results">...</section>     -- existing card grid, restyled per R3.1/R3.2
```
The classic filters (make/model/year/mileage/transmission from BOH-22) and pagination controls (BOH-17) keep their exact current DOM structure/ids/labels — only their container's visual position and styling change, not their markup shape, per R2.2.

## Motion

Card hover: `transform: translateY(-6px)` + shadow deepen, `transition: transform 0.22s cubic-bezier(.2,.8,.2,1), box-shadow 0.22s ease`. Wrapped in `@media (prefers-reduced-motion: no-preference)` so a reduced-motion user gets the hover shadow-only, no transform/transition.

## Testing strategy

- No behavioral test should need to change — R2.2 is explicit that every existing `getByLabelText`/`getByRole` query and request-body assertion keeps working, since this is a pure restyle. If any existing test breaks, that's a signal the restructure went further than intended, not an expected/sanctioned change (unlike the search-ui-redesign feature, which genuinely restructured DOM in ways that legitimately broke old tests).
- New test: assert a `.tabular`-classed element exists for price/mileage (a real assertion that the numeric treatment is actually applied, not just present in CSS unused).
- New test: assert the hero headline/subheading text renders (basic presence check — the actual visual/motion polish isn't meaningfully unit-testable, same caveat as prior styling tasks in this codebase).
- Manual verification in the browser (screenshot or structural inspection) required before considering this done, matching this project's established practice for visual work.

## Open decisions

None — the visual direction is already chosen; the only design decision made fresh in this document is the stats-row omission, resolved above.
