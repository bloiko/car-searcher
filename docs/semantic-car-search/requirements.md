# Semantic car search — requirements

## Problem

Keyword search fails on queries like "reliable family SUV under 30k" or "fuel efficient commuter car" — none of those words may appear in a listing that's actually a great match. The search needs to understand intent, not just match tokens.

## Acceptance criteria

- A free-text query returns cars ranked by relevance to the *meaning* of the query, not just literal keyword overlap.
- Structured filters (make, model, year range, price range, mileage range) can be combined with a free-text query in the same request.
- A query with no results returns an empty list with a 200, not an error.
- Search responds in well under a second for a catalog in the low thousands of listings (local dev scale — not a performance target worth over-engineering yet).
- Each result includes enough of the source listing (make, model, year, price, a short description) to render a result card without a second request.

## Explicitly out of scope for now

- Personalization / search history
- Autocomplete or query suggestions
- Any ranking signal beyond text relevance (e.g. popularity, recency) — revisit once there's real usage data
