# Semantic k-NN search — design

## Requirements traceability

| Requirement | Design element |
|---|---|
| R1.1 | `CarSearchService`'s `must` clause changes from `multiMatch` to a `neural` query against `description_vector`, using the same model ID config `EmbeddingPipelineBootstrap` already reads (BOH-26) |
| R1.2 | Unchanged — k-NN with no matches above threshold still returns an empty `hits` list, mapped the same way as today (`response.hits().hits().stream().map(Hit::source).toList()`) |
| R2.1 | Unchanged — `buildFilterClauses` and the `filter` context are not touched by this task |
| R2.2 | Unchanged — `CarSearchRequest`'s `@NotBlank query` constraint already covers this; verified, not modified |

## Query approach

Replace this (current, placeholder):
```java
.query(q -> q.bool(b -> b
        .must(m -> m.multiMatch(mm -> mm.query(query).fields(SEARCH_FIELDS)))
        .filter(filterClauses)))
```
with:
```java
.query(q -> q.bool(b -> b
        .must(m -> m.neural(n -> n.field("description_vector").queryText(query).modelId(modelId).k(K)))
        .filter(filterClauses)))
```
(exact builder method names to be confirmed against the real opensearch-java 2.14.0 API — **do not assume the typed client supports a `neural` query the way this sketch shows**. This project has now hit two confirmed cases where an assumed opensearch-java API shape was wrong — `documents()` silently empty, and `GetPipelineResponse` not throwing for a missing pipeline — so the same discipline applies here: `test-author` must verify the real API via `javap` against the actual dependency jar before writing to it, exactly like the pattern already used for BOH-26's ingest pipeline task. If the typed client genuinely has no `neural` query support in this version, fall back to the client's raw/generic JSON query path — but confirm that's actually necessary before reaching for it.)

`filterClauses` and `sortClauses` are untouched — this task only changes what goes in `must`.

## Configuration

`CarSearchService` needs the same `car-searcher.embedding.model-id` value `EmbeddingPipelineBootstrap` already reads via `@Value("${car-searcher.embedding.model-id:}")`. Inject it the same way (constructor `@Value` parameter) — no new shared config abstraction needed for two call sites; introduce one only if a third consumer shows up later.

Unlike `EmbeddingPipelineBootstrap`'s fail-fast-in-constructor validation (appropriate for a one-time startup check), `CarSearchService` is invoked per-request — a blank model ID here should still fail loudly (an `IllegalStateException` at construction time is fine, same as the pipeline bootstrap, since the service is a singleton bean constructed once at startup, not per-request).

## `k` (nearest-neighbors considered)

Default `k = 50`. Not tuned against real data (there's currently ~1 test listing) — explicitly flagged in requirements.md as needing revisit once BOH-24 (seed real inventory) lands. 50 is a reasonable placeholder: large enough to not artificially truncate results on a small dataset, small enough to not be wasteful.

## Incidental cleanup (small, in scope since this task touches the same file)

`CarSearchRequest.java`'s javadoc is stale — it says filters are "Accepted but not yet applied" and points at completed tasks (`search-filters`, the k-NN backlog item) as future work that's actually already done (BOH-14, BOH-22, and this ticket itself). Update the class/field javadoc to reflect current reality while implementing this task — not a separate tracked task, too small to warrant one.

## Testing strategy

- `CarSearchService`: mocked-client tests replacing the existing `multi_match`-assertion tests with `neural`-query assertions (field, query text, model ID, k) — same `ArgumentCaptor<SearchRequest>` pattern already used throughout this file. Existing filter/sort tests should need no changes (orthogonal to this task).
- No mocked test can verify real ranking quality — that requires BOH-24's real inventory and is explicitly out of scope for gate-passing here (same caveat already true of every other search task in this codebase).
- Manual verification against the real cluster is required before this is considered actually done (same as BOH-26) — but given this machine hit real memory constraints running the embedding model locally, budget for that verification to potentially need to happen in a separate, lower-memory-pressure session rather than forcing it immediately after implementation.

## Open decisions

None on the query semantics. The only real unknown is the exact opensearch-java 2.14.0 API shape for a `neural` query, which is investigation work for `test-author`, not a decision to make now.
