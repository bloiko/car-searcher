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
			description: 'A reliable family SUV under 30k'
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
});
