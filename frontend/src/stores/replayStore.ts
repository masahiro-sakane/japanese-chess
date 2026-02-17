import { create } from 'zustand'
import type { MoveHistoryDto, BoardStateDto } from '../types/api'
import { gameService } from '../services/gameService'

interface ReplayState {
  // State
  gameId: string | null
  currentMoveNumber: number
  totalMoves: number
  isPlaying: boolean
  playbackSpeed: number
  moves: MoveHistoryDto[]
  boardState: BoardStateDto | null
  isLoading: boolean
  error: string | null

  // Actions
  loadReplay: (gameId: string) => Promise<void>
  setCurrentMove: (moveNumber: number) => Promise<void>
  nextMove: () => Promise<void>
  previousMove: () => Promise<void>
  firstMove: () => Promise<void>
  lastMove: () => Promise<void>
  togglePlayback: () => void
  setPlaybackSpeed: (speed: number) => void
  reset: () => void
}

const INITIAL_STATE = {
  gameId: null,
  currentMoveNumber: 0,
  totalMoves: 0,
  isPlaying: false,
  playbackSpeed: 1000,
  moves: [],
  boardState: null,
  isLoading: false,
  error: null,
}

export const useReplayStore = create<ReplayState>((set, get) => ({
  ...INITIAL_STATE,

  loadReplay: async (gameId: string) => {
    set({ isLoading: true, error: null })

    try {
      const moves = await gameService.getMovesForReplay(gameId)
      const boardState = await gameService.getBoardStateAtMove(gameId, 0)

      set({
        gameId,
        moves,
        totalMoves: moves.length,
        currentMoveNumber: 0,
        boardState,
        isLoading: false,
      })
    } catch (error) {
      set({
        error: error instanceof Error ? error.message : 'Failed to load replay',
        isLoading: false,
      })
    }
  },

  setCurrentMove: async (moveNumber: number) => {
    const { gameId, totalMoves, isPlaying } = get()

    if (!gameId) return

    const clampedMove = Math.max(0, Math.min(moveNumber, totalMoves))

    try {
      const boardState = await gameService.getBoardStateAtMove(gameId, clampedMove)

      set({
        currentMoveNumber: clampedMove,
        boardState,
        isPlaying: isPlaying && clampedMove < totalMoves,
      })
    } catch (error) {
      set({
        error: error instanceof Error ? error.message : 'Failed to load board state',
      })
    }
  },

  nextMove: async () => {
    const { currentMoveNumber, totalMoves, setCurrentMove } = get()

    if (currentMoveNumber < totalMoves) {
      await setCurrentMove(currentMoveNumber + 1)
    }
  },

  previousMove: async () => {
    const { currentMoveNumber, setCurrentMove } = get()

    if (currentMoveNumber > 0) {
      await setCurrentMove(currentMoveNumber - 1)
    }
  },

  firstMove: async () => {
    const { setCurrentMove } = get()
    await setCurrentMove(0)
  },

  lastMove: async () => {
    const { totalMoves, setCurrentMove } = get()
    await setCurrentMove(totalMoves)
  },

  togglePlayback: () => {
    set((state) => ({
      isPlaying: !state.isPlaying,
    }))
  },

  setPlaybackSpeed: (speed: number) => {
    set({ playbackSpeed: speed })
  },

  reset: () => {
    set(INITIAL_STATE)
  },
}))
