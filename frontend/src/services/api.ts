import axios from 'axios'
import type {
  GameQueryDto,
  PageResponse,
  GameStatistics,
  PlayerStatistics,
  MoveHistoryDto,
  BoardStateDto,
} from '../types/api'

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
})

axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const message = error.response.data?.message || error.message
      return Promise.reject(new Error(message))
    }
    return Promise.reject(error)
  }
)

interface CreateGameRequest {
  blackPlayerId: string
  whitePlayerId: string
}

interface GameResponse {
  gameId: string
  status: string
  message: string
  nextTurn?: string
}

export type AiDifficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'
export type PlayerColor = 'BLACK' | 'WHITE'

export interface CreateAiGameRequest {
  humanPlayerId: string
  humanColor: PlayerColor
  difficulty: AiDifficulty
}

export interface CreateAiGameResponse {
  gameId: string
  status: string
  message: string
  aiPlayerId: string
  humanPlayerId: string
  humanColor: PlayerColor
  aiColor: PlayerColor
  difficulty: AiDifficulty
  aiFirstMove?: string
}

export interface AiMoveRequest {
  aiColor: PlayerColor
  difficulty: AiDifficulty
}

export interface AiMoveResponse {
  success: boolean
  gameId: string
  moveDescription?: string
  isDrop?: boolean
  aiColor?: PlayerColor
  difficulty?: AiDifficulty
  message?: string
}

export interface AiDifficultyOption {
  value: AiDifficulty
  label: string
}

export const AI_PLAYER_ID = '00000000-0000-0000-0000-000000000001'

export const api = {
  // Game Commands
  createGame: async (request: CreateGameRequest): Promise<GameResponse> => {
    const response = await axiosInstance.post<GameResponse>('/games', request)
    return response.data
  },

  makeMove: async (
    gameId: string,
    move: {
      player: string
      fromRow: number
      fromColumn: number
      toRow: number
      toColumn: number
      pieceType: string
      promote: boolean
    }
  ): Promise<GameResponse> => {
    const response = await axiosInstance.post<GameResponse>(
      `/games/${gameId}/moves`,
      move
    )
    return response.data
  },

  dropPiece: async (
    gameId: string,
    drop: {
      player: string
      toRow: number
      toColumn: number
      pieceType: string
    }
  ): Promise<GameResponse> => {
    const response = await axiosInstance.post<GameResponse>(
      `/games/${gameId}/drops`,
      drop
    )
    return response.data
  },

  resignGame: async (
    gameId: string,
    player: string
  ): Promise<GameResponse> => {
    const response = await axiosInstance.post<GameResponse>(
      `/games/${gameId}/resign`,
      { player }
    )
    return response.data
  },

  // Game Queries
  getGames: async (
    page: number = 0,
    size: number = 20
  ): Promise<PageResponse<GameQueryDto>> => {
    const response = await axiosInstance.get<PageResponse<GameQueryDto>>(
      '/queries/games',
      {
        params: { page, size },
      }
    )
    return response.data
  },

  getGamesByStatus: async (
    status: string,
    page: number = 0,
    size: number = 20
  ): Promise<PageResponse<GameQueryDto>> => {
    const response = await axiosInstance.get<PageResponse<GameQueryDto>>(
      `/queries/games/status/${status}`,
      {
        params: { page, size },
      }
    )
    return response.data
  },

  getGamesByPlayer: async (
    playerId: string,
    page: number = 0,
    size: number = 20
  ): Promise<PageResponse<GameQueryDto>> => {
    const response = await axiosInstance.get<PageResponse<GameQueryDto>>(
      `/queries/games/player/${playerId}`,
      {
        params: { page, size },
      }
    )
    return response.data
  },

  getGameById: async (gameId: string): Promise<GameQueryDto> => {
    const response = await axiosInstance.get<GameQueryDto>(
      `/queries/games/${gameId}`
    )
    return response.data
  },

  getGameStatistics: async (): Promise<GameStatistics> => {
    const response =
      await axiosInstance.get<GameStatistics>('/queries/statistics')
    return response.data
  },

  getPlayerStatistics: async (playerId: string): Promise<PlayerStatistics> => {
    const response = await axiosInstance.get<PlayerStatistics>(
      `/queries/statistics/player/${playerId}`
    )
    return response.data
  },

  getMoveHistory: async (gameId: string): Promise<MoveHistoryDto[]> => {
    const response = await axiosInstance.get<MoveHistoryDto[]>(
      `/queries/games/${gameId}/moves`
    )
    return response.data
  },

  // Replay
  getBoardStateAtMove: async (
    gameId: string,
    moveNumber: number
  ): Promise<BoardStateDto> => {
    const response = await axiosInstance.get<BoardStateDto>(
      `/queries/games/${gameId}/replay/state/${moveNumber}`
    )
    return response.data
  },

  getMovesForReplay: async (gameId: string): Promise<MoveHistoryDto[]> => {
    const response = await axiosInstance.get<MoveHistoryDto[]>(
      `/queries/games/${gameId}/replay/moves`
    )
    return response.data
  },

  exportKIF: async (gameId: string): Promise<string> => {
    const response = await axiosInstance.get<string>(
      `/queries/games/${gameId}/replay/kif`
    )
    return response.data
  },

  // AI Game Commands
  createAiGame: async (request: CreateAiGameRequest): Promise<CreateAiGameResponse> => {
    const response = await axiosInstance.post<CreateAiGameResponse>('/ai/games', request)
    return response.data
  },

  makeAiMove: async (gameId: string, request: AiMoveRequest): Promise<AiMoveResponse> => {
    const response = await axiosInstance.post<AiMoveResponse>(`/ai/games/${gameId}/ai-move`, request)
    return response.data
  },

  getAiDifficulties: async (): Promise<AiDifficultyOption[]> => {
    const response = await axiosInstance.get<AiDifficultyOption[]>('/ai/difficulties')
    return response.data
  },
}

export default axiosInstance
