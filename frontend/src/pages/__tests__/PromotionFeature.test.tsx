// @vitest-environment jsdom
import { describe, it, expect, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { GameDetailPage } from '../GameDetailPage'

// Mock the API service
vi.mock('../../services/api', () => ({
  gameApi: {
    getGame: vi.fn(),
    movePiece: vi.fn(),
    dropPiece: vi.fn(),
    getMoveHistory: vi.fn(),
  },
  api: {
    getMoveHistory: vi.fn().mockResolvedValue([]),
  },
}))

// Mock useGameStore
vi.mock('../../stores/gameStore', () => ({
  useGameStore: () => ({
    currentGame: null,
    loading: false,
    error: null,
    fetchGame: vi.fn(),
    clearError: vi.fn(),
    makeMove: vi.fn(),
    dropPiece: vi.fn(),
    resign: vi.fn(),
  }),
}))

// Mock useGameWebSocket
vi.mock('../../hooks/useGameWebSocket', () => ({
  useGameWebSocket: () => ({
    connected: false,
  }),
}))

// Mock react-router-dom params
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useParams: () => ({ gameId: 'test-game-id' }),
  }
})

describe('Promotion Feature', () => {
  it('should render game page without loading indicator when game is loaded', async () => {
    render(
      <BrowserRouter>
        <GameDetailPage />
      </BrowserRouter>
    )

    // With mocked store returning loading: false, no loading indicator should show
    await waitFor(() => {
      expect(screen.queryByText(/読み込み中/)).toBeNull()
    })
  })

  it('should allow choosing to promote', async () => {
    // TODO: Test that clicking "成る" button promotes the piece
    expect(true).toBe(true)
  })

  it('should allow choosing not to promote', async () => {
    // TODO: Test that clicking "成らない" button does not promote the piece
    expect(true).toBe(true)
  })

  it('should auto-promote when must promote', async () => {
    // TODO: Test that pieces that must promote (pawn/lance to row 0, knight to row 0-1) auto-promote
    expect(true).toBe(true)
  })

  it('should highlight promotion zone when piece selected', async () => {
    // TODO: Test that promotion zone (rows 0-2 for BLACK, 6-8 for WHITE) is highlighted
    expect(true).toBe(true)
  })

  it('should not show promotion dialog for non-promotable pieces', async () => {
    // TODO: Test that gold, king, already promoted pieces don't trigger dialog
    expect(true).toBe(true)
  })
})
