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
});
