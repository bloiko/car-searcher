import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/svelte';
import Page from './+page.svelte';

afterEach(() => {
	cleanup();
	vi.unstubAllGlobals();
});

describe('search page', () => {
	it('renders a result\'s make, model, year and price after submitting a query', async () => {
		const mockResult = {
			id: '1',
			make: 'Toyota',
			model: 'RAV4',
			year: 2020,
			price: 25000,
			description: 'A reliable family SUV under 30k',
			photoUrls: []
		};
		vi.stubGlobal(
			'fetch',
			vi.fn().mockResolvedValue({
				ok: true,
				json: async () => ({ results: [mockResult] })
			})
		);

		const { container } = render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'reliable family suv under 30k' } });
		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(container.textContent).toContain('Toyota');
		});
		expect(container.textContent).toContain('RAV4');
		expect(container.textContent).toContain('2020');
		expect(container.textContent).toContain('25000');
	});

	it('renders a result\'s mileage after submitting a query', async () => {
		const mockResult = {
			id: '1',
			make: 'Toyota',
			model: 'RAV4',
			year: 2020,
			price: 25000,
			mileage: 42000,
			description: 'A reliable family SUV under 30k',
			photoUrls: []
		};
		vi.stubGlobal(
			'fetch',
			vi.fn().mockResolvedValue({
				ok: true,
				json: async () => ({ results: [mockResult] })
			})
		);

		const { container } = render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'reliable family suv under 30k' } });
		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(container.textContent).toContain('Toyota');
		});
		// Rendered as the raw number, matching how price and year are rendered
		// elsewhere on the card (no thousands separator, no "mi" unit suffix).
		expect(container.textContent).toContain('42000');
	});

	it('renders the first photo URL as a thumbnail image when a result has photos', async () => {
		const mockResult = {
			id: '1',
			make: 'Toyota',
			model: 'RAV4',
			year: 2020,
			price: 25000,
			description: 'A reliable family SUV under 30k',
			photoUrls: ['https://example.com/rav4-front.jpg', 'https://example.com/rav4-side.jpg']
		};
		vi.stubGlobal(
			'fetch',
			vi.fn().mockResolvedValue({
				ok: true,
				json: async () => ({ results: [mockResult] })
			})
		);

		const { container } = render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'reliable family suv under 30k' } });
		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(container.querySelector('img')).not.toBeNull();
		});

		const img = container.querySelector('img') as HTMLImageElement;
		expect(img.src).toBe('https://example.com/rav4-front.jpg');
	});

	it('wraps each result card in a link to its detail page (R1.3)', async () => {
		const mockResult = {
			id: '42',
			make: 'Toyota',
			model: 'RAV4',
			year: 2020,
			price: 25000,
			description: 'A reliable family SUV under 30k',
			photoUrls: []
		};
		vi.stubGlobal(
			'fetch',
			vi.fn().mockResolvedValue({
				ok: true,
				json: async () => ({ results: [mockResult] })
			})
		);

		const { container } = render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'reliable family suv under 30k' } });
		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(container.textContent).toContain('Toyota');
		});

		// Navigating to the detail page is what R1.3 requires -- the whole card
		// (not just the make/model heading) is expected to be the click target,
		// matching design.md's "each result card ... becomes a link ... wrapping
		// the existing card markup".
		const link = container.querySelector('a[href="/cars/42"]');
		expect(link).not.toBeNull();
		expect(link?.textContent).toContain('RAV4');
	});

	it('includes filters.priceMax in the submitted request body when max price is set', async () => {
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			json: async () => ({ results: [] })
		});
		vi.stubGlobal('fetch', fetchMock);

		render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		const maxPriceInput = screen.getByLabelText(/max price/i);
		await fireEvent.input(maxPriceInput, { target: { value: '25000' } });

		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(fetchMock).toHaveBeenCalled();
		});

		const [, options] = fetchMock.mock.calls[0];
		const body = JSON.parse(options.body as string);
		expect(body.filters).toEqual({ priceMax: 25000 });
	});

	it('includes filters.yearMin in the submitted request body when min year is set', async () => {
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			json: async () => ({ results: [] })
		});
		vi.stubGlobal('fetch', fetchMock);

		render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		const yearMinInput = screen.getByLabelText(/min year/i);
		await fireEvent.input(yearMinInput, { target: { value: '2018' } });

		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(fetchMock).toHaveBeenCalled();
		});

		const [, options] = fetchMock.mock.calls[0];
		const body = JSON.parse(options.body as string);
		expect(body.filters).toEqual({ yearMin: 2018 });
	});

	it('includes filters.mileageMax in the submitted request body when max mileage is set', async () => {
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			json: async () => ({ results: [] })
		});
		vi.stubGlobal('fetch', fetchMock);

		render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		const mileageMaxInput = screen.getByLabelText(/max mileage/i);
		await fireEvent.input(mileageMaxInput, { target: { value: '60000' } });

		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(fetchMock).toHaveBeenCalled();
		});

		const [, options] = fetchMock.mock.calls[0];
		const body = JSON.parse(options.body as string);
		expect(body.filters).toEqual({ mileageMax: 60000 });
	});

	it('includes filters.make in the submitted request body when make is set', async () => {
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			json: async () => ({ results: [] })
		});
		vi.stubGlobal('fetch', fetchMock);

		render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		const makeInput = screen.getByLabelText(/^make/i);
		await fireEvent.input(makeInput, { target: { value: 'Toyota' } });

		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(fetchMock).toHaveBeenCalled();
		});

		const [, options] = fetchMock.mock.calls[0];
		const body = JSON.parse(options.body as string);
		expect(body.filters).toEqual({ make: 'Toyota' });
	});

	it('includes filters.model in the submitted request body when model is set', async () => {
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			json: async () => ({ results: [] })
		});
		vi.stubGlobal('fetch', fetchMock);

		render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		const modelInput = screen.getByLabelText(/^model/i);
		await fireEvent.input(modelInput, { target: { value: 'RAV4' } });

		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(fetchMock).toHaveBeenCalled();
		});

		const [, options] = fetchMock.mock.calls[0];
		const body = JSON.parse(options.body as string);
		expect(body.filters).toEqual({ model: 'RAV4' });
	});

	it('includes filters.transmission in the submitted request body when transmission is set', async () => {
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			json: async () => ({ results: [] })
		});
		vi.stubGlobal('fetch', fetchMock);

		render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		const transmissionInput = screen.getByLabelText(/transmission/i);
		await fireEvent.change(transmissionInput, { target: { value: 'Automatic' } });

		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(fetchMock).toHaveBeenCalled();
		});

		const [, options] = fetchMock.mock.calls[0];
		const body = JSON.parse(options.body as string);
		expect(body.filters).toEqual({ transmission: 'Automatic' });
	});

	it('combines multiple set filters into one filters object', async () => {
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			json: async () => ({ results: [] })
		});
		vi.stubGlobal('fetch', fetchMock);

		render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		await fireEvent.input(screen.getByLabelText(/max price/i), { target: { value: '25000' } });
		await fireEvent.input(screen.getByLabelText(/min year/i), { target: { value: '2018' } });
		await fireEvent.input(screen.getByLabelText(/max mileage/i), { target: { value: '60000' } });
		await fireEvent.input(screen.getByLabelText(/^make/i), { target: { value: 'Toyota' } });

		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(fetchMock).toHaveBeenCalled();
		});

		const [, options] = fetchMock.mock.calls[0];
		const body = JSON.parse(options.body as string);
		expect(body.filters).toEqual({
			priceMax: 25000,
			yearMin: 2018,
			mileageMax: 60000,
			make: 'Toyota'
		});
	});

	it('omits filters entirely from the request body when no filter inputs are set', async () => {
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			json: async () => ({ results: [] })
		});
		vi.stubGlobal('fetch', fetchMock);

		render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(fetchMock).toHaveBeenCalled();
		});

		const [, options] = fetchMock.mock.calls[0];
		const body = JSON.parse(options.body as string);
		expect(body).not.toHaveProperty('filters');
		// `page` is always sent (BOH-17 task 2) — unlike the optional filters/sort,
		// `0` is a meaningful default the request body carries explicitly.
		expect(body).toEqual({ query: 'suv', page: 0 });
	});

	it('omits a filter input that was set and then cleared back to empty', async () => {
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			json: async () => ({ results: [] })
		});
		vi.stubGlobal('fetch', fetchMock);

		render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		const maxPriceInput = screen.getByLabelText(/max price/i);
		await fireEvent.input(maxPriceInput, { target: { value: '25000' } });
		await fireEvent.input(maxPriceInput, { target: { value: '' } });

		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(fetchMock).toHaveBeenCalled();
		});

		const [, options] = fetchMock.mock.calls[0];
		const body = JSON.parse(options.body as string);
		expect(body).not.toHaveProperty('filters');
	});

	it('includes a top-level sort value in the submitted request body when a sort option is selected', async () => {
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			json: async () => ({ results: [] })
		});
		vi.stubGlobal('fetch', fetchMock);

		render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		const sortSelect = screen.getByLabelText(/sort/i);
		await fireEvent.change(sortSelect, { target: { value: 'price_asc' } });

		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(fetchMock).toHaveBeenCalled();
		});

		const [, options] = fetchMock.mock.calls[0];
		const body = JSON.parse(options.body as string);
		// `sort` is a top-level sibling of `query`/`filters`, not nested inside
		// `filters` — it changes result ordering, not the result set.
		expect(body.sort).toBe('price_asc');
	});

	it('omits sort from the request body when the sort control is left at its default (Best match)', async () => {
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			json: async () => ({ results: [] })
		});
		vi.stubGlobal('fetch', fetchMock);

		render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(fetchMock).toHaveBeenCalled();
		});

		const [, options] = fetchMock.mock.calls[0];
		const body = JSON.parse(options.body as string);
		expect(body).not.toHaveProperty('sort');
	});

	it('presents all six classic filter inputs immediately on page load, with no drawer-opening interaction required (R3.1)', () => {
		render(Page);

		// No click on the "Filters" toggle (or anything else) happens before
		// these assertions — per R3.1, the classic filter controls (make,
		// model, year, mileage, transmission) must be immediately visible/
		// queryable right after render, not hidden behind a collapsed drawer.
		expect(screen.getByLabelText(/max price/i)).not.toBeNull();
		expect(screen.getByLabelText(/min year/i)).not.toBeNull();
		expect(screen.getByLabelText(/max mileage/i)).not.toBeNull();
		expect(screen.getByLabelText(/^make/i)).not.toBeNull();
		expect(screen.getByLabelText(/^model/i)).not.toBeNull();
		expect(screen.getByLabelText(/transmission/i)).not.toBeNull();
	});

	it('renders with a defined color, typography, and spacing system instead of unstyled browser defaults', () => {
		const { container } = render(Page);

		const h1 = container.querySelector('h1') as HTMLElement;
		const searchInput = screen.getByRole('textbox', { name: /additional details/i });

		const h1Style = getComputedStyle(h1);
		const inputStyle = getComputedStyle(searchInput);
		const bodyStyle = getComputedStyle(document.body);

		// Typography: a real font stack must be defined, not left at jsdom's
		// "unset" placeholder for the browser-default font.
		expect(h1Style.fontFamily).not.toBe('depends on user agent');
		// Spacing: form inputs must have deliberate padding from a spacing scale,
		// not the browser-default zero.
		expect(inputStyle.padding).not.toBe('0');
		// Color: the page background must come from an explicit token, not the
		// browser-default transparent.
		expect(bodyStyle.backgroundColor).not.toBe('rgba(0, 0, 0, 0)');
	});

	it('shows a status-role loading indicator while a search request is in flight', async () => {
		vi.stubGlobal(
			'fetch',
			// A request that never resolves within the test's lifetime, so
			// `loading` stays true long enough to assert against it.
			vi.fn().mockReturnValue(new Promise(() => {}))
		);

		render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'suv' } });
		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(screen.getByRole('status')).not.toBeNull();
		});
	});

	it('shows a dismissible chip for a set filter and clears that filter when the chip is dismissed', async () => {
		render(Page);

		const maxPriceInput = screen.getByLabelText(/max price/i) as HTMLInputElement;
		await fireEvent.input(maxPriceInput, { target: { value: '25000' } });

		// Setting the filter should surface a labeled chip showing its value.
		expect(screen.getByText(/max price.*25000/i)).not.toBeNull();

		// The chip exposes its own dismiss control, distinguishable by an
		// accessible name that references the filter it clears.
		const removeChipButton = screen.getByRole('button', { name: /remove.*max price.*filter/i });
		await fireEvent.click(removeChipButton);

		// Dismissing the chip clears the underlying filter input...
		expect(maxPriceInput.value).toBe('');
		// ...and the chip itself disappears now that the filter is unset.
		expect(screen.queryByText(/max price.*25000/i)).toBeNull();
	});

	it('shows Previous disabled and Next enabled when total exceeds the page size (R3.2)', async () => {
		const mockResult = {
			id: '1',
			make: 'Toyota',
			model: 'RAV4',
			year: 2020,
			price: 25000,
			mileage: 42000,
			description: 'A reliable family SUV under 30k',
			photoUrls: []
		};
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			// 45 total matches with an assumed pageSize of 20 (the backend default,
			// BOH-17 task 1): more than one page, so pagination controls must show,
			// and page 0 means there's a next page but no previous one.
			json: async () => ({ results: [mockResult], total: 45 })
		});
		vi.stubGlobal('fetch', fetchMock);

		render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'suv' } });
		await fireEvent.submit(input.closest('form')!);

		const previousButton = (await screen.findByRole('button', {
			name: /^previous$/i
		})) as HTMLButtonElement;
		const nextButton = screen.getByRole('button', { name: /^next$/i }) as HTMLButtonElement;

		expect(previousButton.disabled).toBe(true);
		expect(nextButton.disabled).toBe(false);
	});

	it('resubmits the search with page: 1 in the request body when Next is clicked (R3.1)', async () => {
		const mockResult = {
			id: '1',
			make: 'Toyota',
			model: 'RAV4',
			year: 2020,
			price: 25000,
			mileage: 42000,
			description: 'A reliable family SUV under 30k',
			photoUrls: []
		};
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			json: async () => ({ results: [mockResult], total: 45 })
		});
		vi.stubGlobal('fetch', fetchMock);

		render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'suv' } });
		await fireEvent.submit(input.closest('form')!);

		const nextButton = await screen.findByRole('button', { name: /^next$/i });
		await fireEvent.click(nextButton);

		await waitFor(() => {
			expect(fetchMock).toHaveBeenCalledTimes(2);
		});

		const [, options] = fetchMock.mock.calls[1];
		const body = JSON.parse(options.body as string);
		expect(body.page).toBe(1);
	});

	it('resets page to 0 when a new search is submitted, discarding stale pagination (R3.1)', async () => {
		const mockResult = {
			id: '1',
			make: 'Toyota',
			model: 'RAV4',
			year: 2020,
			price: 25000,
			mileage: 42000,
			description: 'A reliable family SUV under 30k',
			photoUrls: []
		};
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			json: async () => ({ results: [mockResult], total: 45 })
		});
		vi.stubGlobal('fetch', fetchMock);

		render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'suv' } });
		await fireEvent.submit(input.closest('form')!);

		// Move to page 1 first, so there's stale pagination state to discard.
		const nextButton = await screen.findByRole('button', { name: /^next$/i });
		await fireEvent.click(nextButton);

		await waitFor(() => {
			expect(fetchMock).toHaveBeenCalledTimes(2);
		});
		const [, pageOneOptions] = fetchMock.mock.calls[1];
		expect(JSON.parse(pageOneOptions.body as string).page).toBe(1);

		// Submitting the form again is a "new" search — it should start over at
		// page 0, not resume from the page the user was previously on.
		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(fetchMock).toHaveBeenCalledTimes(3);
		});
		const [, newSearchOptions] = fetchMock.mock.calls[2];
		const body = JSON.parse(newSearchOptions.body as string);
		expect(body.page).toBe(0);
	});

	it('applies the new premium palette background color token to :root (R1.1, BOH-29)', () => {
		render(Page);

		// jsdom's CSSStyleDeclaration doesn't resolve `var(--color-bg)` references
		// down to a computed rgb() (confirmed empirically: `getComputedStyle(document.body).backgroundColor`
		// stays the literal string 'var(--color-bg)' even once the property itself
		// resolves) -- which is exactly why the existing "renders with a defined
		// color..." test above only asserts body background isn't transparent
		// rather than pinning a specific value. So this test instead reads the
		// custom property itself directly off `:root`, which jsdom does resolve
		// to its authored literal.
		//
		// jsdom also doesn't match `@media (prefers-color-scheme: dark)` (nothing
		// in this suite stubs a preference), so the light-mode `:root` block is
		// what's in effect here -- confirmed by probing the *current* unmodified
		// component, where this same property/element combo currently resolves
		// to the OLD light value '#f8f7f5', not the dark value.
		//
		// New palette per docs/premium-redesign/design.md: --color-bg: #FAF8F4
		// (light mode), replacing the old --color-bg: #f8f7f5.
		const colorBg = getComputedStyle(document.documentElement).getPropertyValue('--color-bg').trim();
		expect(colorBg).toBe('#FAF8F4');
	});

	it('defines a --font-mono custom property resolving to a real monospace stack (R1.3, BOH-29)', () => {
		render(Page);

		const rootStyle = getComputedStyle(document.documentElement);
		const fontMono = rootStyle.getPropertyValue('--font-mono').trim();

		// Task 2 (not this task) wires `.tabular` onto the actual price/mileage
		// elements, so this only checks the token itself exists on :root and
		// names a monospace stack, per design.md: `ui-monospace, "SF Mono",
		// "Cascadia Code", "Roboto Mono", monospace`.
		expect(fontMono).not.toBe('');
		expect(fontMono.toLowerCase()).toContain('monospace');
	});

	it('collapses the result card grid to a single column under a 720px media query', () => {
		render(Page);

		// The dev-mode Vite config forces `compilerOptions.css: 'injected'` under
		// `mode === 'test'` specifically so the component's <style> block is
		// injected as a real <style> tag (and thus a real CSSStyleSheet) into the
		// jsdom document, instead of being extracted to a separate .css file the
		// way it is for real dev/build. That lets this test inspect the actual
		// parsed CSSOM rather than a mock.
		let mediaRule: CSSMediaRule | undefined;
		let singleColumnResultsRule: CSSStyleRule | undefined;

		for (const sheet of Array.from(document.styleSheets)) {
			let rules: CSSRuleList;
			try {
				rules = sheet.cssRules;
			} catch {
				continue;
			}

			for (const rule of Array.from(rules)) {
				if (
					rule instanceof CSSMediaRule &&
					/max-width:\s*720px/i.test(rule.conditionText || rule.media.mediaText)
				) {
					mediaRule = rule;
					for (const innerRule of Array.from(rule.cssRules)) {
						if (
							innerRule instanceof CSSStyleRule &&
							innerRule.selectorText.includes('.results') &&
							/^1fr$/.test(innerRule.style.gridTemplateColumns.trim())
						) {
							singleColumnResultsRule = innerRule;
						}
					}
				}
			}
		}

		// R5.1: below a 720px viewport, `.results` (the card grid) must collapse
		// from `repeat(auto-fill, minmax(16rem, 1fr))` to a single column.
		expect(mediaRule).toBeDefined();
		expect(singleColumnResultsRule).toBeDefined();
	});

	it('applies the .tabular monospace treatment to a result card\'s price and mileage elements (R3.2, BOH-29)', async () => {
		const mockResult = {
			id: '1',
			make: 'Toyota',
			model: 'RAV4',
			year: 2020,
			price: 25000,
			mileage: 42000,
			description: 'A reliable family SUV under 30k',
			photoUrls: []
		};
		vi.stubGlobal(
			'fetch',
			vi.fn().mockResolvedValue({
				ok: true,
				json: async () => ({ results: [mockResult] })
			})
		);

		const { container } = render(Page);

		const input = screen.getByRole('textbox', { name: /additional details/i });
		await fireEvent.input(input, { target: { value: 'reliable family suv under 30k' } });
		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(container.textContent).toContain('Toyota');
		});

		// Real DOM-structure assertions, not a CSS-only check: the .price and
		// .mileage elements inside a rendered .result-card must actually carry
		// the .tabular utility class from design.md ("a real assertion that
		// the numeric treatment is actually applied, not just present in CSS
		// unused" per docs/premium-redesign/design.md's testing strategy).
		const priceEl = container.querySelector('.result-card .price');
		const mileageEl = container.querySelector('.result-card .mileage');
		expect(priceEl).not.toBeNull();
		expect(mileageEl).not.toBeNull();
		expect(priceEl!.classList.contains('tabular')).toBe(true);
		expect(mileageEl!.classList.contains('tabular')).toBe(true);
	});

	it('defines a reduced-motion-gated .result-card:hover rule that lifts the card via translateY (R3.1, BOH-29)', () => {
		render(Page);

		// Same CSSOM-inspection technique as the 720px breakpoint test above:
		// the dev-mode Vite config injects the component's <style> block as a
		// real <style> tag (a real CSSStyleSheet) under `mode === 'test'`, so
		// the actual parsed rule can be inspected directly instead of relying
		// on getComputedStyle (which can't evaluate a `:hover` pseudo-class or
		// resolve `@media (prefers-reduced-motion: ...)` in jsdom, since jsdom
		// has no real layout/animation engine to trigger either).
		let reducedMotionMediaRule: CSSMediaRule | undefined;
		let hoverRule: CSSStyleRule | undefined;

		for (const sheet of Array.from(document.styleSheets)) {
			let rules: CSSRuleList;
			try {
				rules = sheet.cssRules;
			} catch {
				continue;
			}

			for (const rule of Array.from(rules)) {
				if (
					rule instanceof CSSMediaRule &&
					/prefers-reduced-motion:\s*no-preference/i.test(
						rule.conditionText || rule.media.mediaText
					)
				) {
					reducedMotionMediaRule = rule;
					for (const innerRule of Array.from(rule.cssRules)) {
						if (
							innerRule instanceof CSSStyleRule &&
							innerRule.selectorText.includes('.result-card:hover')
						) {
							hoverRule = innerRule;
						}
					}
				}
			}
		}

		// R3.1: the hover lift (translateY) + shadow deepen must be scoped
		// inside `@media (prefers-reduced-motion: no-preference)`, so a
		// reduced-motion user gets no transform/transition at all.
		expect(reducedMotionMediaRule).toBeDefined();
		expect(hoverRule).toBeDefined();
		expect(hoverRule!.style.transform).toContain('translateY');
	});
});
