<script lang="ts">
	type CarResult = {
		id: string;
		make: string;
		model: string;
		year: number;
		price: number;
		description: string;
		photoUrls: string[];
	};

	let query = $state('');
	let priceMax = $state('');
	let yearMin = $state('');
	let mileageMax = $state('');
	let make = $state('');
	let results = $state<CarResult[] | null>(null);
	let loading = $state(false);
	let error = $state('');
	let filtersOpen = $state(false);

	async function handleSubmit(event: SubmitEvent) {
		event.preventDefault();
		loading = true;
		error = '';
		try {
			const filters: Record<string, number | string> = {};
			if (priceMax !== '') filters.priceMax = Number(priceMax);
			if (yearMin !== '') filters.yearMin = Number(yearMin);
			if (mileageMax !== '') filters.mileageMax = Number(mileageMax);
			if (make !== '') filters.make = make;

			const requestBody: Record<string, unknown> = { query };
			if (Object.keys(filters).length > 0) {
				requestBody.filters = filters;
			}

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
		} catch (err) {
			error = err instanceof Error ? err.message : 'Search failed';
			results = [];
		} finally {
			loading = false;
		}
	}
</script>

<h1>Car Search</h1>

<form onsubmit={handleSubmit}>
	<label for="search-input" class="search-label">Search</label>
	<div class="search-row">
		<input id="search-input" class="search-input" type="text" bind:value={query} />
		<button type="submit" class="search-submit">Search</button>
	</div>

	<button
		type="button"
		class="filters-toggle"
		aria-expanded={filtersOpen}
		onclick={() => (filtersOpen = !filtersOpen)}
	>
		Filters
	</button>

	{#if filtersOpen}
		<div class="filters-drawer">
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
		</div>
	{/if}
</form>

{#if loading}
	<p>Searching…</p>
{/if}

{#if error}
	<p role="alert">{error}</p>
{/if}

{#if results !== null && !loading}
	{#if results.length === 0}
		<p>No results found.</p>
	{:else}
		<ul class="results">
			{#each results as result (result.id)}
				<li class="result">
					{#if result.photoUrls.length > 0}
						<img src={result.photoUrls[0]} alt="{result.make} {result.model}" class="thumbnail" />
					{:else}
						<img src="/placeholder-car.svg" alt="{result.make} {result.model} placeholder" class="thumbnail" />
					{/if}
					<h2>{result.make} {result.model} ({result.year})</h2>
					<p class="price">${result.price}</p>
					<p class="description">{result.description}</p>
				</li>
			{/each}
		</ul>
	{/if}
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

	/* The search input is the primary, most visually prominent control on the
	   page: it sits first, is visually larger than the filter inputs, and
	   shares a row with its submit button. The filter inputs stay visually
	   secondary, tucked behind the "Filters" toggle in a collapsible drawer. */
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

	.filters-toggle {
		align-self: flex-start;
		font-size: var(--text-sm);
		font-weight: 600;
		color: var(--color-text);
		background-color: var(--color-surface);
		border: 1px solid var(--color-border);
	}

	.filters-drawer {
		display: flex;
		flex-direction: column;
		gap: var(--space-4);
		padding-top: var(--space-4);
		border-top: 1px solid var(--color-border);
	}
</style>
