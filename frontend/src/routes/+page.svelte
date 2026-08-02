<script lang="ts">
	type CarResult = {
		id: string;
		make: string;
		model: string;
		year: number;
		price: number;
		mileage: number;
		description: string;
		photoUrls: string[];
	};

	// Fixed page size for this round (BOH-17): no page-size selector in the UI,
	// the backend defaults to 20 and doesn't echo `pageSize` back, so the
	// frontend keeps its own copy in lockstep with that default.
	const PAGE_SIZE = 20;

	let query = $state('');
	let priceMax = $state('');
	let yearMin = $state('');
	let mileageMax = $state('');
	let make = $state('');
	let model = $state('');
	let transmission = $state('');
	let sort = $state('');
	let page = $state(0);
	let total = $state(0);
	let results = $state<CarResult[] | null>(null);
	let loading = $state(false);
	let error = $state('');

	type FilterChip = {
		key: string;
		label: string;
		value: string;
		clear: () => void;
	};

	let filterChips = $derived<FilterChip[]>(
		(
			[
				{ key: 'priceMax', label: 'Max price', value: priceMax, clear: () => (priceMax = '') },
				{ key: 'yearMin', label: 'Min year', value: yearMin, clear: () => (yearMin = '') },
				{
					key: 'mileageMax',
					label: 'Max mileage',
					value: mileageMax,
					clear: () => (mileageMax = '')
				},
				{ key: 'make', label: 'Make', value: make, clear: () => (make = '') },
				{ key: 'model', label: 'Model', value: model, clear: () => (model = '') },
				{
					key: 'transmission',
					label: 'Transmission',
					value: transmission,
					clear: () => (transmission = '')
				}
			] as FilterChip[]
		).filter((chip) => chip.value !== '')
	);

	// Shared by "form submitted" and "pagination button clicked" — a page change
	// isn't a form SubmitEvent, so it can't reuse handleSubmit directly, but both
	// need the identical fetch-and-render logic.
	async function performSearch() {
		loading = true;
		error = '';
		try {
			const filters: Record<string, number | string> = {};
			if (priceMax !== '') filters.priceMax = Number(priceMax);
			if (yearMin !== '') filters.yearMin = Number(yearMin);
			if (mileageMax !== '') filters.mileageMax = Number(mileageMax);
			if (make !== '') filters.make = make;
			if (model !== '') filters.model = model;
			if (transmission !== '') filters.transmission = transmission;

			// Unlike the optional filters/sort above, `page` is always sent: `0`
			// is a meaningful, deliberate default (not "unset"), and the backend
			// already treats an absent page the same as page 0.
			const requestBody: Record<string, unknown> = { query, page };
			if (Object.keys(filters).length > 0) {
				requestBody.filters = filters;
			}
			if (sort !== '') requestBody.sort = sort;

			const response = await fetch('http://localhost:8080/api/cars/search', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(requestBody)
			});

			if (!response.ok) {
				throw new Error(`Search failed with status ${response.status}`);
			}

			const data = await response.json();
			results = data.results ?? [];
			total = data.total ?? 0;
		} catch (err) {
			error = err instanceof Error ? err.message : 'Search failed';
			results = [];
			total = 0;
		} finally {
			loading = false;
		}
	}

	async function handleSubmit(event: SubmitEvent) {
		event.preventDefault();
		// A submitted form means query/filters/sort potentially changed — a
		// fresh search should start at page 1, not resume stale pagination.
		page = 0;
		await performSearch();
	}

	async function goToPreviousPage() {
		if (page === 0) return;
		page -= 1;
		await performSearch();
	}

	async function goToNextPage() {
		if ((page + 1) * PAGE_SIZE >= total) return;
		page += 1;
		await performSearch();
	}
</script>

<h1>Car Search</h1>

