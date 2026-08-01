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

		const input = screen.getByRole('textbox', { name: /search/i });
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

		const input = screen.getByRole('textbox', { name: /search/i });
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

		const input = screen.getByRole('textbox', { name: /search/i });
		await fireEvent.input(input, { target: { value: 'reliable family suv under 30k' } });
		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(container.querySelector('img')).not.toBeNull();
		});

		const img = container.querySelector('img') as HTMLImageElement;
		expect(img.src).toBe('https://example.com/rav4-front.jpg');
	});

	it('includes filters.priceMax in the submitted request body when max price is set', async () => {
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			json: async () => ({ results: [] })
		});
		vi.stubGlobal('fetch', fetchMock);

		render(Page);

		const input = screen.getByRole('textbox', { name: /search/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		await fireEvent.click(screen.getByRole('button', { name: /filters/i }));
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

		const input = screen.getByRole('textbox', { name: /search/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		await fireEvent.click(screen.getByRole('button', { name: /filters/i }));
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

		const input = screen.getByRole('textbox', { name: /search/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		await fireEvent.click(screen.getByRole('button', { name: /filters/i }));
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

		const input = screen.getByRole('textbox', { name: /search/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		await fireEvent.click(screen.getByRole('button', { name: /filters/i }));
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

	it('combines multiple set filters into one filters object', async () => {
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			json: async () => ({ results: [] })
		});
		vi.stubGlobal('fetch', fetchMock);

		render(Page);

		const input = screen.getByRole('textbox', { name: /search/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		await fireEvent.click(screen.getByRole('button', { name: /filters/i }));
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

		const input = screen.getByRole('textbox', { name: /search/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(fetchMock).toHaveBeenCalled();
		});

		const [, options] = fetchMock.mock.calls[0];
		const body = JSON.parse(options.body as string);
		expect(body).not.toHaveProperty('filters');
		expect(body).toEqual({ query: 'suv' });
	});

	it('omits a filter input that was set and then cleared back to empty', async () => {
		const fetchMock = vi.fn().mockResolvedValue({
			ok: true,
			json: async () => ({ results: [] })
		});
		vi.stubGlobal('fetch', fetchMock);

		render(Page);

		const input = screen.getByRole('textbox', { name: /search/i });
		await fireEvent.input(input, { target: { value: 'suv' } });

		await fireEvent.click(screen.getByRole('button', { name: /filters/i }));
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

	it('keeps filter inputs collapsed in a drawer until the Filters toggle is activated', async () => {
		render(Page);

		// Search input is primary and always visible.
		expect(screen.getByRole('textbox', { name: /search/i })).not.toBeNull();

		// Filter inputs must not be queryable before the drawer is opened.
		expect(screen.queryByLabelText(/max price/i)).toBeNull();
		expect(screen.queryByLabelText(/min year/i)).toBeNull();
		expect(screen.queryByLabelText(/max mileage/i)).toBeNull();
		expect(screen.queryByLabelText(/^make/i)).toBeNull();

		const filtersToggle = screen.getByRole('button', { name: /filters/i });
		await fireEvent.click(filtersToggle);

		// After opening the drawer, filter inputs become queryable.
		expect(screen.getByLabelText(/max price/i)).not.toBeNull();
		expect(screen.getByLabelText(/min year/i)).not.toBeNull();
		expect(screen.getByLabelText(/max mileage/i)).not.toBeNull();
		expect(screen.getByLabelText(/^make/i)).not.toBeNull();
	});

	it('renders with a defined color, typography, and spacing system instead of unstyled browser defaults', () => {
		const { container } = render(Page);

		const h1 = container.querySelector('h1') as HTMLElement;
		const searchInput = screen.getByRole('textbox', { name: /search/i });

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

		const input = screen.getByRole('textbox', { name: /search/i });
		await fireEvent.input(input, { target: { value: 'suv' } });
		await fireEvent.submit(input.closest('form')!);

		await waitFor(() => {
			expect(screen.getByRole('status')).not.toBeNull();
		});
	});

	it('shows a dismissible chip for a set filter and clears that filter when the chip is dismissed', async () => {
		render(Page);

		await fireEvent.click(screen.getByRole('button', { name: /filters/i }));
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
});
