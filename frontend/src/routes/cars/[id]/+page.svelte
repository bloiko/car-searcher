<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/state';

	type Car = {
		id: string;
		make: string;
		model: string;
		year: number;
		price: number;
		mileage: number;
		transmission: string;
		description: string;
		photoUrls: string[];
		sourceUrl?: string | null;
	};

	let car = $state<Car | null>(null);
	let loading = $state(true);
	let notFound = $state(false);
	let error = $state('');

	async function loadCar() {
		loading = true;
		error = '';
		notFound = false;
		car = null;
		try {
			const response = await fetch(`http://localhost:8080/api/cars/${page.params.id}`);

			if (response.status === 404) {
				notFound = true;
				return;
			}
			if (!response.ok) {
				throw new Error(`Failed to load listing with status ${response.status}`);
			}

			car = await response.json();
		} catch (err) {
			error = err instanceof Error ? err.message : 'Failed to load listing';
		} finally {
			loading = false;
		}
	}

	// Client-side fetch on mount -- this route has no `+page.ts`/`load`, matching
	// the rest of this app (the search page fetches from inside the component
	// too, not via SvelteKit's load functions).
	onMount(() => {
		loadCar();
	});
</script>

{#if loading}
	<p role="status" class="status-message">Loading…</p>
{/if}

{#if error}
	<p role="alert" class="error-banner">{error}</p>
{/if}

{#if notFound}
	<p class="empty-state">Listing not found.</p>
{/if}

{#if car}
	<div class="detail">
		<div class="gallery">
			{#if car.photoUrls.length > 0}
				{#each car.photoUrls as photoUrl (photoUrl)}
					<img src={photoUrl} alt="{car.make} {car.model}" class="gallery-photo" />
				{/each}
			{:else}
				<img
					src="/placeholder-car.svg"
					alt="{car.make} {car.model} placeholder"
					class="gallery-photo"
				/>
			{/if}
		</div>

		<div class="detail-info">
			<!-- Same visual weight as the search-result card's price (R2.1). -->
			<p class="price">${car.price}</p>
			<h1>{car.make} {car.model} ({car.year})</h1>
			<p class="mileage">{car.mileage}</p>
			<p class="transmission">{car.transmission}</p>
			<p class="description">{car.description}</p>

			{#if car.sourceUrl}
				<!-- R3.1: the "act on a listing" requirement -- styled as the primary
				     CTA, not a footnote link. R3.2: omitted entirely (this whole block)
				     when sourceUrl is null/blank. -->
				<a href={car.sourceUrl} target="_blank" rel="noopener noreferrer" class="source-link">
					View original listing
				</a>
			{/if}
		</div>
	</div>
{/if}

<style>
	/* Duplicated from `../../+page.svelte`'s token block rather than imported from
	   a shared location: this project compiles each route's <style> block with
	   `css: 'injected'` under test (and a separate external chunk per route in
	   real build/dev), so a sibling route's <style> tag is never present in the
	   document when this route is the one mounted -- there's no shared layout
	   stylesheet yet for these tokens to live in without either (a) breaking the
	   root page's own existing style-system test (which renders +page.svelte
	   standalone, bypassing the layout) or (b) introducing a new global CSS file
	   plus vitest CSS-processing config this task's scope doesn't call for.
	   Values are copied verbatim -- nothing here is reinvented. */
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
		font-weight: 400;
		color: var(--color-text);
		margin: 0 0 var(--space-2);
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

	.detail {
		display: flex;
		flex-direction: column;
		gap: var(--space-6);
		max-width: 60rem;
		margin: var(--space-6) var(--space-4);
	}

	.gallery {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(16rem, 1fr));
		gap: var(--space-4);
	}

	.gallery-photo {
		width: 100%;
		aspect-ratio: 16 / 9;
		object-fit: cover;
		border-radius: var(--radius);
		background-color: var(--color-surface);
		border: 1px solid var(--color-border);
	}

	.detail-info {
		display: flex;
		flex-direction: column;
		gap: var(--space-2);
		background-color: var(--color-surface);
		border: 1px solid var(--color-border);
		border-radius: var(--radius);
		padding: var(--space-4);
	}

	/* Same visual weight as `+page.svelte`'s `.result-card .price` (R2.1). */
	.detail-info .price {
		font-size: var(--text-2xl);
		font-weight: 700;
		color: var(--color-text);
		margin: 0;
	}

	.detail-info .mileage,
	.detail-info .transmission {
		font-size: var(--text-sm);
		color: var(--color-text-muted);
		margin: 0;
	}

	.detail-info .description {
		font-size: var(--text-base);
		color: var(--color-text);
		margin: var(--space-2) 0 0;
	}

	/* The primary call-to-action for this page -- styled like the search page's
	   submit button, not a footnote link. */
	.source-link {
		align-self: flex-start;
		font-family: var(--font-body);
		font-size: var(--text-base);
		font-weight: 600;
		text-decoration: none;
		color: var(--color-primary-text);
		background-color: var(--color-primary);
		border-radius: var(--radius);
		margin-top: var(--space-2);
		padding-top: var(--space-2);
		padding-bottom: var(--space-2);
		padding-left: var(--space-4);
		padding-right: var(--space-4);
	}

	@media (max-width: 720px) {
		.gallery {
			grid-template-columns: 1fr;
		}
	}
</style>
