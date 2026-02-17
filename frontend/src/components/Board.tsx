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
  invalidDropColumns?: number[]
}

export function Board({
  boardState,
  onMove,
  onDrop,
  interactive = false,
  currentTurn,
  dropMode = false,
  dropPieceType = null,
  invalidDropColumns = []
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
    if (board[row][column]) return false // Can only drop on empty cells
    if (invalidDropColumns.includes(column)) return false // 二歩など無効な列
    return true
  }

  const isPromotionZone = (row: number): boolean => {
    if (!selectedCell) return false
    const selectedPiece = board[selectedCell.row]?.[selectedCell.column]
    if (!selectedPiece) return false

    const promotablePieces = ['PAWN', 'LANCE', 'KNIGHT', 'SILVER', 'BISHOP', 'ROOK']
    if (!promotablePieces.includes(selectedPiece.type)) return false

    if (selectedPiece.owner === 'BLACK') {
      return row <= 2
    } else {
      return row >= 6
    }
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
              const isInPromotionZone = isPromotionZone(rowIndex)
              const cellClass = `board-cell ${isSelected ? 'selected' : ''} ${
                interactive && isSelectable ? 'selectable' : ''
              } ${isDroppable ? 'droppable' : ''} ${isInPromotionZone ? 'promotion-zone' : ''} ${interactive ? 'interactive' : ''}`

              let backgroundColor: string | undefined
              if (isDroppable) {
                backgroundColor = '#d4edda'
              } else if (isInPromotionZone) {
                backgroundColor = '#fff3cd'
              }

              return (
                <div
                  key={`${rowIndex}-${colIndex}`}
                  className={cellClass}
                  onClick={() => handleCellClick(rowIndex, colIndex)}
                  style={{
                    cursor: interactive ? 'pointer' : 'default',
                    backgroundColor,
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
