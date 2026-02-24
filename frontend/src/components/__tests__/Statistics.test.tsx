// @vitest-environment jsdom
import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest'
import { render, screen, cleanup, waitFor } from '@testing-library/react'
import { Statistics } from '../Statistics'
import type { GameStatistics } from '../../types/api'

afterEach(cleanup)

const mockFetchStatistics = vi.fn()
const mockClearError = vi.fn()

let mockStatistics: GameStatistics | null = null
let mockLoading = false
let mockError: string | null = null

vi.mock('../../stores/gameStore', () => ({
  useGameStore: () => ({
    get statistics() { return mockStatistics },
    get loading() { return mockLoading },
    get error() { return mockError },
    fetchStatistics: mockFetchStatistics,
    clearError: mockClearError,
  }),
}))

vi.mock('../../services/gameService', () => ({
  gameService: {
    getDailyGameCounts: vi.fn().mockResolvedValue([]),
    getPlayerRankings: vi.fn().mockResolvedValue([]),
  },
}))

// Mock recharts to avoid canvas issues
vi.mock('../charts/GameStatusPieChart', () => ({
  GameStatusPieChart: () => <div data-testid="pie-chart" />,
}))
vi.mock('../charts/DailyGamesLineChart', () => ({
  DailyGamesLineChart: () => <div data-testid="line-chart" />,
}))
vi.mock('../charts/PlayerRankingBarChart', () => ({
  PlayerRankingBarChart: () => <div data-testid="bar-chart" />,
}))

beforeEach(() => {
  mockFetchStatistics.mockReset()
  mockFetchStatistics.mockResolvedValue(undefined)
  mockStatistics = null
  mockLoading = false
  mockError = null
})

describe('Statistics component', () => {
  it('shows loading indicator when loading and no statistics', () => {
    mockLoading = true
    render(<Statistics />)
    expect(screen.getByText('読み込み中...')).toBeTruthy()
  })

  it('shows error message when error', () => {
    mockError = 'Failed to load'
    render(<Statistics />)
    expect(screen.getByText(/Failed to load/)).toBeTruthy()
  })

  it('renders nothing when statistics is null and not loading', () => {
    const { container } = render(<Statistics />)
    expect(container.firstChild).toBeNull()
  })

  it('renders statistics when data available', async () => {
    mockStatistics = { totalGames: 10, activeGames: 3, completedGames: 7 }
    render(<Statistics />)

    expect(screen.getByText('統計情報')).toBeTruthy()
    expect(screen.getByText('10')).toBeTruthy()
    expect(screen.getByText('3')).toBeTruthy()
    expect(screen.getByText('7')).toBeTruthy()
  })

  it('renders stat card labels', async () => {
    mockStatistics = { totalGames: 10, activeGames: 3, completedGames: 7 }
    render(<Statistics />)

    expect(screen.getByText('総対局数')).toBeTruthy()
    expect(screen.getByText('対局中')).toBeTruthy()
    expect(screen.getByText('終了')).toBeTruthy()
  })

  it('calls fetchStatistics on mount', () => {
    mockStatistics = { totalGames: 0, activeGames: 0, completedGames: 0 }
    render(<Statistics />)
    expect(mockFetchStatistics).toHaveBeenCalledTimes(1)
  })

  it('shows charts when statistics available', async () => {
    mockStatistics = { totalGames: 5, activeGames: 2, completedGames: 3 }
    render(<Statistics />)

    await waitFor(() => {
      expect(screen.getByTestId('pie-chart')).toBeTruthy()
      expect(screen.getByTestId('line-chart')).toBeTruthy()
      expect(screen.getByTestId('bar-chart')).toBeTruthy()
    })
  })

  it('shows chart loading indicator while charts load', async () => {
    mockStatistics = { totalGames: 5, activeGames: 2, completedGames: 3 }

    const { gameService } = await import('../../services/gameService')
    vi.mocked(gameService.getDailyGameCounts).mockImplementation(
      () => new Promise(() => {}) // Never resolves
    )

    render(<Statistics />)
    expect(screen.getByText('グラフ読み込み中...')).toBeTruthy()
  })
})
