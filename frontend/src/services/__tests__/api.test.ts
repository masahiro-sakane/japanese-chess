import { describe, it, expect, vi, beforeEach } from 'vitest'

const { mockGet, mockPost } = vi.hoisted(() => ({
  mockGet: vi.fn(),
  mockPost: vi.fn(),
}))

vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      get: mockGet,
      post: mockPost,
      interceptors: {
        response: { use: vi.fn() },
      },
    })),
  },
}))

import { api } from '../api'

beforeEach(() => {
  mockGet.mockReset()
  mockPost.mockReset()
})

describe('api.createGame', () => {
  it('posts to /games and returns response data', async () => {
    const data = { gameId: 'g1', status: 'ok', message: 'created' }
    mockPost.mockResolvedValue({ data })

    const result = await api.createGame({ blackPlayerId: 'b', whitePlayerId: 'w' })

    expect(mockPost).toHaveBeenCalledWith('/games', { blackPlayerId: 'b', whitePlayerId: 'w' })
    expect(result).toEqual(data)
  })
})

describe('api.makeMove', () => {
  it('posts to /games/:id/moves', async () => {
    const data = { gameId: 'g1', status: 'ok', message: 'moved' }
    mockPost.mockResolvedValue({ data })

    const move = {
      player: 'bp',
      fromRow: 6, fromColumn: 4,
      toRow: 5, toColumn: 4,
      pieceType: 'PAWN', promote: false,
    }
    const result = await api.makeMove('g1', move)

    expect(mockPost).toHaveBeenCalledWith('/games/g1/moves', move)
    expect(result).toEqual(data)
  })
})

describe('api.dropPiece', () => {
  it('posts to /games/:id/drops', async () => {
    const data = { gameId: 'g1', status: 'ok', message: 'dropped' }
    mockPost.mockResolvedValue({ data })

    const drop = { player: 'bp', toRow: 4, toColumn: 4, pieceType: 'PAWN' }
    const result = await api.dropPiece('g1', drop)

    expect(mockPost).toHaveBeenCalledWith('/games/g1/drops', drop)
    expect(result).toEqual(data)
  })
})

describe('api.resignGame', () => {
  it('posts to /games/:id/resign', async () => {
    const data = { gameId: 'g1', status: 'ok', message: 'resigned' }
    mockPost.mockResolvedValue({ data })

    const result = await api.resignGame('g1', 'bp')

    expect(mockPost).toHaveBeenCalledWith('/games/g1/resign', { player: 'bp' })
    expect(result).toEqual(data)
  })
})

describe('api.getGames', () => {
  it('gets with default page/size params', async () => {
    const data = { content: [], totalPages: 0, totalElements: 0, size: 20, number: 0 }
    mockGet.mockResolvedValue({ data })

    const result = await api.getGames()

    expect(mockGet).toHaveBeenCalledWith('/queries/games', { params: { page: 0, size: 20 } })
    expect(result).toEqual(data)
  })

  it('passes custom page and size', async () => {
    mockGet.mockResolvedValue({ data: {} })
    await api.getGames(1, 10)
    expect(mockGet).toHaveBeenCalledWith('/queries/games', { params: { page: 1, size: 10 } })
  })
})

describe('api.getGamesByStatus', () => {
  it('gets games filtered by status', async () => {
    mockGet.mockResolvedValue({ data: {} })
    await api.getGamesByStatus('IN_PROGRESS')
    expect(mockGet).toHaveBeenCalledWith('/queries/games/status/IN_PROGRESS', { params: { page: 0, size: 20 } })
  })
})

describe('api.getGamesByPlayer', () => {
  it('gets games for a specific player', async () => {
    mockGet.mockResolvedValue({ data: {} })
    await api.getGamesByPlayer('player-1')
    expect(mockGet).toHaveBeenCalledWith('/queries/games/player/player-1', { params: { page: 0, size: 20 } })
  })
})

describe('api.getGameById', () => {
  it('gets a single game by id', async () => {
    const game = { gameId: 'g1', status: 'IN_PROGRESS' }
    mockGet.mockResolvedValue({ data: game })

    const result = await api.getGameById('g1')

    expect(mockGet).toHaveBeenCalledWith('/queries/games/g1')
    expect(result).toEqual(game)
  })
})

describe('api.getGameStatistics', () => {
  it('gets overall statistics', async () => {
    const stats = { totalGames: 10, activeGames: 3, completedGames: 7 }
    mockGet.mockResolvedValue({ data: stats })

    const result = await api.getGameStatistics()

    expect(mockGet).toHaveBeenCalledWith('/queries/statistics')
    expect(result).toEqual(stats)
  })
})

describe('api.getPlayerStatistics', () => {
  it('gets statistics for a specific player', async () => {
    const stats = { playerId: 'p1', totalGames: 5, activeGames: 1, wins: 3, losses: 1 }
    mockGet.mockResolvedValue({ data: stats })

    const result = await api.getPlayerStatistics('p1')

    expect(mockGet).toHaveBeenCalledWith('/queries/statistics/player/p1')
    expect(result).toEqual(stats)
  })
})

describe('api.getMoveHistory', () => {
  it('gets move history for a game', async () => {
    const moves = [{ id: 1, moveNumber: 1 }]
    mockGet.mockResolvedValue({ data: moves })

    const result = await api.getMoveHistory('g1')

    expect(mockGet).toHaveBeenCalledWith('/queries/games/g1/moves')
    expect(result).toEqual(moves)
  })
})

describe('api.getBoardStateAtMove', () => {
  it('gets board state at a specific move number', async () => {
    const boardState = { pieces: [], moveNumber: 3 }
    mockGet.mockResolvedValue({ data: boardState })

    const result = await api.getBoardStateAtMove('g1', 3)

    expect(mockGet).toHaveBeenCalledWith('/queries/games/g1/replay/state/3')
    expect(result).toEqual(boardState)
  })
})

describe('api.getMovesForReplay', () => {
  it('gets moves for replay', async () => {
    mockGet.mockResolvedValue({ data: [] })
    await api.getMovesForReplay('g1')
    expect(mockGet).toHaveBeenCalledWith('/queries/games/g1/replay/moves')
  })
})

describe('api.exportKIF', () => {
  it('exports KIF format content', async () => {
    mockGet.mockResolvedValue({ data: '# KIF content' })

    const result = await api.exportKIF('g1')

    expect(mockGet).toHaveBeenCalledWith('/queries/games/g1/replay/kif')
    expect(result).toBe('# KIF content')
  })
})
