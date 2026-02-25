import { useNavigate } from 'react-router-dom'
import Lozenge from '@atlaskit/lozenge'
import { token } from '@atlaskit/tokens'
import type { GameQueryDto } from '../types/api'

interface GameCardProps {
  game: GameQueryDto
}

export function GameCard({ game }: GameCardProps) {
  const navigate = useNavigate()

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString)
    return date.toLocaleString('ja-JP')
  }

  const getLozengeAppearance = (status: string) => {
    return status === 'IN_PROGRESS' ? 'inprogress' : 'default'
  }

  const getStatusText = (status: string): string => {
    return status === 'IN_PROGRESS' ? '対局中' : '終了'
  }

  return (
    <div
      onClick={() => navigate(`/game/${game.gameId}`)}
      style={{
        backgroundColor: '#FFFFFF',
        border: `1px solid ${token('color.border', '#DFE1E6')}`,
        borderRadius: 8,
        padding: token('space.200', '16px'),
        cursor: 'pointer',
        transition: 'box-shadow 0.2s, transform 0.15s',
        display: 'block',
        textDecoration: 'none',
        color: 'inherit',
      }}
      onMouseEnter={e => {
        const el = e.currentTarget as HTMLDivElement
        el.style.boxShadow = '0 4px 12px rgba(9,30,66,0.15)'
        el.style.transform = 'translateY(-2px)'
      }}
      onMouseLeave={e => {
        const el = e.currentTarget as HTMLDivElement
        el.style.boxShadow = 'none'
        el.style.transform = 'none'
      }}
      role="link"
      tabIndex={0}
      onKeyDown={e => e.key === 'Enter' && navigate(`/game/${game.gameId}`)}
    >
      {/* ヘッダー */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: token('space.150', '12px') }}>
        <Lozenge appearance={getLozengeAppearance(game.status)}>
          {getStatusText(game.status)}
        </Lozenge>
        {game.aiGame && (
          <Lozenge appearance="new">AI対戦</Lozenge>
        )}
        <span style={{ fontFamily: 'monospace', fontSize: 12, color: token('color.text.subtlest', '#8993A4') }}>
          {game.gameId.substring(0, 8)}
        </span>
      </div>

      {/* プレイヤー情報 */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: token('space.075', '6px'), marginBottom: token('space.150', '12px') }}>
        <div style={{ display: 'flex', gap: token('space.100', '8px'), fontSize: 14 }}>
          <span style={{ fontWeight: 600, minWidth: '4rem', color: token('color.text', '#172B4D') }}>先手:</span>
          <span style={{ fontFamily: 'monospace', color: token('color.text.subtle', '#6B778C') }}>
            {game.blackPlayerId.substring(0, 8)}
          </span>
        </div>
        <div style={{ display: 'flex', gap: token('space.100', '8px'), fontSize: 14 }}>
          <span style={{ fontWeight: 600, minWidth: '4rem', color: token('color.text', '#172B4D') }}>後手:</span>
          <span style={{ fontFamily: 'monospace', color: token('color.text.subtle', '#6B778C') }}>
            {game.whitePlayerId.substring(0, 8)}
          </span>
        </div>
      </div>

      {/* ターン・勝者 */}
      {game.status === 'IN_PROGRESS' && (
        <div style={{
          padding: `${token('space.075', '6px')} ${token('space.100', '8px')}`,
          backgroundColor: token('color.background.information', '#DEEBFF'),
          borderRadius: 4,
          fontSize: 13,
          color: token('color.text.information', '#0052CC'),
          marginBottom: token('space.100', '8px'),
        }}>
          現在の手番: {game.currentTurn === 'BLACK' ? '先手' : '後手'}
        </div>
      )}

      {game.winner && (
        <div style={{
          padding: `${token('space.075', '6px')} ${token('space.100', '8px')}`,
          backgroundColor: token('color.background.success', '#E3FCEF'),
          borderRadius: 4,
          fontSize: 13,
          color: token('color.text.success', '#006644'),
          marginBottom: token('space.100', '8px'),
        }}>
          勝者: {game.winner === 'BLACK' ? '先手' : '後手'}
        </div>
      )}

      {/* フッター */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        fontSize: 12,
        color: token('color.text.subtlest', '#8993A4'),
        paddingTop: token('space.100', '8px'),
        borderTop: `1px solid ${token('color.border', '#DFE1E6')}`,
        marginTop: token('space.100', '8px'),
      }}>
        <span>手数: {game.moveCount}</span>
        <span>作成: {formatDate(game.createdAt)}</span>
      </div>
    </div>
  )
}
