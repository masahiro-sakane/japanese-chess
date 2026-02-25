// @vitest-environment jsdom
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useGameStore } from '../gameStore'
import type { GameQueryDto, GameStatistics } from '../../types/api'

// Mock gameService
vi.mock('../../services/gameService', () => ({
  gameService: {
    getGames: vi.fn(),
    getGame: vi.fn(),
    getGamesByStatus: vi.fn(),
    getStatistics: vi.fn(),
  },
}))

import { gameService } from '../../services/gameService'

const mockService = vi.mocked(gameService)

function makePageResponse(games: GameQueryDto[] = []) {
  return {
    content: games,
    number: 0,
    totalPages: 1,
    totalElements: games.length,
    size: 20,
    pageable: { pageNumber: 0, pageSize: 20, offset: 0, paged: true, unpaged: false, sort: { sorted: false, unsorted: true, empty: true } },
    last: true,
    sort: { sorted: false, unsorted: true, empty: true },
    numberOfElements: games.length,
    first: true,
    empty: games.length === 0,
  }
}

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

const makeStatistics = (): GameStatistics => ({
  totalGames: 10,
  activeGames: 3,
  completedGames: 7,
})

beforeEach(() => {
  vi.clearAllMocks()
  useGameStore.setState({
    games: [],
    currentGame: null,
    statistics: null,
    loading: false,
    error: null,
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    pageSize: 20,
    statusFilter: null,
  })
})

describe('useGameStore.fetchGames', () => {
  it('sets loading then populates games on success', async () => {
    const games = [makeGame('g1'), makeGame('g2')]
    mockService.getGames.mockResolvedValue(makePageResponse(games))

    const store = useGameStore.getState()
    await store.fetchGames()

    const state = useGameStore.getState()
    expect(state.loading).toBe(false)
    expect(state.games).toHaveLength(2)
    expect(state.totalElements).toBe(2)
    expect(state.error).toBeNull()
  })

  it('sets error on failure', async () => {
    mockService.getGames.mockRejectedValue(new Error('Network error'))

    const store = useGameStore.getState()
    await store.fetchGames()

    const state = useGameStore.getState()
    expect(state.loading).toBe(false)
    expect(state.error).toBe('Network error')
    expect(state.games).toHaveLength(0)
  })

  it('uses custom page and size', async () => {
    mockService.getGames.mockResolvedValue({
      ...makePageResponse([]),
      number: 2,
      totalPages: 5,
      size: 10,
    })

    const store = useGameStore.getState()
    await store.fetchGames(2, 10)

    expect(mockService.getGames).toHaveBeenCalledWith(2, 10)
  })
})

describe('useGameStore.fetchGame', () => {
  it('sets currentGame on success', async () => {
    const game = makeGame('g1')
    mockService.getGame.mockResolvedValue(game)

    const store = useGameStore.getState()
    await store.fetchGame('g1')

    const state = useGameStore.getState()
    expect(state.currentGame).toEqual(game)
    expect(state.loading).toBe(false)
    expect(state.error).toBeNull()
  })

  it('sets error when game not found', async () => {
    mockService.getGame.mockRejectedValue(new Error('Game not found'))

    const store = useGameStore.getState()
    await store.fetchGame('not-exist')

    const state = useGameStore.getState()
    expect(state.currentGame).toBeNull()
    expect(state.error).toBe('Game not found')
  })
})

describe('useGameStore.fetchGamesByStatus', () => {
  it('fetches games with status filter', async () => {
    const games = [makeGame('g1')]
    mockService.getGamesByStatus.mockResolvedValue(makePageResponse(games))

    const store = useGameStore.getState()
    await store.fetchGamesByStatus('IN_PROGRESS')

    const state = useGameStore.getState()
    expect(state.games).toHaveLength(1)
    expect(state.statusFilter).toBe('IN_PROGRESS')
    expect(mockService.getGamesByStatus).toHaveBeenCalledWith('IN_PROGRESS', 0, 20)
  })

  it('sets error on failure', async () => {
    mockService.getGamesByStatus.mockRejectedValue(new Error('Server error'))

    const store = useGameStore.getState()
    await store.fetchGamesByStatus('FINISHED')

    expect(useGameStore.getState().error).toBe('Server error')
  })
})

