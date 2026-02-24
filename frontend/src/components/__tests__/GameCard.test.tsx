// @vitest-environment jsdom
import { describe, it, expect, afterEach } from 'vitest'
import { render, screen, cleanup } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { GameCard } from '../GameCard'
import type { GameQueryDto } from '../../types/api'

afterEach(cleanup)

function makeGame(overrides: Partial<GameQueryDto> = {}): GameQueryDto {
  return {
    gameId: 'aaaabbbb-cccc-dddd-eeee-ffff00001111',
    blackPlayerId: 'black-player-id',
    whitePlayerId: 'white-player-id',
    status: 'IN_PROGRESS',
    currentTurn: 'BLACK',
    winner: null,
    endReason: null,
    boardState: [],
    blackCapturedPieces: [],
    whiteCapturedPieces: [],
    moveCount: 5,
    createdAt: '2026-01-15T10:30:00',
    updatedAt: '2026-01-15T11:00:00',
    blackInCheck: false,
    whiteInCheck: false,
    aiGame: false,
    aiDifficulty: null,
    aiThinking: false,
    ...overrides,
  }
}

function renderCard(game: GameQueryDto) {
  return render(
    <BrowserRouter>
      <GameCard game={game} />
    </BrowserRouter>
  )
}

describe('GameCard', () => {
  describe('IN_PROGRESS game', () => {
    it('renders status badge as 対局中', () => {
      renderCard(makeGame())
      expect(screen.getByText('対局中')).toBeTruthy()
    })

    it('renders short game ID (first 8 chars)', () => {
      renderCard(makeGame())
      expect(screen.getByText('aaaabbbb')).toBeTruthy()
    })

    it('renders short player IDs', () => {
      renderCard(makeGame())
      expect(screen.getByText('black-pl')).toBeTruthy()
      expect(screen.getByText('white-pl')).toBeTruthy()
    })

    it('renders current turn info', () => {
      renderCard(makeGame({ currentTurn: 'BLACK' }))
      // Multiple elements may contain 先手 (label + turn text)
      expect(screen.getAllByText(/先手/).length).toBeGreaterThan(0)
    })

    it('renders WHITE turn', () => {
      renderCard(makeGame({ currentTurn: 'WHITE' }))
      expect(screen.getAllByText(/後手/).length).toBeGreaterThan(0)
    })

    it('renders move count', () => {
      renderCard(makeGame({ moveCount: 12 }))
      expect(screen.getByText(/12/)).toBeTruthy()
    })

    it('links to game detail page', () => {
      const { container } = renderCard(makeGame())
      const link = container.querySelector('a')
      expect(link?.getAttribute('href')).toBe('/game/aaaabbbb-cccc-dddd-eeee-ffff00001111')
    })
  })

  describe('FINISHED game', () => {
    it('renders status badge as 終了', () => {
      renderCard(makeGame({ status: 'FINISHED' }))
      expect(screen.getByText('終了')).toBeTruthy()
    })

    it('does not render current turn for finished game', () => {
      renderCard(makeGame({ status: 'FINISHED', winner: 'BLACK' }))
      expect(screen.queryByText('現在の手番:')).toBeNull()
    })

    it('shows winner when present', () => {
      renderCard(makeGame({ status: 'FINISHED', winner: 'BLACK' }))
      expect(screen.getByText(/勝者/)).toBeTruthy()
      expect(screen.getAllByText(/先手/).length).toBeGreaterThan(0)
    })

    it('shows WHITE as winner', () => {
      renderCard(makeGame({ status: 'FINISHED', winner: 'WHITE' }))
      expect(screen.getAllByText(/後手/).length).toBeGreaterThan(0)
    })
  })

  describe('player label display', () => {
    it('shows 先手 label', () => {
      renderCard(makeGame())
      expect(screen.getByText('先手:')).toBeTruthy()
    })

    it('shows 後手 label', () => {
      renderCard(makeGame())
      expect(screen.getByText('後手:')).toBeTruthy()
    })
  })
})
