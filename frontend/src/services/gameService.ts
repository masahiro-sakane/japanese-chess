import api from './api'
import type {
  GameQueryDto,
  PageResponse,
  GameStatistics,
  PlayerStatistics,
  MoveHistoryDto,
  BoardStateDto,
  GameResponse,
  CreateGameRequest,
  MovePieceRequest,
  DropPieceRequest,
  ResignGameRequest,
} from '../types/api'

export const gameService = {
  async getGames(page = 0, size = 20): Promise<PageResponse<GameQueryDto>> {
    const response = await api.get<PageResponse<GameQueryDto>>('/queries/games', {
      params: { page, size },
    })
    return response.data
  },

  async getGame(gameId: string): Promise<GameQueryDto> {
    const response = await api.get<GameQueryDto>(`/queries/games/${gameId}`)
    return response.data
  },

  async getGamesByStatus(
    status: string,
    page = 0,
    size = 20
  ): Promise<PageResponse<GameQueryDto>> {
    const response = await api.get<PageResponse<GameQueryDto>>(
      `/queries/games/status/${status}`,
      {
        params: { page, size },
      }
    )
    return response.data
  },

  async getGamesByPlayer(
    playerId: string,
    page = 0,
    size = 20
  ): Promise<PageResponse<GameQueryDto>> {
    const response = await api.get<PageResponse<GameQueryDto>>(
      `/queries/games/player/${playerId}`,
      {
        params: { page, size },
      }
    )
    return response.data
  },

  async getStatistics(): Promise<GameStatistics> {
    const response = await api.get<GameStatistics>('/queries/statistics')
    return response.data
  },

  async getPlayerStatistics(playerId: string): Promise<PlayerStatistics> {
    const response = await api.get<PlayerStatistics>(
      `/queries/statistics/player/${playerId}`
    )
    return response.data
  },

  async getMoveHistory(gameId: string): Promise<MoveHistoryDto[]> {
    const response = await api.get<MoveHistoryDto[]>(
      `/queries/games/${gameId}/moves`
    )
    return response.data
  },

  async createGame(request: CreateGameRequest): Promise<GameResponse> {
    const response = await api.post<GameResponse>('/games', request)
    return response.data
  },

  async movePiece(gameId: string, request: MovePieceRequest): Promise<GameResponse> {
    const response = await api.post<GameResponse>(`/games/${gameId}/moves`, request)
    return response.data
  },

  async dropPiece(gameId: string, request: DropPieceRequest): Promise<GameResponse> {
    const response = await api.post<GameResponse>(`/games/${gameId}/drops`, request)
    return response.data
  },

  async resignGame(gameId: string, request: ResignGameRequest): Promise<GameResponse> {
    const response = await api.post<GameResponse>(`/games/${gameId}/resign`, request)
    return response.data
  },

  async getBoardStateAtMove(
    gameId: string,
    moveNumber: number
  ): Promise<BoardStateDto> {
    const response = await api.get<BoardStateDto>(
      `/queries/games/${gameId}/replay/state/${moveNumber}`
    )
    return response.data
  },

  async getMovesForReplay(gameId: string): Promise<MoveHistoryDto[]> {
    const response = await api.get<MoveHistoryDto[]>(
      `/queries/games/${gameId}/replay/moves`
    )
    return response.data
  },

  async exportKIF(gameId: string): Promise<string> {
    const response = await api.get<string>(`/queries/games/${gameId}/replay/kif`)
    return response.data
  },
}
