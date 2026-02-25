// @vitest-environment jsdom
import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest'
import { render, screen, fireEvent, cleanup, waitFor } from '@testing-library/react'
import { Pagination } from '../Pagination'

afterEach(cleanup)

const mockSetPage = vi.fn()
const mockSetPageSize = vi.fn()

vi.mock('../../stores/gameStore', () => ({
  useGameStore: () => ({
    pageSize: 20,
    setPage: mockSetPage,
    setPageSize: mockSetPageSize,
  }),
}))

beforeEach(() => {
  mockSetPage.mockReset()
  mockSetPageSize.mockReset()
})

describe('Pagination', () => {
  it('renders nothing when totalPages <= 1', () => {
    const { container } = render(
      <Pagination currentPage={0} totalPages={1} totalElements={5} />
    )
    expect(container.firstChild).toBeNull()
  })

  it('renders nothing when totalPages is 0', () => {
    const { container } = render(
      <Pagination currentPage={0} totalPages={0} totalElements={0} />
    )
    expect(container.firstChild).toBeNull()
  })

  it('renders pagination controls when totalPages > 1', () => {
    const { container } = render(
      <Pagination currentPage={0} totalPages={3} totalElements={55} />
    )
    // Atlaskit Pagination renders numbered page buttons
    expect(container.firstChild).not.toBeNull()
    // Should display page numbers
    expect(screen.getByText('1')).toBeTruthy()
    expect(screen.getByText('2')).toBeTruthy()
    expect(screen.getByText('3')).toBeTruthy()
  })

  it('renders page count info', () => {
    render(<Pagination currentPage={0} totalPages={3} totalElements={55} />)
    // First page: 1-20 / 55 件
    expect(screen.getByText(/55/)).toBeTruthy()
  })

  it('calls setPage when a numbered page button clicked', () => {
    render(<Pagination currentPage={0} totalPages={3} totalElements={60} />)
    fireEvent.click(screen.getByText('2'))
    expect(mockSetPage).toHaveBeenCalledWith(1)
  })

  it('calls setPage when page 3 button clicked', () => {
    render(<Pagination currentPage={0} totalPages={3} totalElements={60} />)
    fireEvent.click(screen.getByText('3'))
    expect(mockSetPage).toHaveBeenCalledWith(2)
  })

  it('renders page size selector with Atlaskit Select', () => {
    const { container } = render(
      <Pagination currentPage={0} totalPages={3} totalElements={55} />
    )
    // Atlaskit Select renders a combobox or input for page size
    expect(container.textContent).toContain('表示件数')
    expect(container.textContent).toContain('20件')
  })

  it('calls setPageSize when page size option selected', async () => {
    const { container } = render(
      <Pagination currentPage={0} totalPages={3} totalElements={55} />
    )
    // Open the Atlaskit Select menu
    const selectControl = container.querySelector('[id="page-size"]')
      || screen.getByRole('combobox')
    fireEvent.mouseDown(selectControl)

    await waitFor(() => {
      const option50 = screen.getByText('50件')
      fireEvent.click(option50)
    })

    expect(mockSetPageSize).toHaveBeenCalledWith(50)
  })

  it('shows available page size options in menu', async () => {
    const { container } = render(
      <Pagination currentPage={0} totalPages={3} totalElements={55} />
    )
    // Open the Atlaskit Select menu via its control area
    const selectControl = container.querySelector('[id="page-size"]')
      || screen.getByRole('combobox')
    fireEvent.mouseDown(selectControl)

    await waitFor(() => {
      // Use role="option" to find items in the opened dropdown menu
      const options = screen.getAllByRole('option')
      const optionTexts = options.map(o => o.textContent)
      expect(optionTexts).toContain('10件')
      expect(optionTexts).toContain('20件')
      expect(optionTexts).toContain('50件')
      expect(optionTexts).toContain('100件')
    })
  })
})
