// @vitest-environment jsdom
import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest'
import { render, screen, fireEvent, cleanup, waitFor } from '@testing-library/react'
import { StatusFilter } from '../StatusFilter'

afterEach(cleanup)

const mockSetStatusFilter = vi.fn()

vi.mock('../../stores/gameStore', () => ({
  useGameStore: () => ({
    statusFilter: null,
    setStatusFilter: mockSetStatusFilter,
  }),
}))

beforeEach(() => {
  mockSetStatusFilter.mockReset()
})

describe('StatusFilter', () => {
  it('renders a select input', () => {
    render(<StatusFilter />)
    const input = screen.getByRole('combobox')
    expect(input).toBeTruthy()
  })

  it('shows default selected value (すべて)', () => {
    const { container } = render(<StatusFilter />)
    // Atlaskit Select shows selected value as text in its single-value container
    expect(container.textContent).toContain('すべて')
  })

  it('opens menu and shows all options when clicked', async () => {
    const { container } = render(<StatusFilter />)
    // Atlaskit Select opens on mouseDown on the control area
    const control = container.querySelector('[class*="control"]') || screen.getByRole('combobox')
    fireEvent.mouseDown(control)

    await waitFor(() => {
      expect(screen.getByText('対局中')).toBeTruthy()
      expect(screen.getByText('終了')).toBeTruthy()
    })
  })

  it('calls setStatusFilter("IN_PROGRESS") when 対局中 clicked in menu', async () => {
    const { container } = render(<StatusFilter />)
    const control = container.querySelector('[class*="control"]') || screen.getByRole('combobox')
    fireEvent.mouseDown(control)

    await waitFor(() => {
      const option = screen.getByText('対局中')
      fireEvent.click(option)
    })

    expect(mockSetStatusFilter).toHaveBeenCalledWith('IN_PROGRESS')
  })

  it('calls setStatusFilter("FINISHED") when 終了 clicked in menu', async () => {
    const { container } = render(<StatusFilter />)
    const control = container.querySelector('[class*="control"]') || screen.getByRole('combobox')
    fireEvent.mouseDown(control)

    await waitFor(() => {
      const option = screen.getByText('終了')
      fireEvent.click(option)
    })

    expect(mockSetStatusFilter).toHaveBeenCalledWith('FINISHED')
  })

  it('calls setStatusFilter(null) when すべて selected', async () => {
    const { container } = render(<StatusFilter />)
    const control = container.querySelector('[class*="control"]') || screen.getByRole('combobox')
    fireEvent.mouseDown(control)

    await waitFor(() => {
      // When menu opens, there are option elements with role="option"
      const option = screen.getByRole('option', { name: 'すべて' })
      fireEvent.click(option)
    })

    expect(mockSetStatusFilter).toHaveBeenCalledWith(null)
  })
})
