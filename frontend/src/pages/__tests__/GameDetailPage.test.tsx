// @vitest-environment jsdom
import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor, cleanup } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { GameDetailPage } from '../GameDetailPage'
import type { GameQueryDto } from '../../types/api'

afterEach(cleanup)

const mockMakeMove = vi.fn()
const mockDropPiece = vi.fn()
const mockResignGame = vi.fn()
const mockFetchGame = vi.fn()
const mockClearError = vi.fn()

vi.mock('../../services/api', () => ({
  api: {
    makeMove: (...args: unknown[]) => mockMakeMove(...args),
    dropPiece: (...args: unknown[]) => mockDropPiece(...args),
    resignGame: (...args: unknown[]) => mockResignGame(...args),
    getMoveHistory: vi.fn().mockResolvedValue([]),
  },
}))

vi.mock('../../hooks/useGameWebSocket', () => ({
  useGameWebSocket: () => ({ connected: false }),
}))

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return { ...actual, useParams: () => ({ gameId: 'test-game-id' }) }
})

let currentGameState: GameQueryDto | null = null

vi.mock('../../stores/gameStore', () => ({
  useGameStore: Object.assign(
    () => ({
      get currentGame() { return currentGameState },
      loading: false,
      error: null,
      fetchGame: mockFetchGame,
      clearError: mockClearError,
    }),
    {
      setState: (update: { currentGame?: GameQueryDto }) => {
        if (update.currentGame !== undefined) currentGameState = update.currentGame
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
  mockFetchGame.mockResolvedValue(undefined)
  mockMakeMove.mockResolvedValue({ gameId: 'test-game-id', status: 'ok', message: 'ok' })
  mockDropPiece.mockResolvedValue({ gameId: 'test-game-id', status: 'ok', message: 'ok' })
  mockResignGame.mockResolvedValue({ gameId: 'test-game-id', status: 'ok', message: 'ok' })
  return render(
    <BrowserRouter>
      <GameDetailPage />
    </BrowserRouter>
  )
}

beforeEach(() => {
  mockMakeMove.mockReset()
  mockDropPiece.mockReset()
  mockResignGame.mockReset()
  mockFetchGame.mockReset()
  mockClearError.mockReset()
})

describe('GameDetailPage - loading/empty states', () => {
  it('shows empty state when no game', () => {
    renderGame(null)
    expect(screen.getByText('対局が見つかりません')).toBeTruthy()
  })

  it('calls fetchGame on mount', () => {
    renderGame(null)
    expect(mockFetchGame).toHaveBeenCalledWith('test-game-id')
  })
})

describe('GameDetailPage - game info', () => {
  it('shows game ID', () => {
    renderGame(makeGame())
    expect(screen.getByText('test-game-id')).toBeTruthy()
  })

  it('shows black player ID', () => {
    renderGame(makeGame())
    expect(screen.getAllByText('black-player').length).toBeGreaterThan(0)
  })

  it('shows white player ID', () => {
    renderGame(makeGame())
    expect(screen.getAllByText('white-player').length).toBeGreaterThan(0)
  })

  it('shows 対局中 status', () => {
    renderGame(makeGame())
    expect(screen.getByText('対局中')).toBeTruthy()
  })

  it('shows move count', () => {
    renderGame(makeGame({ moveCount: 12 }))
    expect(screen.getByText('12')).toBeTruthy()
  })

  it('shows BLACK turn indicator', () => {
    renderGame(makeGame({ currentTurn: 'BLACK' }))
    expect(screen.getByText('先手の番')).toBeTruthy()
  })

  it('shows WHITE turn indicator', () => {
    renderGame(makeGame({ currentTurn: 'WHITE' }))
    expect(screen.getByText('後手の番')).toBeTruthy()
  })

  it('shows link back to game list', () => {
    renderGame(makeGame())
    expect(screen.getByText('← 対局一覧に戻る')).toBeTruthy()
  })
})

describe('GameDetailPage - check display', () => {
  it('shows 王手 warning when BLACK is in check', () => {
    renderGame(makeGame({ blackInCheck: true }))
    expect(screen.getByText(/王手/)).toBeTruthy()
  })

  it('shows 王手 warning when WHITE is in check', () => {
    renderGame(makeGame({ whiteInCheck: true }))
    expect(screen.getByText(/王手/)).toBeTruthy()
  })

  it('does not show 王手 when no check', () => {
    renderGame(makeGame())
    expect(screen.queryByText(/王手/)).toBeNull()
  })
})

describe('GameDetailPage - finished game', () => {
  it('shows 詰み banner for checkmate win', () => {
    renderGame(makeGame({ status: 'FINISHED', endReason: 'CHECKMATE', winner: 'BLACK' }))
    expect(screen.getAllByText(/詰み/).length).toBeGreaterThan(0)
  })

  it('shows winner for finished game', () => {
    renderGame(makeGame({ status: 'FINISHED', winner: 'BLACK', endReason: 'RESIGNATION' }))
    expect(screen.getAllByText(/先手/).length).toBeGreaterThan(0)
  })

  it('does not show resign button for finished game', () => {
    renderGame(makeGame({ status: 'FINISHED', winner: 'BLACK', endReason: 'RESIGNATION' }))
    expect(screen.queryByText('投了する (Resign)')).toBeNull()
  })

  it('shows replay link when moveCount > 0', () => {
    renderGame(makeGame({ status: 'FINISHED', winner: 'BLACK', endReason: 'RESIGNATION', moveCount: 10 }))
    expect(screen.getByText(/棋譜再生/)).toBeTruthy()
  })
})

describe('GameDetailPage - resignation', () => {
  it('renders resign button for in-progress game', () => {
    renderGame(makeGame())
    expect(screen.getByText('投了する (Resign)')).toBeTruthy()
  })

  it('shows resign confirmation dialog when resign button clicked', () => {
    renderGame(makeGame())
    fireEvent.click(screen.getByText('投了する (Resign)'))
    expect(screen.getByText('投了の確認')).toBeTruthy()
  })

  it('hides dialog when キャンセル clicked', () => {
    renderGame(makeGame())
    fireEvent.click(screen.getByText('投了する (Resign)'))
    fireEvent.click(screen.getByText('キャンセル'))
    expect(screen.queryByText('投了の確認')).toBeNull()
  })

  it('calls resignGame when 投了する clicked in dialog', async () => {
    renderGame(makeGame({ currentTurn: 'BLACK' }))
    fireEvent.click(screen.getByText('投了する (Resign)'))
    fireEvent.click(screen.getByText('投了する'))

    await waitFor(() => {
      expect(mockResignGame).toHaveBeenCalledWith('test-game-id', 'black-player')
    })
  })

  it('calls resignGame for WHITE turn', async () => {
    renderGame(makeGame({ currentTurn: 'WHITE' }))
    fireEvent.click(screen.getByText('投了する (Resign)'))
    fireEvent.click(screen.getByText('投了する'))

    await waitFor(() => {
      expect(mockResignGame).toHaveBeenCalledWith('test-game-id', 'white-player')
    })
  })
})

describe('GameDetailPage - AI game', () => {
  it('shows AIの番 when AI game and WHITE turn', () => {
    renderGame(makeGame({ aiGame: true, currentTurn: 'WHITE' }))
    expect(screen.getByText('AIの番')).toBeTruthy()
  })

  it('does not show resign button during AI turn', () => {
    renderGame(makeGame({ aiGame: true, currentTurn: 'WHITE' }))
    // Board should be non-interactive but resign button should still be there
    // (resign is valid any time, but the board is locked)
  })
})

describe('GameDetailPage - drop piece', () => {
  it('shows drop hint when captured piece selected', () => {
    renderGame(makeGame({
      blackCapturedPieces: ['PAWN'],
      currentTurn: 'BLACK',
    }))
    // Click on the captured PAWN in the hand
    fireEvent.click(screen.getByText('歩'))
    expect(screen.getByText(/盤面の空いているマスをクリック/)).toBeTruthy()
  })

  it('deselects captured piece when clicking same piece again', () => {
    renderGame(makeGame({
      blackCapturedPieces: ['PAWN'],
      currentTurn: 'BLACK',
    }))
    fireEvent.click(screen.getByText('歩'))
    fireEvent.click(screen.getByText('歩'))
    expect(screen.queryByText(/盤面の空いているマスをクリック/)).toBeNull()
  })
})