<form onsubmit={handleSubmit}>
	<label for="search-input" class="search-label">Additional details</label>
	<div class="search-row">
		<input
			id="search-input"
			class="search-input"
			type="text"
			placeholder="e.g. Sportline, Laurin & Klement, leather seats"
			bind:value={query}
		/>
		<button type="submit" class="search-submit">Search</button>
	</div>

	<label for="sort-select" class="sort-label">Sort</label>
	<select id="sort-select" class="sort-select" bind:value={sort}>
		<option value="">Best match</option>
		<option value="price_asc">Price: low to high</option>
		<option value="mileage_asc">Mileage: low to high</option>
	</select>

	{#if filterChips.length > 0}
		<div class="filter-chips">
			{#each filterChips as chip (chip.key)}
				<span class="filter-chip">
					<span class="filter-chip-text">{chip.label}: {chip.value}</span>
					<button
						type="button"
						class="filter-chip-remove"
						aria-label={`Remove ${chip.label} filter`}
						onclick={chip.clear}
					>
						×
					</button>
				</span>
			{/each}
		</div>
	{/if}

	<div class="filters-section">
		<label for="price-max-input">Max price</label>
		<input
			id="price-max-input"
			type="number"
			value={priceMax}
			oninput={(e) => (priceMax = e.currentTarget.value)}
		/>

		<label for="year-min-input">Min year</label>
		<input
			id="year-min-input"
			type="number"
			value={yearMin}
			oninput={(e) => (yearMin = e.currentTarget.value)}
		/>

		<label for="mileage-max-input">Max mileage</label>
		<input
			id="mileage-max-input"
			type="number"
			value={mileageMax}
			oninput={(e) => (mileageMax = e.currentTarget.value)}
		/>

		<label for="make-input">Make</label>
		<input id="make-input" type="text" bind:value={make} />

		<label for="model-input">Model</label>
		<input id="model-input" type="text" bind:value={model} />

		<label for="transmission-input">Transmission</label>
		<select id="transmission-input" bind:value={transmission}>
			<option value="">Any</option>
			<option value="Automatic">Automatic</option>
			<option value="Manual">Manual</option>
			<option value="CVT">CVT</option>
		</select>
	</div>
</form>

{#if loading}
	<p role="status" class="status-message">Searching…</p>
{/if}

{#if error}
	<p role="alert" class="error-banner">{error}</p>
{/if}

{#if results !== null && !loading}
	{#if results.length === 0}
		<p class="empty-state">No results found.</p>
	{:else}
		<div class="results">
			{#each results as result (result.id)}
				<div class="result-card">
					{#if result.photoUrls.length > 0}
						<img src={result.photoUrls[0]} alt="{result.make} {result.model}" class="thumbnail" />
					{:else}
						<img src="/placeholder-car.svg" alt="{result.make} {result.model} placeholder" class="thumbnail" />
					{/if}
					<p class="price">${result.price}</p>
					<h2>{result.make} {result.model} ({result.year})</h2>
					<p class="mileage">{result.mileage}</p>
					<p class="description">{result.description}</p>
				</div>
			{/each}
		</div>
	{/if}
{/if}

{#if results !== null && total > PAGE_SIZE}
	<div class="pagination">
		<button type="button" onclick={goToPreviousPage} disabled={page === 0}>Previous</button>
		<span class="pagination-info">Page {page + 1} of {Math.ceil(total / PAGE_SIZE)}</span>
		<button type="button" onclick={goToNextPage} disabled={(page + 1) * PAGE_SIZE >= total}
			>Next</button
		>
	</div>
{/if}

<style>
	:global(:root) {
		--color-bg: #f8f7f5;
		--color-surface: #ffffff;
		--color-text: #1c1917;
		--color-text-muted: #57534e;
		--color-border: #d6d3d1;
		--color-primary: #b45309;
		--color-primary-text: #ffffff;
		--color-danger: #b91c1c;
		--color-danger-bg: #fef2f2;
		--color-danger-border: #fecaca;

		--font-body: system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;
		--font-heading: Georgia, 'Times New Roman', serif;

		--text-sm: 0.875rem;
		--text-base: 1rem;
		--text-lg: 1.25rem;
		--text-2xl: 2.25rem;

		--space-1: 0.25rem;
		--space-2: 0.5rem;
		--space-3: 0.75rem;
		--space-4: 1rem;
		--space-6: 1.5rem;

		--radius: 0.375rem;
	}

	@media (prefers-color-scheme: dark) {
		:global(:root) {
			--color-bg: #1c1917;
			--color-surface: #292524;
			--color-text: #f5f5f4;
			--color-text-muted: #a8a29e;
			--color-border: #44403c;
			--color-primary: #f59e0b;
			--color-primary-text: #1c1917;
			--color-danger: #f87171;
			--color-danger-bg: #451a1a;
			--color-danger-border: #7f1d1d;
		}
	}

	:global(body) {
		margin: 0;
		background-color: var(--color-bg);
		color: var(--color-text);
		font-family: var(--font-body);
		font-size: var(--text-base);
		line-height: 1.5;
	}

	h1 {
		font-family: var(--font-heading);
		font-size: var(--text-2xl);
		color: var(--color-text);
		margin: var(--space-6) var(--space-4);
	}

	form {
		display: flex;
		flex-direction: column;
		gap: var(--space-4);
		max-width: 40rem;
		margin: 0 var(--space-4) var(--space-6);
		padding: var(--space-4);
		background-color: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: var(--radius);
	}

	label {
		font-family: var(--font-body);
		font-size: var(--text-sm);
		font-weight: 600;
		color: var(--color-text-muted);
	}

	input {
		font-family: var(--font-body);
		font-size: var(--text-base);
		color: var(--color-text);
		background-color: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: var(--radius);
		/* Longhand padding (rather than the shorthand) so each side keeps
		   resolving its own custom property. */
		padding-top: var(--space-2);
		padding-bottom: var(--space-2);
		padding-left: var(--space-3);
		padding-right: var(--space-3);
	}

	button {
		align-self: flex-start;
		font-family: var(--font-body);
		font-size: var(--text-base);
		font-weight: 600;
		color: var(--color-primary-text);
		background-color: var(--color-primary);
		border: none;
		border-radius: var(--radius);
		padding-top: var(--space-2);
		padding-bottom: var(--space-2);
		padding-left: var(--space-4);
		padding-right: var(--space-4);
		cursor: pointer;
	}

	/* The classic filters (make/model/year/mileage/transmission) are always
	   visible in .filters-section below, not tucked behind a toggle — the
	   search input here is for residual/descriptive text only, not the
	   single primary control. */
	.search-row {
		display: flex;
		align-items: flex-end;
		gap: var(--space-3);
	}

	.search-input {
		flex: 1;
		font-size: var(--text-lg);
		padding-top: var(--space-3);
		padding-bottom: var(--space-3);
		padding-left: var(--space-4);
		padding-right: var(--space-4);
	}

	.search-submit {
		align-self: stretch;
		font-size: var(--text-lg);
	}

	.sort-label {
		align-self: flex-start;
	}

	.sort-select {
		align-self: flex-start;
		font-family: var(--font-body);
		font-size: var(--text-base);
		color: var(--color-text);
		background-color: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: var(--radius);
		padding-top: var(--space-2);
		padding-bottom: var(--space-2);
		padding-left: var(--space-3);
		padding-right: var(--space-3);
	}

	.filters-section {
		display: flex;
		flex-direction: column;
		gap: var(--space-4);
		padding-top: var(--space-4);
		border-top: 1px solid var(--color-border);
	}

	.filter-chips {
		display: flex;
		flex-wrap: wrap;
		gap: var(--space-2);
	}

	.filter-chip {
		display: inline-flex;
		align-items: center;
		gap: var(--space-1);
		font-family: var(--font-body);
		font-size: var(--text-sm);
		color: var(--color-text);
		background-color: var(--color-bg);
		border: 1px solid var(--color-border);
		border-radius: 999px;
		padding-top: var(--space-1);
		padding-bottom: var(--space-1);
		padding-left: var(--space-3);
		padding-right: var(--space-2);
	}

	.filter-chip-remove {
		align-self: auto;
		font-family: var(--font-body);
		font-size: var(--text-sm);
		font-weight: 600;
		line-height: 1;
		color: var(--color-text-muted);
		background: none;
		border: none;
		border-radius: 50%;
		padding: 0;
		width: 1.25rem;
		height: 1.25rem;
		cursor: pointer;
	}

	.status-message {
		display: flex;
		align-items: center;
		gap: var(--space-2);
		font-family: var(--font-body);
		font-size: var(--text-base);
		font-weight: 600;
		color: var(--color-text-muted);
		background-color: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: var(--radius);
		max-width: 40rem;
		margin: 0 var(--space-4) var(--space-6);
		padding: var(--space-4);
	}

	.status-message::before {
		content: '';
		width: 1rem;
		height: 1rem;
		border: 2px solid var(--color-border);
		border-top-color: var(--color-primary);
		border-radius: 50%;
		animation: status-spin 0.8s linear infinite;
	}

	@keyframes status-spin {
		to {
			transform: rotate(360deg);
		}
	}

	.empty-state {
		font-family: var(--font-body);
		font-size: var(--text-base);
		color: var(--color-text-muted);
		text-align: center;
		max-width: 40rem;
		margin: 0 var(--space-4) var(--space-6);
		padding: var(--space-6) var(--space-4);
		background-color: var(--color-surface);
		border: 1px dashed var(--color-border);
		border-radius: var(--radius);
	}

	.error-banner {
		font-family: var(--font-body);
		font-size: var(--text-base);
		font-weight: 600;
		color: var(--color-danger);
		max-width: 40rem;
		margin: 0 var(--space-4) var(--space-6);
		padding: var(--space-4);
		background-color: var(--color-danger-bg);
		border: 1px solid var(--color-danger-border);
		border-radius: var(--radius);
	}

	.results {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(16rem, 1fr));
		gap: var(--space-4);
		margin: 0 var(--space-4) var(--space-6);
	}

	.result-card {
		display: flex;
		flex-direction: column;
		gap: var(--space-1);
		background-color: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: var(--radius);
		padding: var(--space-4);
	}

	.result-card .thumbnail {
		width: 100%;
		aspect-ratio: 16 / 9;
		object-fit: cover;
		border-radius: var(--radius);
		margin-bottom: var(--space-2);
	}

	.result-card h2 {
		font-family: var(--font-heading);
		font-size: var(--text-base);
		font-weight: 400;
		color: var(--color-text);
		margin: 0;
	}

	/* Price is the most visually prominent figure on the card: larger and
	   heavier than the make/model/year heading and the mileage figure. */
	.result-card .price {
		font-size: var(--text-2xl);
		font-weight: 700;
		color: var(--color-text);
		margin: 0;
	}

	.result-card .mileage {
		font-size: var(--text-sm);
		color: var(--color-text-muted);
		margin: 0;
	}

	.result-card .description {
		font-size: var(--text-sm);
		color: var(--color-text-muted);
		margin: 0;
	}

	.pagination {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: var(--space-4);
		max-width: 40rem;
		margin: 0 var(--space-4) var(--space-6);
	}

	.pagination button {
		align-self: auto;
	}

	.pagination button:disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}

	.pagination-info {
		font-family: var(--font-body);
		font-size: var(--text-sm);
		color: var(--color-text-muted);
	}

	@media (max-width: 720px) {
		.results {
			grid-template-columns: 1fr;
		}
	}
</style>
