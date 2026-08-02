# car-searcher

A car search app with semantic search, backed by OpenSearch.

- **Backend:** Java 21 / Spring Boot, `backend/`
- **Frontend:** SvelteKit, `frontend/`
- **Search:** OpenSearch (local via Docker Compose for now)

Status: early scaffold, local development only.

## Quick start

```bash
docker compose -f backend/docker-compose.yml up -d
cd backend && ./mvnw spring-boot:run
cd frontend && npm install && npm run dev
```

Backend: http://localhost:8080 · Frontend: http://localhost:5173

See [CLAUDE.md](CLAUDE.md) for repo conventions, and `docs/` for feature specs.

### One-time: semantic search embedding model

Real (non-placeholder) semantic search needs an embedding model registered and deployed in OpenSearch — a one-time step per cluster, not part of every backend restart (see `docs/semantic-car-search/design.md`, "Embedding model & vector architecture"):

```bash
./backend/scripts/register-embedding-model.sh
```

Set the printed model ID as an environment variable before starting the backend (don't hardcode it into `application.yml` — it's checked into git and the model ID is unique per OpenSearch cluster, so a committed value would be wrong for anyone else's environment):

```bash
export CAR_SEARCHER_EMBEDDING_MODEL_ID=<the-printed-model-id>
cd backend && ./mvnw spring-boot:run
```

Startup fails with a clear error if this is unset.

## This repo has an AI dev workflow — how to use it

Tasks live in [Linear](https://linear.app/bohdanloiko) (one issue per feature) and in `docs/<feature>/tasks.md` (the actual tracker, with a `Status` column per task). Driving a task through implementation is either:

**Ask an AI coding assistant directly** — open a session in this repo and ask it to work a task, e.g. *"implement task 3 of docs/semantic-car-search/tasks.md"* or *"continue the semantic-car-search tracker."* If you're using Claude Code and the `implement` skill is installed (see the sibling [`ai-workflow`](../ai-workflow) repo's README for what that means), it already knows the protocol: an isolated write-the-test → make-it-pass → independent-review cycle per task, committed on a shared feature branch, one PR per batch of tasks — not per task. If the skill isn't installed, the same protocol is written out in full at `../ai-workflow/skills/implement/SKILL.md` — point the assistant at that file.

**Or run the mechanics yourself** — `../ai-workflow/scripts/workflow.mjs` (zero-dependency Node CLI, needs Node 18+) handles the git/GitHub/Linear bookkeeping:
```bash
# needs backend's/frontend's sibling ../ai-workflow checked out, and this repo's
# .env.local populated with GITHUB_TOKEN + LINEAR_API_KEY (gitignored, ask for these)
node ../ai-workflow/scripts/workflow.mjs status     --repo . --feature semantic-car-search
node ../ai-workflow/scripts/workflow.mjs claim      --repo . --feature semantic-car-search
node ../ai-workflow/scripts/workflow.mjs open-pr    --repo . --feature semantic-car-search --title "..."
node ../ai-workflow/scripts/workflow.mjs check-merged --repo . --feature semantic-car-search
```
This only handles the mechanical parts (branch, tracker, PR, Linear state) — the actual test-writing/implementing/reviewing is still a human or an AI assistant's job, not something this script does for you.
