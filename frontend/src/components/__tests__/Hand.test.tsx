// @vitest-environment jsdom
import { describe, it, expect, vi, afterEach } from 'vitest'
import { render, screen, fireEvent, cleanup } from '@testing-library/react'
import { Hand } from '../Hand'

afterEach(cleanup)

describe('Hand component', () => {
  describe('empty hand', () => {
    it('shows なし when no pieces', () => {
      render(<Hand pieces={[]} playerColor="BLACK" isActive={false} />)
      expect(screen.getByText('なし')).toBeTruthy()
    })

    it('renders 先手の持ち駒 for BLACK', () => {
      render(<Hand pieces={[]} playerColor="BLACK" isActive={false} />)
      expect(screen.getByText('先手の持ち駒')).toBeTruthy()
    })

    it('renders 後手の持ち駒 for WHITE', () => {
      render(<Hand pieces={[]} playerColor="WHITE" isActive={false} />)
      expect(screen.getByText('後手の持ち駒')).toBeTruthy()
    })
  })

  describe('piece display', () => {
    it('shows 歩 for PAWN', () => {
      render(<Hand pieces={['PAWN']} playerColor="BLACK" isActive={false} />)
      expect(screen.getByText('歩')).toBeTruthy()
    })

    it('shows 角 for BISHOP', () => {
      render(<Hand pieces={['BISHOP']} playerColor="BLACK" isActive={false} />)
      expect(screen.getByText('角')).toBeTruthy()
    })

    it('shows 飛 for ROOK', () => {
      render(<Hand pieces={['ROOK']} playerColor="BLACK" isActive={false} />)
      expect(screen.getByText('飛')).toBeTruthy()
    })

    it('shows count ×2 for two pawns', () => {
      render(<Hand pieces={['PAWN', 'PAWN']} playerColor="BLACK" isActive={false} />)
      expect(screen.getByText('×2')).toBeTruthy()
    })

    it('does not show count when only 1 piece', () => {
      render(<Hand pieces={['GOLD']} playerColor="BLACK" isActive={false} />)
      expect(screen.queryByText(/×/)).toBeNull()
    })

    it('groups same piece types', () => {
      render(<Hand pieces={['PAWN', 'PAWN', 'PAWN']} playerColor="BLACK" isActive={false} />)
      const pawns = screen.getAllByText('歩')
      expect(pawns).toHaveLength(1) // grouped into one element
      expect(screen.getByText('×3')).toBeTruthy()
    })

    it('shows multiple piece types', () => {
      render(<Hand pieces={['PAWN', 'LANCE', 'BISHOP']} playerColor="BLACK" isActive={false} />)
      expect(screen.getByText('歩')).toBeTruthy()
      expect(screen.getByText('香')).toBeTruthy()
      expect(screen.getByText('角')).toBeTruthy()
    })
  })

  describe('interactivity', () => {
    it('calls onPieceSelect when active piece clicked', () => {
      const onPieceSelect = vi.fn()
      render(
        <Hand
          pieces={['PAWN']}
          playerColor="BLACK"
          isActive={true}
          onPieceSelect={onPieceSelect}
        />
      )
      fireEvent.click(screen.getByText('歩'))
      expect(onPieceSelect).toHaveBeenCalledWith('PAWN')
    })

    it('does not call onPieceSelect when not active', () => {
      const onPieceSelect = vi.fn()
      render(
        <Hand
          pieces={['PAWN']}
          playerColor="BLACK"
          isActive={false}
          onPieceSelect={onPieceSelect}
        />
      )
      fireEvent.click(screen.getByText('歩'))
      expect(onPieceSelect).not.toHaveBeenCalled()
    })

    it('applies selected style when piece is selected', () => {
      const { container } = render(
        <Hand
          pieces={['ROOK']}
          playerColor="BLACK"
          isActive={true}
          selectedPiece="ROOK"
        />
      )
      const piece = container.querySelector('.hand-piece.selected')
      expect(piece).toBeTruthy()
    })

    it('does not apply selected style for non-selected piece', () => {
      const { container } = render(
        <Hand
          pieces={['ROOK', 'BISHOP']}
          playerColor="BLACK"
          isActive={true}
          selectedPiece="ROOK"
        />
      )
      const selected = container.querySelectorAll('.hand-piece.selected')
      expect(selected).toHaveLength(1)
    })

    it('uses pointer cursor when active', () => {
      const { container } = render(
        <Hand
          pieces={['PAWN']}
          playerColor="BLACK"
          isActive={true}
        />
      )
      const piece = container.querySelector('.hand-piece') as HTMLElement
      expect(piece?.style.cursor).toBe('pointer')
    })

    it('uses default cursor when not active', () => {
      const { container } = render(
        <Hand
          pieces={['PAWN']}
          playerColor="BLACK"
          isActive={false}
        />
      )
      const piece = container.querySelector('.hand-piece') as HTMLElement
      expect(piece?.style.cursor).toBe('default')
    })
  })
})
