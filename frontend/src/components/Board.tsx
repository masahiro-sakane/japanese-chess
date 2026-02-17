import { useState } from 'react'
import type { PiecePosition, PiecePositionDto, Player, PieceType } from '../types/api'
import { Piece } from './Piece'

type BoardPiece = PiecePosition | PiecePositionDto

interface BoardProps {
  boardState: BoardPiece[]
  onMove?: (from: { row: number; column: number }, to: { row: number; column: number }) => void
  onDrop?: (to: { row: number; column: number }, pieceType: PieceType) => void
  interactive?: boolean
  currentTurn?: Player
  dropMode?: boolean
  dropPieceType?: PieceType | null
}

export function Board({
  boardState,
  onMove,
  onDrop,
  interactive = false,
  currentTurn,
  dropMode = false,
  dropPieceType = null
}: BoardProps) {
  const [selectedCell, setSelectedCell] = useState<{ row: number; column: number } | null>(null)

  const createEmptyBoard = (): (PiecePosition | null)[][] => {
    return Array.from({ length: 9 }, () => Array(9).fill(null))
  }

  const board = createEmptyBoard()

  const normalizedPieces: PiecePosition[] = boardState.map((piece) => {
    if ('pieceType' in piece) {
      return {
        row: piece.row,
        column: piece.column,
        type: piece.pieceType,
        owner: piece.owner,
        promoted: piece.promoted,
      } as PiecePosition
    }
    return piece
  })

  normalizedPieces.forEach((piece) => {
    if (piece.row >= 0 && piece.row < 9 && piece.column >= 0 && piece.column < 9) {
      board[piece.row][piece.column] = piece
    }
  })

  const handleCellClick = (row: number, column: number) => {
    if (!interactive) return

    const clickedPiece = board[row][column]

    // Drop mode: place piece from hand
    if (dropMode && dropPieceType && onDrop) {
      // Only drop on empty cells
      if (!clickedPiece) {
        onDrop({ row, column }, dropPieceType)
      }
      return
    }

    // Move mode: move pieces on board
    if (onMove) {
      if (selectedCell) {
        // 移動先をクリック
        if (selectedCell.row === row && selectedCell.column === column) {
          // 同じマスをクリック → 選択解除
          setSelectedCell(null)
        } else {
          // 移動を実行
          onMove(selectedCell, { row, column })
          setSelectedCell(null)
        }
      } else {
        // 駒を選択
        if (clickedPiece && clickedPiece.owner === currentTurn) {
          setSelectedCell({ row, column })
        }
      }
    }
  }

  const isCellSelected = (row: number, column: number): boolean => {
    return selectedCell !== null && selectedCell.row === row && selectedCell.column === column
  }

  const canSelectPiece = (piece: PiecePosition | null): boolean => {
    if (!interactive || !piece || dropMode) return false
    return piece.owner === currentTurn
  }

  const canDropOnCell = (row: number, column: number): boolean => {
    if (!dropMode || !dropPieceType) return false
    return !board[row][column] // Can only drop on empty cells
  }

  return (
    <div className="board-container">
      <div className="board">
        {board.map((row, rowIndex) => (
          <div key={rowIndex} className="board-row">
            {row.map((cell, colIndex) => {
              const isSelected = isCellSelected(rowIndex, colIndex)
              const isSelectable = canSelectPiece(cell)
              const isDroppable = canDropOnCell(rowIndex, colIndex)
              const cellClass = `board-cell ${isSelected ? 'selected' : ''} ${
                interactive && isSelectable ? 'selectable' : ''
              } ${isDroppable ? 'droppable' : ''} ${interactive ? 'interactive' : ''}`

              return (
                <div
                  key={`${rowIndex}-${colIndex}`}
                  className={cellClass}
                  onClick={() => handleCellClick(rowIndex, colIndex)}
                  style={{
                    cursor: interactive ? 'pointer' : 'default',
                    backgroundColor: isDroppable ? '#d4edda' : undefined,
                  }}
                >
                  {cell && <Piece piece={cell} />}
                </div>
              )
            })}
          </div>
        ))}
      </div>
    </div>
  )
}
