// @vitest-environment jsdom
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import type { GameQueryDto } from '../types/api'

interface MockStompClient {
  subscribe: ReturnType<typeof vi.fn>
  activate: ReturnType<typeof vi.fn>
  deactivate: ReturnType<typeof vi.fn>
  onConnect?: () => void
}

let mockClientInstance: MockStompClient | null = null

vi.mock('@stomp/stompjs', () => ({
  Client: vi.fn().mockImplementation((config: { onConnect?: () => void }) => {
    mockClientInstance = {
      subscribe: vi.fn(),
      activate: vi.fn(),
      deactivate: vi.fn(),
      onConnect: config.onConnect,
    }
    return mockClientInstance
  }),
}))

vi.mock('sockjs-client', () => ({
  default: vi.fn().mockImplementation(() => ({})),
}))

describe('useGameWebSocket', () => {
  const mockGame: GameQueryDto = {
    gameId: 'test-game-id',
    blackPlayerId: 'player1',
    whitePlayerId: 'player2',
    status: 'IN_PROGRESS',
    currentTurn: 'BLACK',
    winner: null,
    endReason: null,
    boardState: [],
    blackCapturedPieces: [],
    whiteCapturedPieces: [],
    moveCount: 5,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:01:00Z',
    blackInCheck: false,
    whiteInCheck: false,
    aiGame: false,
    aiDifficulty: null,
    aiThinking: false,
  }

  beforeEach(() => {
    mockClientInstance = null
    vi.clearAllMocks()
  })

  it('activates STOMP client on mount', async () => {
    const { useGameWebSocket } = await import('./useGameWebSocket')
    renderHook(() => useGameWebSocket('game-123', vi.fn()))
    expect(mockClientInstance?.activate).toHaveBeenCalledTimes(1)
  })

  it('deactivates STOMP client on unmount', async () => {
    const { useGameWebSocket } = await import('./useGameWebSocket')
    const { unmount } = renderHook(() => useGameWebSocket('game-123', vi.fn()))
    unmount()
    expect(mockClientInstance?.deactivate).toHaveBeenCalledTimes(1)
  })

  it('subscribes to correct topic on connect', async () => {
    const { useGameWebSocket } = await import('./useGameWebSocket')
    const gameId = 'game-abc-123'
    renderHook(() => useGameWebSocket(gameId, vi.fn()))

    act(() => {
      mockClientInstance?.onConnect?.()
    })

    expect(mockClientInstance?.subscribe).toHaveBeenCalledWith(
      `/topic/game/${gameId}`,
      expect.any(Function)
    )
  })

  it('calls onUpdate with parsed game data when message received', async () => {
    const { useGameWebSocket } = await import('./useGameWebSocket')
    const onUpdate = vi.fn()
    renderHook(() => useGameWebSocket('game-123', onUpdate))

    act(() => {
      mockClientInstance?.onConnect?.()
    })

    const messageHandler = mockClientInstance?.subscribe.mock.calls[0][1] as (msg: { body: string }) => void
    act(() => {
      messageHandler({ body: JSON.stringify(mockGame) })
    })

    expect(onUpdate).toHaveBeenCalledWith(mockGame)
  })

  it('does not activate client when gameId is undefined', async () => {
    const { useGameWebSocket } = await import('./useGameWebSocket')
    renderHook(() => useGameWebSocket(undefined, vi.fn()))
    expect(mockClientInstance).toBeNull()
  })
})
