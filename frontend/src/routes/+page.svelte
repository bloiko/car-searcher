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
	let results = $state<CarResult[] | null>(null);
	let loading = $state(false);
	let error = $state('');

	async function handleSubmit(event: SubmitEvent) {
		event.preventDefault();
		loading = true;
		error = '';
		try {
			const response = await fetch('http://localhost:8080/api/cars/search', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ query })
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
	<label for="search-input">Search</label>
	<input id="search-input" type="text" bind:value={query} />
	<button type="submit">Search</button>
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
