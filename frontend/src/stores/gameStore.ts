import { create } from 'zustand'
import type { GameQueryDto, GameStatistics } from '../types/api'
import { gameService } from '../services/gameService'

interface GameState {
  games: GameQueryDto[]
  currentGame: GameQueryDto | null
  statistics: GameStatistics | null
  loading: boolean
  error: string | null
  currentPage: number
  totalPages: number
  totalElements: number
  pageSize: number
  statusFilter: string | null

  fetchGames: (page?: number, size?: number) => Promise<void>
  fetchGame: (gameId: string) => Promise<void>
  fetchGamesByStatus: (status: string, page?: number, size?: number) => Promise<void>
  fetchStatistics: () => Promise<void>
  setPage: (page: number) => void
  setPageSize: (size: number) => void
  setStatusFilter: (status: string | null) => void
  clearError: () => void
  reset: () => void
}

const initialState = {
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
}

export const useGameStore = create<GameState>((set, get) => ({
  ...initialState,

  fetchGames: async (page = 0, size = 20) => {
    set({ loading: true, error: null })
    try {
      const response = await gameService.getGames(page, size)
      set({
        games: response.content,
        currentPage: response.number,
        totalPages: response.totalPages,
        totalElements: response.totalElements,
        pageSize: response.size,
        loading: false,
      })
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to fetch games'
      set({ error: message, loading: false })
    }
  },

  fetchGame: async (gameId: string) => {
    set({ loading: true, error: null })
    try {
      const game = await gameService.getGame(gameId)
      set({ currentGame: game, loading: false })
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to fetch game'
      set({ error: message, loading: false })
    }
  },

  fetchGamesByStatus: async (status: string, page = 0, size = 20) => {
    set({ loading: true, error: null, statusFilter: status })
    try {
      const response = await gameService.getGamesByStatus(status, page, size)
      set({
        games: response.content,
        currentPage: response.number,
        totalPages: response.totalPages,
        totalElements: response.totalElements,
        pageSize: response.size,
        loading: false,
      })
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to fetch games by status'
      set({ error: message, loading: false })
    }
  },

  fetchStatistics: async () => {
    set({ loading: true, error: null })
    try {
      const statistics = await gameService.getStatistics()
      set({ statistics, loading: false })
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Failed to fetch statistics'
      set({ error: message, loading: false })
    }
  },

  setPage: (page: number) => {
    const { statusFilter, pageSize, fetchGames, fetchGamesByStatus } = get()
    set({ currentPage: page })
    if (statusFilter) {
      fetchGamesByStatus(statusFilter, page, pageSize)
    } else {
      fetchGames(page, pageSize)
    }
  },

  setPageSize: (size: number) => {
    set({ pageSize: size, currentPage: 0 })
    const { statusFilter, fetchGames, fetchGamesByStatus } = get()
    if (statusFilter) {
      fetchGamesByStatus(statusFilter, 0, size)
    } else {
      fetchGames(0, size)
    }
  },

  setStatusFilter: (status: string | null) => {
    const { pageSize, fetchGames, fetchGamesByStatus } = get()
    set({ statusFilter: status, currentPage: 0 })
    if (status) {
      fetchGamesByStatus(status, 0, pageSize)
    } else {
      fetchGames(0, pageSize)
    }
  },

  clearError: () => set({ error: null }),

  reset: () => set(initialState),
}))
