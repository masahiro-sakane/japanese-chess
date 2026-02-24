// @vitest-environment jsdom
import { describe, it, expect, vi, afterEach } from 'vitest'
import { render, screen, fireEvent, cleanup } from '@testing-library/react'
import { StatusFilter } from '../StatusFilter'

afterEach(cleanup)

const mockSetStatusFilter = vi.fn()

vi.mock('../../stores/gameStore', () => ({
  useGameStore: () => ({
    statusFilter: null,
    setStatusFilter: mockSetStatusFilter,
  }),
}))

describe('StatusFilter', () => {
  it('renders a select element', () => {
    render(<StatusFilter />)
    expect(screen.getByRole('combobox')).toBeTruthy()
  })

  it('shows all status options', () => {
    render(<StatusFilter />)
    expect(screen.getByRole('option', { name: 'すべて' })).toBeTruthy()
    expect(screen.getByRole('option', { name: '対局中' })).toBeTruthy()
    expect(screen.getByRole('option', { name: '終了' })).toBeTruthy()
  })

  it('defaults to empty value (all)', () => {
    render(<StatusFilter />)
    const select = screen.getByRole('combobox') as HTMLSelectElement
    expect(select.value).toBe('')
  })

  it('calls setStatusFilter(null) when "すべて" selected', () => {
    render(<StatusFilter />)
    const select = screen.getByRole('combobox')
    fireEvent.change(select, { target: { value: '' } })
    expect(mockSetStatusFilter).toHaveBeenCalledWith(null)
  })

  it('calls setStatusFilter("IN_PROGRESS") when 対局中 selected', () => {
    render(<StatusFilter />)
    const select = screen.getByRole('combobox')
    fireEvent.change(select, { target: { value: 'IN_PROGRESS' } })
    expect(mockSetStatusFilter).toHaveBeenCalledWith('IN_PROGRESS')
  })

  it('calls setStatusFilter("FINISHED") when 終了 selected', () => {
    render(<StatusFilter />)
    const select = screen.getByRole('combobox')
    fireEvent.change(select, { target: { value: 'FINISHED' } })
    expect(mockSetStatusFilter).toHaveBeenCalledWith('FINISHED')
  })

  it('reflects current statusFilter in select value', () => {
    vi.mocked(
      vi.importActual('../../stores/gameStore') as { useGameStore: () => unknown }
    )
    // Re-mock with active filter
    vi.doMock('../../stores/gameStore', () => ({
      useGameStore: () => ({
        statusFilter: 'IN_PROGRESS',
        setStatusFilter: mockSetStatusFilter,
      }),
    }))
  })
})
