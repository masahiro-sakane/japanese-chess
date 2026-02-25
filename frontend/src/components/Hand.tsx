import Badge from '@atlaskit/badge'
import { token } from '@atlaskit/tokens'
import type { PieceType } from '../types/api'

interface HandProps {
  pieces: string[]
  playerColor: 'BLACK' | 'WHITE'
  isActive: boolean
  onPieceSelect?: (pieceType: PieceType) => void
  selectedPiece?: PieceType | null
}

const PIECE_DISPLAY: Record<string, string> = {
  PAWN: '歩',
  LANCE: '香',
  KNIGHT: '桂',
  SILVER: '銀',
  GOLD: '金',
  BISHOP: '角',
  ROOK: '飛',
  KING: '玉',
}

export function Hand({ pieces, playerColor, isActive, onPieceSelect, selectedPiece }: HandProps) {
  const pieceCounts = pieces.reduce((acc, piece) => {
    acc[piece] = (acc[piece] || 0) + 1
    return acc
  }, {} as Record<string, number>)

  const uniquePieces = Object.keys(pieceCounts)

  const handlePieceClick = (pieceType: string) => {
    if (!isActive || !onPieceSelect) return
    onPieceSelect(pieceType as PieceType)
  }

  const isSelected = (pieceType: string): boolean => selectedPiece === pieceType

  return (
    <div>
      <h4 style={{
        margin: `0 0 ${token('space.075', '6px')} 0`,
        fontSize: 13,
        fontWeight: 600,
        color: token('color.text.subtle', '#6B778C'),
      }}>
        {playerColor === 'BLACK' ? '先手の持ち駒' : '後手の持ち駒'}
      </h4>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: token('space.075', '6px'), minHeight: 36 }}>
        {uniquePieces.length === 0 ? (
          <span style={{ fontSize: 13, color: token('color.text.subtlest', '#8993A4'), fontStyle: 'italic' }}>
            なし
          </span>
        ) : (
          uniquePieces.map((pieceType) => {
            const count = pieceCounts[pieceType]
            const displayName = PIECE_DISPLAY[pieceType] || pieceType
            const selected = isSelected(pieceType)

            return (
              <button
                key={pieceType}
                onClick={() => handlePieceClick(pieceType)}
                disabled={!isActive}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: token('space.050', '4px'),
                  padding: `${token('space.075', '6px')} ${token('space.100', '8px')}`,
                  border: `2px solid ${selected
                    ? token('color.border.selected', '#0052CC')
                    : token('color.border', '#DFE1E6')}`,
                  borderRadius: 4,
                  backgroundColor: selected
                    ? token('color.background.selected', '#DEEBFF')
                    : '#FFFFFF',
                  cursor: isActive ? 'pointer' : 'default',
                  fontWeight: selected ? 700 : 400,
                  fontSize: 16,
                  color: token('color.text', '#172B4D'),
                  transition: 'border-color 0.15s, background-color 0.15s',
                }}
                aria-pressed={selected}
                aria-label={`${displayName} ${count}枚`}
              >
                <span>{displayName}</span>
                {count > 1 && (
                  <Badge appearance={selected ? 'primary' : 'default'} max={99}>
                    {count}
                  </Badge>
                )}
              </button>
            )
          })
        )}
      </div>
    </div>
  )
}
