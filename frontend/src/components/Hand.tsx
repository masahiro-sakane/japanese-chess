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

  const isSelected = (pieceType: string): boolean => {
    return selectedPiece === pieceType
  }

  return (
    <div className={`hand hand-${playerColor.toLowerCase()}`}>
      <h4>{playerColor === 'BLACK' ? '先手の持ち駒' : '後手の持ち駒'}</h4>
      <div className="hand-pieces">
        {uniquePieces.length === 0 ? (
          <div className="hand-empty">なし</div>
        ) : (
          uniquePieces.map((pieceType) => {
            const count = pieceCounts[pieceType]
            const displayName = PIECE_DISPLAY[pieceType] || pieceType
            const selected = isSelected(pieceType)

            return (
              <div
                key={pieceType}
                className={`hand-piece ${isActive ? 'interactive' : ''} ${selected ? 'selected' : ''}`}
                onClick={() => handlePieceClick(pieceType)}
                style={{
                  cursor: isActive ? 'pointer' : 'default',
                  padding: '8px 12px',
                  margin: '4px',
                  border: selected ? '2px solid #007bff' : '1px solid #ccc',
                  borderRadius: '4px',
                  backgroundColor: selected ? '#e7f3ff' : '#fff',
                  display: 'inline-block',
                  fontWeight: selected ? 'bold' : 'normal',
                }}
              >
                <span className="piece-character">{displayName}</span>
                {count > 1 && <span className="piece-count"> ×{count}</span>}
              </div>
            )
          })
        )}
      </div>
    </div>
  )
}
