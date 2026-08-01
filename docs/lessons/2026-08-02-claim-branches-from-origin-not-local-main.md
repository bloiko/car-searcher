# `workflow.mjs claim` silently drops locally-committed-but-unpushed commits

**What happened:** Committed `docs/search-ui-redesign/{requirements,design,tasks}.md` directly to local `main` (per this repo's SDD convention), then immediately ran `workflow.mjs claim --feature search-ui-redesign --task 1` without pushing first. `claim` reported success and printed the correct task title (proving it read `tasks.md` correctly), so nothing looked wrong. Only when the first `implementer` subagent reported "`docs/search-ui-redesign/` doesn't exist anywhere in the repo" did it surface: the checked-out `feature/search-ui-redesign` branch was missing the SDD-docs commit entirely.

**Root cause, confirmed via `git reflog`/`git log`:** `ai-workflow/scripts/lib/git.mjs`'s `createBranchFrom` does:
```js
run(repoPath, ["fetch", "origin", fromBranch]);
run(repoPath, ["switch", "-c", branchName, `origin/${fromBranch}`]);
```
It branches from `origin/main`, not local `main`. My SDD-docs commit existed on local `main` but hadn't been pushed yet, so at the moment `createBranchFrom` ran, `origin/main` was still one commit behind — the feature branch was created without it. Confusingly, `claim` *also* commits its own "Tracker: task N claimed" bookkeeping commit onto local `main` and pushes that afterward, and that push carries the full commit range (including my earlier unpushed commit) — so `origin/main` ends up correct and `git log origin/main` shows everything, masking that the *feature branch itself* diverged from a stale point.

**Fix applied (not a `git.mjs` code change, a workflow discipline fix):** confirmed the feature branch was a pure fast-forward behind local `main` (it had zero commits of its own yet — no task had been committed), so `git merge main --ff-only` on the feature branch pulled in the missing commits cleanly with the in-progress uncommitted task-1 work preserved on top. This only works because it was caught before any task commit landed on the feature branch; catching it later would need a rebase instead.

**Why this matters beyond this one incident:** `createBranchFrom` will silently do this again any time SDD docs (or anything else) get committed to local `main` without an explicit push immediately before `claim`. The practical rule: **always `git push` right after committing anything to `main` by hand, never leave a local-only commit on `main` before calling `claim`** — `claim`'s own bookkeeping commits are safe because they push immediately, but manual commits (like the SDD-docs-authoring step, which isn't part of `workflow.mjs` at all) have no such guarantee. A more robust fix would be branching from local `main` (or pushing local `main` first) inside `createBranchFrom` itself rather than trusting `origin/main` is current — flagged here rather than silently patched, since that's a sibling-repo (`ai-workflow`) change outside this session's scope.

**Tag:** `workflow-mjs`, `git`, `claim`, `stale-branch`, `sdd-docs`
