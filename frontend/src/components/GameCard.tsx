import { Link } from 'react-router-dom'
import type { GameQueryDto } from '../types/api'

interface GameCardProps {
  game: GameQueryDto
}

export function GameCard({ game }: GameCardProps) {
  const formatDate = (dateString: string): string => {
    const date = new Date(dateString)
    return date.toLocaleString('ja-JP')
  }

  const getStatusBadge = (status: string): string => {
    return status === 'IN_PROGRESS' ? 'status-active' : 'status-finished'
  }

  const getStatusText = (status: string): string => {
    return status === 'IN_PROGRESS' ? '対局中' : '終了'
  }

  return (
    <Link to={`/game/${game.gameId}`} className="game-card">
      <div className="game-card-header">
        <span className={`status-badge ${getStatusBadge(game.status)}`}>
          {getStatusText(game.status)}
        </span>
        <span className="game-id">{game.gameId.substring(0, 8)}</span>
      </div>

      <div className="game-card-body">
        <div className="players">
          <div className="player">
            <span className="player-label">先手:</span>
            <span className="player-id">{game.blackPlayerId.substring(0, 8)}</span>
          </div>
          <div className="player">
            <span className="player-label">後手:</span>
            <span className="player-id">{game.whitePlayerId.substring(0, 8)}</span>
          </div>
        </div>

        {game.status === 'IN_PROGRESS' && (
          <div className="current-turn">
            現在の手番: {game.currentTurn === 'BLACK' ? '先手' : '後手'}
          </div>
        )}

        {game.winner && (
          <div className="winner">
            勝者: {game.winner === 'BLACK' ? '先手' : '後手'}
          </div>
        )}

        <div className="game-info">
          <span>手数: {game.moveCount}</span>
          <span>作成: {formatDate(game.createdAt)}</span>
        </div>
      </div>
    </Link>
  )
}
