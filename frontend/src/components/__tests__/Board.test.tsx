// @vitest-environment jsdom
import { describe, it, expect, vi, afterEach } from 'vitest'
import { render, screen, fireEvent, cleanup } from '@testing-library/react'
import { Board } from '../Board'
import type { PiecePosition } from '../../types/api'

afterEach(cleanup)

const emptyBoard: PiecePosition[] = []

const initialBoard: PiecePosition[] = [
  // BLACK pawn at row 6, col 4
  { row: 6, column: 4, type: 'PAWN', owner: 'BLACK', promoted: false },
  // WHITE pawn at row 2, col 4
  { row: 2, column: 4, type: 'PAWN', owner: 'WHITE', promoted: false },
  // BLACK king at row 8, col 4
  { row: 8, column: 4, type: 'KING', owner: 'BLACK', promoted: false },
]

describe('Board component', () => {
  describe('rendering', () => {
    it('renders a 9x9 grid of cells', () => {
      const { container } = render(<Board boardState={emptyBoard} />)
      const cells = container.querySelectorAll('.board-cell')
      expect(cells).toHaveLength(81)
    })

    it('renders board with pieces', () => {
      const { container } = render(<Board boardState={initialBoard} />)
      const cells = container.querySelectorAll('.board-cell')
      expect(cells).toHaveLength(81)
    })

    it('renders suji labels (9-1)', () => {
      const { container } = render(<Board boardState={emptyBoard} />)
      const labels = ['9', '8', '7', '6', '5', '4', '3', '2', '1']
      labels.forEach(label => {
        const el = container.querySelector(`.board-suji-label:nth-child(${labels.indexOf(label) + 2})`)
        expect(el?.textContent).toBe(label)
      })
    })

    it('renders dan labels (一-九)', () => {
      const { container } = render(<Board boardState={emptyBoard} />)
      const danLabels = container.querySelectorAll('.board-dan-label')
      expect(danLabels).toHaveLength(9)
      expect(danLabels[0].textContent).toBe('一')
      expect(danLabels[8].textContent).toBe('九')
    })
  })

  describe('piece selection', () => {
    it('does not select piece when not interactive', () => {
      const onMove = vi.fn()
      const { container } = render(
        <Board
          boardState={initialBoard}
          onMove={onMove}
          interactive={false}
          currentTurn="BLACK"
        />
      )
      const cells = container.querySelectorAll('.board-cell')
      // Click cell at row 6, col 4 (BLACK pawn)
      fireEvent.click(cells[6 * 9 + 4])
      expect(container.querySelectorAll('.selected')).toHaveLength(0)
      expect(onMove).not.toHaveBeenCalled()
    })

    it('selects own piece when interactive', () => {
      const { container } = render(
        <Board
          boardState={initialBoard}
          onMove={vi.fn()}
          interactive={true}
          currentTurn="BLACK"
        />
      )
      const cells = container.querySelectorAll('.board-cell')
      // Click BLACK pawn at row 6, col 4
      fireEvent.click(cells[6 * 9 + 4])
      expect(container.querySelectorAll('.selected')).toHaveLength(1)
    })

    it('does not select opponent piece', () => {
      const { container } = render(
        <Board
          boardState={initialBoard}
          onMove={vi.fn()}
          interactive={true}
          currentTurn="BLACK"
        />
      )
      const cells = container.querySelectorAll('.board-cell')
      // Click WHITE pawn at row 2, col 4 (not BLACK's turn)
      fireEvent.click(cells[2 * 9 + 4])
      expect(container.querySelectorAll('.selected')).toHaveLength(0)
    })

    it('deselects piece by clicking same cell again', () => {
      const { container } = render(
        <Board
          boardState={initialBoard}
          onMove={vi.fn()}
          interactive={true}
          currentTurn="BLACK"
        />
      )
      const cells = container.querySelectorAll('.board-cell')
      // Select
      fireEvent.click(cells[6 * 9 + 4])
      expect(container.querySelectorAll('.selected')).toHaveLength(1)
      // Deselect
      fireEvent.click(cells[6 * 9 + 4])
      expect(container.querySelectorAll('.selected')).toHaveLength(0)
    })

    it('calls onMove when moving to different cell', () => {
      const onMove = vi.fn()
      const { container } = render(
        <Board
          boardState={initialBoard}
          onMove={onMove}
          interactive={true}
          currentTurn="BLACK"
        />
      )
      const cells = container.querySelectorAll('.board-cell')
      // Select BLACK pawn at row 6, col 4
      fireEvent.click(cells[6 * 9 + 4])
      // Move to row 5, col 4
      fireEvent.click(cells[5 * 9 + 4])
      expect(onMove).toHaveBeenCalledWith({ row: 6, column: 4 }, { row: 5, column: 4 })
    })
  })

  describe('drop mode', () => {
    it('calls onDrop when dropping piece on empty cell', () => {
      const onDrop = vi.fn()
      const { container } = render(
        <Board
          boardState={emptyBoard}
          onDrop={onDrop}
          interactive={true}
          currentTurn="BLACK"
          dropMode={true}
          dropPieceType="PAWN"
        />
      )
      const cells = container.querySelectorAll('.board-cell')
      fireEvent.click(cells[4 * 9 + 4])
      expect(onDrop).toHaveBeenCalledWith({ row: 4, column: 4 }, 'PAWN')
    })

    it('does not call onDrop when clicking occupied cell', () => {
      const onDrop = vi.fn()
      const { container } = render(
        <Board
          boardState={initialBoard}
          onDrop={onDrop}
          interactive={true}
          currentTurn="BLACK"
          dropMode={true}
          dropPieceType="PAWN"
        />
      )
      const cells = container.querySelectorAll('.board-cell')
      // Row 6, col 4 has a piece
      fireEvent.click(cells[6 * 9 + 4])
      expect(onDrop).not.toHaveBeenCalled()
    })

    it('marks valid cells as droppable', () => {
      const { container } = render(
        <Board
          boardState={emptyBoard}
          interactive={true}
          currentTurn="BLACK"
          dropMode={true}
          dropPieceType="PAWN"
          invalidDropColumns={[4]}
        />
      )
      const droppableCells = container.querySelectorAll('.droppable')
      // All 81 cells are empty, 9 cells in col 4 are invalid => 72 droppable
      expect(droppableCells).toHaveLength(72)
    })
  })

  describe('promotion zone', () => {
    it('highlights promotion zone for BLACK piece selected', () => {
      const { container } = render(
        <Board
          boardState={initialBoard}
          onMove={vi.fn()}
          interactive={true}
          currentTurn="BLACK"
        />
      )
      const cells = container.querySelectorAll('.board-cell')
      // Select BLACK pawn at row 6, col 4
      fireEvent.click(cells[6 * 9 + 4])
      // Rows 0-2 should have promotion-zone class (9 * 3 = 27 cells)
      const promotionZoneCells = container.querySelectorAll('.promotion-zone')
      expect(promotionZoneCells).toHaveLength(27)
    })

    it('does not highlight promotion zone for KING', () => {
      const { container } = render(
        <Board
          boardState={initialBoard}
          onMove={vi.fn()}
          interactive={true}
          currentTurn="BLACK"
        />
      )
      const cells = container.querySelectorAll('.board-cell')
      // Select BLACK king at row 8, col 4
      fireEvent.click(cells[8 * 9 + 4])
      expect(container.querySelectorAll('.promotion-zone')).toHaveLength(0)
    })

    it('does not show promotion zone when no piece selected', () => {
      const { container } = render(
        <Board
          boardState={initialBoard}
          onMove={vi.fn()}
          interactive={true}
          currentTurn="BLACK"
        />
      )
      expect(container.querySelectorAll('.promotion-zone')).toHaveLength(0)
    })
  })

  describe('PiecePositionDto normalization', () => {
    it('accepts PiecePositionDto format (pieceType instead of type)', () => {
      const dtoPieces = [
        { row: 3, column: 3, pieceType: 'ROOK', owner: 'WHITE' as const, promoted: false }
      ]
      const { container } = render(<Board boardState={dtoPieces} />)
      const cells = container.querySelectorAll('.board-cell')
      expect(cells).toHaveLength(81)
    })
  })
})
