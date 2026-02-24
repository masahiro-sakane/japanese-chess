// @vitest-environment jsdom
import { describe, it, expect, vi, afterEach } from 'vitest'
import { render, screen, fireEvent, cleanup } from '@testing-library/react'
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
    render(<Pagination currentPage={0} totalPages={3} totalElements={55} />)
    expect(screen.getByText('最初')).toBeTruthy()
    expect(screen.getByText('前へ')).toBeTruthy()
    expect(screen.getByText('次へ')).toBeTruthy()
    expect(screen.getByText('最後')).toBeTruthy()
  })

  it('disables 最初/前へ on first page', () => {
    render(<Pagination currentPage={0} totalPages={3} totalElements={55} />)
    expect((screen.getByText('最初') as HTMLButtonElement).disabled).toBe(true)
    expect((screen.getByText('前へ') as HTMLButtonElement).disabled).toBe(true)
  })

  it('disables 次へ/最後 on last page', () => {
    render(<Pagination currentPage={2} totalPages={3} totalElements={55} />)
    expect((screen.getByText('次へ') as HTMLButtonElement).disabled).toBe(true)
    expect((screen.getByText('最後') as HTMLButtonElement).disabled).toBe(true)
  })

  it('calls setPage(0) when 最初 clicked', () => {
    render(<Pagination currentPage={2} totalPages={5} totalElements={100} />)
    fireEvent.click(screen.getByText('最初'))
    expect(mockSetPage).toHaveBeenCalledWith(0)
  })

  it('calls setPage(currentPage - 1) when 前へ clicked', () => {
    render(<Pagination currentPage={2} totalPages={5} totalElements={100} />)
    fireEvent.click(screen.getByText('前へ'))
    expect(mockSetPage).toHaveBeenCalledWith(1)
  })

  it('calls setPage(currentPage + 1) when 次へ clicked', () => {
    render(<Pagination currentPage={1} totalPages={5} totalElements={100} />)
    fireEvent.click(screen.getByText('次へ'))
    expect(mockSetPage).toHaveBeenCalledWith(2)
  })

  it('calls setPage(totalPages - 1) when 最後 clicked', () => {
    render(<Pagination currentPage={1} totalPages={5} totalElements={100} />)
    fireEvent.click(screen.getByText('最後'))
    expect(mockSetPage).toHaveBeenCalledWith(4)
  })

  it('calls setPage when a numbered page button clicked', () => {
    render(<Pagination currentPage={0} totalPages={3} totalElements={60} />)
    fireEvent.click(screen.getByText('2'))
    expect(mockSetPage).toHaveBeenCalledWith(1)
  })

  it('renders page count info', () => {
    render(<Pagination currentPage={0} totalPages={3} totalElements={55} />)
    // First page: 1-20 / 55 件
    expect(screen.getByText(/55/)).toBeTruthy()
  })

  it('calls setPageSize when page size selector changes', () => {
    render(<Pagination currentPage={0} totalPages={3} totalElements={55} />)
    const select = screen.getByRole('combobox')
    fireEvent.change(select, { target: { value: '50' } })
    expect(mockSetPageSize).toHaveBeenCalledWith(50)
  })

  it('renders page size options', () => {
    render(<Pagination currentPage={0} totalPages={3} totalElements={55} />)
    const options = screen.getAllByRole('option')
    const values = options.map((o) => (o as HTMLOptionElement).value)
    expect(values).toContain('10')
    expect(values).toContain('20')
    expect(values).toContain('50')
    expect(values).toContain('100')
  })
})
