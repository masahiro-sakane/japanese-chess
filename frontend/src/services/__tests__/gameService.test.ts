import { describe, it, expect, vi, beforeEach } from 'vitest'

const { mockGet, mockPost } = vi.hoisted(() => ({
  mockGet: vi.fn(),
  mockPost: vi.fn(),
}))

vi.mock('../api', () => ({
  default: {
    get: mockGet,
    post: mockPost,
  },
}))

import { gameService } from '../gameService'

beforeEach(() => {
  mockGet.mockReset()
  mockPost.mockReset()
})

describe('gameService.getGames', () => {
  it('calls GET /queries/games and returns response data', async () => {
    const data = { content: [], totalPages: 0, totalElements: 0, size: 20, number: 0 }
    mockGet.mockResolvedValue({ data })

    const result = await gameService.getGames()

    expect(mockGet).toHaveBeenCalledWith('/queries/games', { params: { page: 0, size: 20 } })
    expect(result).toEqual(data)
  })

  it('passes custom page and size', async () => {
    mockGet.mockResolvedValue({ data: { content: [] } })
    await gameService.getGames(2, 10)
    expect(mockGet).toHaveBeenCalledWith('/queries/games', { params: { page: 2, size: 10 } })
  })
})

describe('gameService.getGame', () => {
  it('calls GET /queries/games/:id', async () => {
    const game = { gameId: 'g1' }
    mockGet.mockResolvedValue({ data: game })

    const result = await gameService.getGame('g1')

    expect(mockGet).toHaveBeenCalledWith('/queries/games/g1')
    expect(result).toEqual(game)
  })
})

describe('gameService.getGamesByStatus', () => {
  it('calls GET with status in path', async () => {
    mockGet.mockResolvedValue({ data: { content: [] } })
    await gameService.getGamesByStatus('FINISHED')
    expect(mockGet).toHaveBeenCalledWith('/queries/games/status/FINISHED', { params: { page: 0, size: 20 } })
  })
})

describe('gameService.getGamesByPlayer', () => {
  it('calls GET /queries/games/player/:id', async () => {
    mockGet.mockResolvedValue({ data: { content: [] } })
    await gameService.getGamesByPlayer('player-1')
    expect(mockGet).toHaveBeenCalledWith('/queries/games/player/player-1', { params: { page: 0, size: 20 } })
  })
})

describe('gameService.getStatistics', () => {
  it('calls GET /queries/statistics', async () => {
    const stats = { totalGames: 10, activeGames: 3, completedGames: 7 }
    mockGet.mockResolvedValue({ data: stats })

    const result = await gameService.getStatistics()

    expect(mockGet).toHaveBeenCalledWith('/queries/statistics')
    expect(result).toEqual(stats)
  })
})

describe('gameService.getPlayerStatistics', () => {
  it('calls GET /queries/statistics/player/:id', async () => {
    const stats = { playerId: 'p1', totalGames: 5, activeGames: 0, wins: 3, losses: 2 }
    mockGet.mockResolvedValue({ data: stats })

    const result = await gameService.getPlayerStatistics('p1')

    expect(mockGet).toHaveBeenCalledWith('/queries/statistics/player/p1')
    expect(result).toEqual(stats)
  })
})

describe('gameService.getDailyGameCounts', () => {
  it('calls GET /queries/statistics/daily', async () => {
    const data = [{ date: '2026-01-01', count: 3 }]
    mockGet.mockResolvedValue({ data })

    const result = await gameService.getDailyGameCounts()

    expect(mockGet).toHaveBeenCalledWith('/queries/statistics/daily')
    expect(result).toEqual(data)
  })
})

describe('gameService.getPlayerRankings', () => {
  it('calls GET /queries/statistics/rankings', async () => {
    const data = [{ rank: 1, playerId: 'p1', totalGames: 10, wins: 7, losses: 3, winRate: 70 }]
    mockGet.mockResolvedValue({ data })

    const result = await gameService.getPlayerRankings()

    expect(mockGet).toHaveBeenCalledWith('/queries/statistics/rankings')
    expect(result).toEqual(data)
  })
})

describe('gameService.getMoveHistory', () => {
  it('calls GET /queries/games/:id/moves', async () => {
    mockGet.mockResolvedValue({ data: [] })
    await gameService.getMoveHistory('g1')
    expect(mockGet).toHaveBeenCalledWith('/queries/games/g1/moves')
  })
})

describe('gameService.createGame', () => {
  it('calls POST /games', async () => {
    const data = { gameId: 'g1', status: 'ok', message: 'created' }
    mockPost.mockResolvedValue({ data })

    const request = { blackPlayerId: 'bp', whitePlayerId: 'wp' }
    const result = await gameService.createGame(request)

    expect(mockPost).toHaveBeenCalledWith('/games', request)
    expect(result).toEqual(data)
  })
})

describe('gameService.movePiece', () => {
  it('calls POST /games/:id/moves', async () => {
    mockPost.mockResolvedValue({ data: { gameId: 'g1', status: 'ok', message: 'moved' } })

    const request = { gameId: 'g1', playerId: 'bp', fromRow: 6, fromColumn: 4, toRow: 5, toColumn: 4, promote: false }
    await gameService.movePiece('g1', request)

    expect(mockPost).toHaveBeenCalledWith('/games/g1/moves', request)
  })
})

describe('gameService.dropPiece', () => {
  it('calls POST /games/:id/drops', async () => {
    mockPost.mockResolvedValue({ data: { gameId: 'g1', status: 'ok', message: 'dropped' } })

    const request = { gameId: 'g1', playerId: 'bp', pieceType: 'PAWN', toRow: 4, toColumn: 4 }
    await gameService.dropPiece('g1', request)

    expect(mockPost).toHaveBeenCalledWith('/games/g1/drops', request)
  })
})

describe('gameService.resignGame', () => {
  it('calls POST /games/:id/resign', async () => {
    mockPost.mockResolvedValue({ data: { gameId: 'g1', status: 'ok', message: 'resigned' } })

    const request = { gameId: 'g1', playerId: 'bp' }
    await gameService.resignGame('g1', request)

    expect(mockPost).toHaveBeenCalledWith('/games/g1/resign', request)
  })
})

describe('gameService.getBoardStateAtMove', () => {
  it('calls GET /queries/games/:id/replay/state/:moveNumber', async () => {
    const boardState = { pieces: [], blackCapturedPieces: [], whiteCapturedPieces: [], moveNumber: 5, currentPlayer: 'BLACK' }
    mockGet.mockResolvedValue({ data: boardState })

    const result = await gameService.getBoardStateAtMove('g1', 5)

    expect(mockGet).toHaveBeenCalledWith('/queries/games/g1/replay/state/5')
    expect(result).toEqual(boardState)
  })
})

describe('gameService.getMovesForReplay', () => {
  it('calls GET /queries/games/:id/replay/moves', async () => {
    mockGet.mockResolvedValue({ data: [] })
    await gameService.getMovesForReplay('g1')
    expect(mockGet).toHaveBeenCalledWith('/queries/games/g1/replay/moves')
  })
})

describe('gameService.exportKIF', () => {
  it('calls GET /queries/games/:id/replay/kif', async () => {
    mockGet.mockResolvedValue({ data: '# KIF' })
    const result = await gameService.exportKIF('g1')
    expect(mockGet).toHaveBeenCalledWith('/queries/games/g1/replay/kif')
    expect(result).toBe('# KIF')
  })
})
