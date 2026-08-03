import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, render, screen, waitFor } from '@testing-library/svelte';
import Page from './+page.svelte';

// The real implementation reads the dynamic route param (`id`) via
// `$app/state`'s `page.params` -- this project is on SvelteKit 2.70 (well
// past 2.12, where `$app/state` was introduced) and already writes every
// other component in runes style, so this is the modern, non-deprecated
// equivalent of the older `$app/stores` `$page` store pattern. Rendering a
// component directly with @testing-library/svelte bypasses SvelteKit's
// router entirely, so this module must be mocked for the id to be available
// at all -- matching how `$app/navigation`/`$app/stores` are conventionally
// mocked in SvelteKit component tests elsewhere.
vi.mock('$app/state', () => ({
	page: { params: { id: '1' } }
}));

afterEach(() => {
	cleanup();
	vi.unstubAllGlobals();
});

describe('car detail page', () => {
	it('renders make, model, year, price, mileage, transmission, full description, and every photo from the fetched listing', async () => {
		const mockCar = {
			id: '1',
			make: 'Toyota',
			model: 'RAV4',
			year: 2020,
			price: 25000,
			mileage: 42000,
			transmission: 'Automatic',
			description: 'A reliable family SUV under 30k with leather seats and a sunroof.',
			photoUrls: [
				'https://example.com/rav4-1.jpg',
				'https://example.com/rav4-2.jpg',
				'https://example.com/rav4-3.jpg'
			]
		};
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			status: 200,
			json: async () => mockCar
		});
		vi.stubGlobal('fetch', fetchMock);

		const { container } = render(Page);

		await waitFor(() => {
			expect(container.textContent).toContain('Toyota');
		});

		// Fetches the specific listing by the route's id param, not a hardcoded id.
		expect(fetchMock.mock.calls[0]?.[0]).toBe('http://localhost:8080/api/cars/1');

		expect(container.textContent).toContain('RAV4');
		expect(container.textContent).toContain('2020');
		expect(container.textContent).toContain('25000');
		expect(container.textContent).toContain('42000');
		expect(container.textContent).toContain('Automatic');
		expect(container.textContent).toContain(mockCar.description);

		// R2.1: every photo in photoUrls, not just the first (contrast with the
		// search-result card, which only shows photoUrls[0] as a thumbnail).
		const images = container.querySelectorAll('img');
		expect(images.length).toBe(3);
		expect((images[0] as HTMLImageElement).src).toBe(mockCar.photoUrls[0]);
		expect((images[1] as HTMLImageElement).src).toBe(mockCar.photoUrls[1]);
		expect((images[2] as HTMLImageElement).src).toBe(mockCar.photoUrls[2]);
	});

	it('renders an outbound link to the source listing, opening in a new tab, when sourceUrl is present (R3.1)', async () => {
		const mockCar = {
			id: '1',
			make: 'Toyota',
			model: 'RAV4',
			year: 2020,
			price: 25000,
			mileage: 42000,
			transmission: 'Automatic',
			description: 'A reliable family SUV.',
			photoUrls: [],
			sourceUrl: 'https://example.com/listings/rav4-2020'
		};
		vi.stubGlobal(
			'fetch',
			vi.fn().mockResolvedValue({ ok: true, status: 200, json: async () => mockCar })
		);

		render(Page);

		const link = await screen.findByRole('link', { name: /view original listing/i });
		expect(link.getAttribute('href')).toBe(mockCar.sourceUrl);
		expect(link.getAttribute('target')).toBe('_blank');
	});

	it('omits the outbound source listing link entirely when sourceUrl is absent (R3.2)', async () => {
		const mockCar = {
			id: '1',
			make: 'Toyota',
			model: 'RAV4',
			year: 2020,
			price: 25000,
			mileage: 42000,
			transmission: 'Automatic',
			description: 'A reliable family SUV.',
			photoUrls: []
			// no sourceUrl field at all
		};
		vi.stubGlobal(
			'fetch',
			vi.fn().mockResolvedValue({ ok: true, status: 200, json: async () => mockCar })
		);

		const { container } = render(Page);

		await waitFor(() => {
			expect(container.textContent).toContain('Toyota');
		});

		expect(screen.queryByRole('link', { name: /view original listing/i })).toBeNull();
	});

	it('applies the new premium palette background color token to :root (R4.1, BOH-29)', () => {
		// Mirrors `frontend/src/routes/page.svelte.test.ts`'s equivalent test for
		// the search page's `+page.svelte`. This route compiles its own separate
		// component with its own duplicated `:root` token block (see design.md's
		// note on the unresolved duplication), so this is a genuinely distinct
		// assertion against a distinct <style> block/CSSStyleSheet -- not a
		// retest of the already-passing search-page assertion. Same jsdom
		// caveat applies here as there: `getComputedStyle(...).backgroundColor`
		// doesn't resolve `var(--color-bg)` to a computed value, but reading the
		// custom property directly off `:root` does resolve to its authored
		// literal, and this project's Vite config (`css: mode === 'test' ?
		// 'injected' : 'external'`) injects every route's <style> block as a
		// real <style> tag under test, this one included.
		//
		// New palette per docs/premium-redesign/design.md: --color-bg: #FAF8F4
		// (light mode), replacing this file's current (old, still-unsynced)
		// --color-bg: #f8f7f5.
		const mockCar = {
			id: '1',
			make: 'Toyota',
			model: 'RAV4',
			year: 2020,
			price: 25000,
			mileage: 42000,
			transmission: 'Automatic',
			description: 'A reliable family SUV.',
			photoUrls: []
		};
		vi.stubGlobal(
			'fetch',
			vi.fn().mockResolvedValue({ ok: true, status: 200, json: async () => mockCar })
		);

		render(Page);

		const colorBg = getComputedStyle(document.documentElement).getPropertyValue('--color-bg').trim();
		expect(colorBg).toBe('#FAF8F4');
	});

	it("applies the .tabular monospace treatment to the listing's price and mileage elements (R4.1, BOH-29)", async () => {
		const mockCar = {
			id: '1',
			make: 'Toyota',
			model: 'RAV4',
			year: 2020,
			price: 25000,
			mileage: 42000,
			transmission: 'Automatic',
			description: 'A reliable family SUV.',
			photoUrls: []
		};
		vi.stubGlobal(
			'fetch',
			vi.fn().mockResolvedValue({ ok: true, status: 200, json: async () => mockCar })
		);

		const { container } = render(Page);

		await waitFor(() => {
			expect(container.textContent).toContain('Toyota');
		});

		// Real DOM-structure assertions, matching the search page's equivalent
		// test's approach: the .price and .mileage elements must actually carry
		// the .tabular utility class, not merely have it defined-but-unused in
		// CSS.
		const priceEl = container.querySelector('.detail-info .price');
		const mileageEl = container.querySelector('.detail-info .mileage');
		expect(priceEl).not.toBeNull();
		expect(mileageEl).not.toBeNull();
		expect(priceEl!.classList.contains('tabular')).toBe(true);
		expect(mileageEl!.classList.contains('tabular')).toBe(true);
	});
});
