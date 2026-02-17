import type { PiecePosition } from '../types/api'

interface PieceProps {
  piece: PiecePosition
  onClick?: () => void
}

const PIECE_NAMES: Record<string, string> = {
  KING: '王',
  ROOK: '飛',
  BISHOP: '角',
  GOLD: '金',
  SILVER: '銀',
  KNIGHT: '桂',
  LANCE: '香',
  PAWN: '歩',
  PROMOTED_ROOK: '龍',
  PROMOTED_BISHOP: '馬',
  PROMOTED_SILVER: '成銀',
  PROMOTED_KNIGHT: '成桂',
  PROMOTED_LANCE: '成香',
  PROMOTED_PAWN: 'と',
}

export function Piece({ piece, onClick }: PieceProps) {
  const displayName = PIECE_NAMES[piece.type] || piece.type
  const isBlack = piece.owner === 'BLACK'

  return (
    <div
      className={`piece ${isBlack ? 'black' : 'white'} ${piece.promoted ? 'promoted' : ''}`}
      onClick={onClick}
      role="button"
      tabIndex={0}
    >
      {displayName}
    </div>
  )
}
