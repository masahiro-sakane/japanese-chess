// @vitest-environment jsdom
import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest'
import { render, screen, fireEvent, cleanup, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { GameList } from '../GameList'
import type { GameQueryDto } from '../../types/api'

afterEach(cleanup)

const mockFetchGames = vi.fn()
const mockClearError = vi.fn()

let mockGames: GameQueryDto[] = []
let mockLoading = false
let mockError: string | null = null
let mockCurrentPage = 0
let mockTotalPages = 1
let mockTotalElements = 0

vi.mock('../../stores/gameStore', () => ({
  useGameStore: () => ({
    get games() { return mockGames },
    get loading() { return mockLoading },
    get error() { return mockError },
    get currentPage() { return mockCurrentPage },
    get totalPages() { return mockTotalPages },
    get totalElements() { return mockTotalElements },
    fetchGames: mockFetchGames,
    clearError: mockClearError,
  }),
}))

// Mock child components to isolate GameList
vi.mock('../Pagination', () => ({
  Pagination: ({ totalElements }: { totalElements: number }) => (
    <div data-testid="pagination">Pagination({totalElements})</div>
  ),
}))

vi.mock('../StatusFilter', () => ({
  StatusFilter: () => <div data-testid="status-filter" />,
}))

vi.mock('../GameCard', () => ({
  GameCard: ({ game }: { game: GameQueryDto }) => (
    <div data-testid={`game-card-${game.gameId}`}>{game.gameId}</div>
  ),
}))

function makeGame(id: string): GameQueryDto {
  return {
    gameId: id,
    blackPlayerId: 'bp',
    whitePlayerId: 'wp',
    status: 'IN_PROGRESS',
    currentTurn: 'BLACK',
    winner: null,
    endReason: null,
    boardState: [],
    blackCapturedPieces: [],
    whiteCapturedPieces: [],
    moveCount: 0,
    createdAt: '2026-01-01T00:00:00',
    updatedAt: '2026-01-01T00:00:00',
    blackInCheck: false,
    whiteInCheck: false,
    aiGame: false,
    aiDifficulty: null,
    aiThinking: false,
  }
}

function renderGameList() {
  return render(
    <BrowserRouter>
      <GameList />
    </BrowserRouter>
  )
}

beforeEach(() => {
  mockFetchGames.mockReset()
  mockFetchGames.mockResolvedValue(undefined)
  mockGames = []
  mockLoading = false
  mockError = null
  mockCurrentPage = 0
  mockTotalPages = 1
  mockTotalElements = 0
})

describe('GameList', () => {
  it('calls fetchGames on mount', () => {
    renderGameList()
    expect(mockFetchGames).toHaveBeenCalledTimes(1)
  })

  it('shows loading indicator when loading and no games', () => {
    mockLoading = true
    renderGameList()
    expect(screen.getByText('読み込み中...')).toBeTruthy()
  })

  it('shows error message when error', () => {
    mockError = 'Connection failed'
    renderGameList()
    expect(screen.getByText(/Connection failed/)).toBeTruthy()
  })

  it('shows empty state when no games', () => {
    renderGameList()
    expect(screen.getByText('対局が見つかりません')).toBeTruthy()
  })

  it('renders game cards when games exist', () => {
    mockGames = [makeGame('game-1'), makeGame('game-2')]
    mockTotalElements = 2
    renderGameList()

    expect(screen.getByTestId('game-card-game-1')).toBeTruthy()
    expect(screen.getByTestId('game-card-game-2')).toBeTruthy()
  })

  it('shows total elements count', () => {
    mockGames = [makeGame('game-1')]
    mockTotalElements = 1
    renderGameList()
    expect(screen.getByText(/1 件/)).toBeTruthy()
  })

  it('renders pagination when games exist', () => {
    mockGames = [makeGame('game-1')]
    mockTotalElements = 5
    mockTotalPages = 2
    renderGameList()
    expect(screen.getByTestId('pagination')).toBeTruthy()
  })

  it('renders status filter', () => {
    renderGameList()
    expect(screen.getByTestId('status-filter')).toBeTruthy()
  })

  it('renders 新規対局 link', () => {
    renderGameList()
    const link = screen.getByText('+ 新規対局')
    expect(link).toBeTruthy()
    expect(link.getAttribute('href')).toBe('/create')
  })

  it('shows すべてクリア button', () => {
    renderGameList()
    expect(screen.getByText('すべてクリア')).toBeTruthy()
  })

  it('disables すべてクリア button while clearing', async () => {
    // Mock global.confirm and fetch
    const originalConfirm = global.confirm
    const originalFetch = global.fetch
    global.confirm = () => true
    global.fetch = vi.fn().mockResolvedValue({ ok: true })
    const originalReload = window.location.reload
    Object.defineProperty(window, 'location', {
      writable: true,
      value: { ...window.location, reload: vi.fn() },
    })
    global.alert = vi.fn()

    renderGameList()
    const clearBtn = screen.getByText('すべてクリア')
    fireEvent.click(clearBtn)

    // Button should show クリア中... during the async operation
    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/admin/clear-database',
        { method: 'POST' }
      )
    })

    // Restore
    global.confirm = originalConfirm
    global.fetch = originalFetch
    window.location.reload = originalReload
  })

  it('shows 対局一覧 heading', () => {
    renderGameList()
    expect(screen.getByText('対局一覧')).toBeTruthy()
  })

  it('renders clearError button when error shown', () => {
    mockError = 'Some error'
    renderGameList()
    const closeBtn = screen.getByText('閉じる')
    fireEvent.click(closeBtn)
    expect(mockClearError).toHaveBeenCalled()
  })
})
