// @vitest-environment jsdom
import { describe, it, expect, vi, afterEach } from 'vitest'
import { render, screen, fireEvent, waitFor, cleanup } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { GameDetailPage } from '../GameDetailPage'
import type { GameQueryDto } from '../../types/api'

afterEach(cleanup)

const mockMakeMove = vi.fn()
const mockFetchGame = vi.fn()

// Mock the API service
vi.mock('../../services/api', () => ({
  api: {
    makeMove: (...args: unknown[]) => mockMakeMove(...args),
    dropPiece: vi.fn().mockResolvedValue({ gameId: 'test', status: 'ok', message: 'ok' }),
    resignGame: vi.fn().mockResolvedValue({ gameId: 'test', status: 'ok', message: 'ok' }),
    getMoveHistory: vi.fn().mockResolvedValue([]),
  },
}))

// Mock useGameWebSocket
vi.mock('../../hooks/useGameWebSocket', () => ({
  useGameWebSocket: () => ({ connected: false }),
}))

// Mock react-router-dom params
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useParams: () => ({ gameId: 'test-game-id' }),
  }
})

type CurrentGameState = GameQueryDto | null

let currentGameState: CurrentGameState = null

// Mock useGameStore with mutable state
vi.mock('../../stores/gameStore', () => ({
  useGameStore: Object.assign(
    () => ({
      get currentGame() { return currentGameState },
      loading: false,
      error: null,
      fetchGame: mockFetchGame,
      clearError: vi.fn(),
    }),
    {
      setState: (update: { currentGame?: GameQueryDto }) => {
        if (update.currentGame !== undefined) {
          currentGameState = update.currentGame
        }
      },
    }
  ),
}))

function makeGame(overrides: Partial<GameQueryDto> = {}): GameQueryDto {
  return {
    gameId: 'test-game-id',
    blackPlayerId: 'black-player',
    whitePlayerId: 'white-player',
    status: 'IN_PROGRESS',
    currentTurn: 'BLACK',
    winner: null,
    endReason: null,
    boardState: [],
    blackCapturedPieces: [],
    whiteCapturedPieces: [],
    moveCount: 1,
    createdAt: '2026-01-01T00:00:00',
    updatedAt: '2026-01-01T00:00:00',
    blackInCheck: false,
    whiteInCheck: false,
    aiGame: false,
    aiDifficulty: null,
    aiThinking: false,
    ...overrides,
  }
}

function renderGame(game: GameQueryDto | null = null) {
  currentGameState = game
  mockMakeMove.mockResolvedValue({ gameId: 'test-game-id', status: 'ok', message: 'ok' })
  mockFetchGame.mockResolvedValue(undefined)
  return render(
    <BrowserRouter>
      <GameDetailPage />
    </BrowserRouter>
  )
}

describe('Promotion Feature', () => {
  it('should render game page without loading indicator when game is loaded', async () => {
    renderGame(null)
    await waitFor(() => {
      expect(screen.queryByText(/読み込み中/)).toBeNull()
    })
  })

  it('should show promotion dialog when moving BLACK pawn to promotion zone', async () => {
    const game = makeGame({
      boardState: [
        { row: 3, column: 4, type: 'PAWN', owner: 'BLACK', promoted: false },
      ],
    })
    const { container } = renderGame(game)

    const cells = container.querySelectorAll('.board-cell')
    // Select BLACK pawn at row 3, col 4
    fireEvent.click(cells[3 * 9 + 4])
    // Move to row 1 (promotion zone for BLACK: rows 0-2)
    fireEvent.click(cells[1 * 9 + 4])

    await waitFor(() => {
      expect(screen.queryByText('駒を成りますか？')).toBeTruthy()
    })
  })

  it('should allow choosing to promote', async () => {
    const game = makeGame({
      boardState: [
        { row: 3, column: 4, type: 'PAWN', owner: 'BLACK', promoted: false },
      ],
    })
    const { container } = renderGame(game)

    const cells = container.querySelectorAll('.board-cell')
    fireEvent.click(cells[3 * 9 + 4])
    fireEvent.click(cells[1 * 9 + 4])

    await waitFor(() => {
      expect(screen.queryByText('駒を成りますか？')).toBeTruthy()
    })

    fireEvent.click(screen.getByText('成る'))

    await waitFor(() => {
      expect(mockMakeMove).toHaveBeenCalledWith(
        'test-game-id',
        expect.objectContaining({ promote: true })
      )
    })
  })

  it('should allow choosing not to promote', async () => {
    const game = makeGame({
      boardState: [
        { row: 3, column: 4, type: 'PAWN', owner: 'BLACK', promoted: false },
      ],
    })
    const { container } = renderGame(game)

    const cells = container.querySelectorAll('.board-cell')
    fireEvent.click(cells[3 * 9 + 4])
    fireEvent.click(cells[1 * 9 + 4])

    await waitFor(() => {
      expect(screen.queryByText('駒を成りますか？')).toBeTruthy()
    })

    fireEvent.click(screen.getByText('成らない'))

    await waitFor(() => {
      expect(mockMakeMove).toHaveBeenCalledWith(
        'test-game-id',
        expect.objectContaining({ promote: false })
      )
    })
  })

  it('should auto-promote when must promote (PAWN to row 0)', async () => {
    const game = makeGame({
      boardState: [
        { row: 1, column: 4, type: 'PAWN', owner: 'BLACK', promoted: false },
      ],
    })
    const { container } = renderGame(game)

    const cells = container.querySelectorAll('.board-cell')
    // Select pawn at row 1
    fireEvent.click(cells[1 * 9 + 4])
    // Move to row 0 (must promote)
    fireEvent.click(cells[0 * 9 + 4])

    // No dialog - auto-promote
    await waitFor(() => {
      expect(screen.queryByText('駒を成りますか？')).toBeNull()
      expect(mockMakeMove).toHaveBeenCalledWith(
        'test-game-id',
        expect.objectContaining({ promote: true })
      )
    })
  })

  it('should highlight promotion zone when BLACK piece selected', async () => {
    const game = makeGame({
      boardState: [
        { row: 5, column: 4, type: 'PAWN', owner: 'BLACK', promoted: false },
      ],
    })
    const { container } = renderGame(game)

    const cells = container.querySelectorAll('.board-cell')
    fireEvent.click(cells[5 * 9 + 4])

    // Rows 0-2 should be highlighted as promotion zone (27 cells)
    const promotionZoneCells = container.querySelectorAll('.promotion-zone')
    expect(promotionZoneCells.length).toBeGreaterThan(0)
  })

  it('should not show promotion dialog for non-promotable pieces (GOLD)', async () => {
    const game = makeGame({
      boardState: [
        { row: 5, column: 4, type: 'GOLD', owner: 'BLACK', promoted: false },
      ],
    })
    const { container } = renderGame(game)

    const cells = container.querySelectorAll('.board-cell')
    // Select GOLD at row 5
    fireEvent.click(cells[5 * 9 + 4])
    // Move to row 1 (promotion zone, but GOLD cannot promote)
    fireEvent.click(cells[1 * 9 + 4])

    await waitFor(() => {
      expect(screen.queryByText('駒を成りますか？')).toBeNull()
      expect(mockMakeMove).toHaveBeenCalledWith(
        'test-game-id',
        expect.objectContaining({ promote: false })
      )
    })
  })
})
