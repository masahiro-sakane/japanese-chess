// @vitest-environment jsdom
import { describe, it, expect, vi, afterEach } from 'vitest'
import { render, screen, cleanup } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { GameListPage } from '../GameListPage'
import { StatisticsPage } from '../StatisticsPage'

afterEach(cleanup)

// Mock GameList to avoid complex store setup
vi.mock('../../components/GameList', () => ({
  GameList: () => <div data-testid="game-list">GameList</div>,
}))

// Mock Statistics to avoid complex store setup
vi.mock('../../components/Statistics', () => ({
  Statistics: () => <div data-testid="statistics">Statistics</div>,
}))

describe('GameListPage', () => {
  it('renders GameList component', () => {
    render(
      <BrowserRouter>
        <GameListPage />
      </BrowserRouter>
    )
    expect(screen.getByTestId('game-list')).toBeTruthy()
  })

  it('has page class', () => {
    const { container } = render(
      <BrowserRouter>
        <GameListPage />
      </BrowserRouter>
    )
    expect(container.querySelector('.page')).toBeTruthy()
  })
})

describe('StatisticsPage', () => {
  it('renders Statistics component', () => {
    render(
      <BrowserRouter>
        <StatisticsPage />
      </BrowserRouter>
    )
    expect(screen.getByTestId('statistics')).toBeTruthy()
  })

  it('has page class', () => {
    const { container } = render(
      <BrowserRouter>
        <StatisticsPage />
      </BrowserRouter>
    )
    expect(container.querySelector('.page')).toBeTruthy()
  })
})
