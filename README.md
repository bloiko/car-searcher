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