describe('useGameStore.fetchStatistics', () => {
  it('populates statistics on success', async () => {
    const stats = makeStatistics()
    mockService.getStatistics.mockResolvedValue(stats)

    const store = useGameStore.getState()
    await store.fetchStatistics()

    const state = useGameStore.getState()
    expect(state.statistics).toEqual(stats)
    expect(state.loading).toBe(false)
  })

  it('sets error on failure', async () => {
    mockService.getStatistics.mockRejectedValue(new Error('Stats unavailable'))

    const store = useGameStore.getState()
    await store.fetchStatistics()

    expect(useGameStore.getState().error).toBe('Stats unavailable')
  })
})

describe('useGameStore.setPage', () => {
  it('calls fetchGames when no status filter', async () => {
    mockService.getGames.mockResolvedValue(makePageResponse([]))

    const store = useGameStore.getState()
    store.setPage(2)

    expect(mockService.getGames).toHaveBeenCalledWith(2, 20)
  })

  it('calls fetchGamesByStatus when status filter set', async () => {
    useGameStore.setState({ statusFilter: 'IN_PROGRESS' })
    mockService.getGamesByStatus.mockResolvedValue(makePageResponse([]))

    const store = useGameStore.getState()
    store.setPage(1)

    expect(mockService.getGamesByStatus).toHaveBeenCalledWith('IN_PROGRESS', 1, 20)
  })
})

describe('useGameStore.setPageSize', () => {
  it('resets to page 0 and fetches with new size', async () => {
    mockService.getGames.mockResolvedValue(makePageResponse([]))
    useGameStore.setState({ currentPage: 3 })

    const store = useGameStore.getState()
    store.setPageSize(10)

    expect(mockService.getGames).toHaveBeenCalledWith(0, 10)
    expect(useGameStore.getState().currentPage).toBe(0)
  })

  it('calls fetchGamesByStatus when status filter is active', async () => {
    useGameStore.setState({ statusFilter: 'FINISHED' })
    mockService.getGamesByStatus.mockResolvedValue(makePageResponse([]))

    const store = useGameStore.getState()
    store.setPageSize(50)

    expect(mockService.getGamesByStatus).toHaveBeenCalledWith('FINISHED', 0, 50)
  })
})

describe('useGameStore.setStatusFilter', () => {
  it('sets filter and fetches by status', async () => {
    mockService.getGamesByStatus.mockResolvedValue(makePageResponse([]))

    const store = useGameStore.getState()
    store.setStatusFilter('IN_PROGRESS')

    const state = useGameStore.getState()
    expect(state.statusFilter).toBe('IN_PROGRESS')
    expect(state.currentPage).toBe(0)
    expect(mockService.getGamesByStatus).toHaveBeenCalledWith('IN_PROGRESS', 0, 20)
  })

  it('clears filter and fetches all games', async () => {
    useGameStore.setState({ statusFilter: 'IN_PROGRESS' })
    mockService.getGames.mockResolvedValue(makePageResponse([]))

    const store = useGameStore.getState()
    store.setStatusFilter(null)

    expect(useGameStore.getState().statusFilter).toBeNull()
    expect(mockService.getGames).toHaveBeenCalledWith(0, 20)
  })
})

describe('useGameStore.clearError', () => {
  it('clears the error', () => {
    useGameStore.setState({ error: 'some error' })
    useGameStore.getState().clearError()
    expect(useGameStore.getState().error).toBeNull()
  })
})

describe('useGameStore.reset', () => {
  it('resets all state to initial values', async () => {
    useGameStore.setState({
      games: [makeGame('g1')],
      currentGame: makeGame('g2'),
      statistics: makeStatistics(),
      loading: true,
      error: 'err',
      currentPage: 5,
      totalPages: 10,
      totalElements: 100,
      pageSize: 50,
      statusFilter: 'FINISHED',
    })

    useGameStore.getState().reset()

    const state = useGameStore.getState()
    expect(state.games).toHaveLength(0)
    expect(state.currentGame).toBeNull()
    expect(state.statistics).toBeNull()
    expect(state.loading).toBe(false)
    expect(state.error).toBeNull()
    expect(state.currentPage).toBe(0)
    expect(state.totalPages).toBe(0)
    expect(state.totalElements).toBe(0)
    expect(state.pageSize).toBe(20)
    expect(state.statusFilter).toBeNull()
  })
})
