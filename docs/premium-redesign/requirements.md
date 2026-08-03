# Premium redesign — requirements

BOH-29. Current UI (verified via computed styles on the running app): dark bg `#1c1917`, stock amber accent `#f59e0b`, Georgia serif heading capped at `2.25rem`, spacing scale topping out at `1.5rem`, no hero moment — reads as a functional MVP, not a confident product.

Redesign direction already evaluated, chosen, and mocked: [artifact](https://claude.ai/code/artifact/258625ca-9ebb-4d9c-9602-72f73926f03f). This document scopes implementing that exact chosen direction into the real app — it is not a fresh design decision, the decision is already made.

## EARS pattern legend

| Pattern | Form | Use for |
|---|---|---|
| Ubiquitous | `THE SYSTEM SHALL <response>` | Always-true invariants |
| Event-driven | `WHEN <trigger> THE SYSTEM SHALL <response>` | Normal-flow behavior |
| State-driven | `WHILE <state> THE SYSTEM SHALL <response>` | Behavior only valid during a state |
| Unwanted behavior | `IF <condition> THEN THE SYSTEM SHALL <response>` | Error handling, edge cases |
| Optional feature | `WHERE <feature present> THE SYSTEM SHALL <response>` | Conditional/configurable behavior |

## User Story 1 — The design tokens read as a considered product, not a default

**Acceptance criteria:**
- R1.1 — THE SYSTEM SHALL replace the existing color-token palette with the chosen warm near-black/ivory + muted brass-gold-accent palette (light and dark variants), matching the artifact's exact hex values.
- R1.2 — THE SYSTEM SHALL raise the type scale's ceiling to support a hero display size (`clamp(2.6rem, 6vw, 4.6rem)`, weight 800, tight tracking), while keeping body text at its current comfortable size.
- R1.3 — THE SYSTEM SHALL render price and mileage figures in a monospace font family with `tabular-nums`, distinct from body/display text.

## User Story 2 — The search page opens with a hero moment, not a bare form

**Acceptance criteria:**
- R2.1 — THE SYSTEM SHALL replace the current `<h1>Car Search</h1>` + immediate form layout with a hero section: headline, subheading, and the search input as the visual centerpiece — matching the artifact's structure.
- R2.2 — THE SYSTEM SHALL preserve every existing form control's `id`/`for`/label text and behavior exactly (classic filters, sort, pagination) — this is a visual restyle, not a functional change; every existing test must keep passing without its assertions changing.

## User Story 3 — Result cards feel alive, not static

**Acceptance criteria:**
- R3.1 — WHEN a user hovers a result card THE SYSTEM SHALL lift it (translateY) and deepen its shadow, respecting `prefers-reduced-motion` (no transform/transition when reduced motion is requested).
- R3.2 — THE SYSTEM SHALL render each card's price and mileage using the new monospace numeric treatment from R1.3.

## User Story 4 — The detail page matches, not clashes

**Acceptance criteria:**
- R4.1 — THE SYSTEM SHALL apply the same token palette, type scale, and numeric treatment to the `/cars/[id]` detail page, so navigating from a result card doesn't feel like a different product.

## Explicitly out of scope

- Any change to search/filter/sort/pagination *behavior* — this is a visual restyle only, every existing acceptance criterion from prior features (BOH-17, BOH-22, BOH-27) stays exactly as implemented.
- The stats row's actual numbers (listings indexed, vector dimension, search latency) shown in the mockup are illustrative — real numbers require real inventory (BOH-18, still blocked) and real latency measurement; this ticket does not fabricate fake statistics in the shipped UI. If a stats row ships, it must show real, currently-computable values (e.g. `total` from the search response) or be omitted rather than show placeholder numbers as if real.
- Custom/downloaded webfonts — the chosen direction deliberately uses system font stacks (one sans family for display+body, system monospace for figures), no external font files.
