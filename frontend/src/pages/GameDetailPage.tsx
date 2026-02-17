import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useGameStore } from '../stores/gameStore'
import { Board } from '../components/Board'
import { api } from '../services/api'

export function GameDetailPage() {
  const { gameId } = useParams<{ gameId: string }>()
  const { currentGame, loading, error, fetchGame, clearError } = useGameStore()
  const [moveError, setMoveError] = useState<string | null>(null)
  const [isMoving, setIsMoving] = useState(false)

  useEffect(() => {
    if (gameId) {
      fetchGame(gameId)
    }
  }, [gameId, fetchGame])

  const handleMove = async (
    from: { row: number; column: number },
    to: { row: number; column: number }
  ) => {
    if (!currentGame || !gameId) return

    setIsMoving(true)
    setMoveError(null)

    const initialMoveCount = currentGame.moveCount

    try {
      const piece = currentGame.boardState.find(
        (p) => p.row === from.row && p.column === from.column
      )

      if (!piece) {
        throw new Error('駒が見つかりません')
      }

      // 現在のターンに基づいてプレイヤーIDを取得
      const playerId =
        currentGame.currentTurn === 'BLACK'
          ? currentGame.blackPlayerId
          : currentGame.whitePlayerId

      await api.makeMove(gameId, {
        player: playerId,
        fromRow: from.row,
        fromColumn: from.column,
        toRow: to.row,
        toColumn: to.column,
        pieceType: piece.type,
        promote: false, // TODO: 成り判定を実装
      })

      console.log('✅ Move command executed successfully')

      // Projectionの更新を待つ（ポーリング）
      let retries = 0
      const maxRetries = 20
      let updated = false

      while (retries < maxRetries && !updated) {
        await new Promise(resolve => setTimeout(resolve, 200))
        await fetchGame(gameId)

        const state = useGameStore.getState()
        if (state.currentGame && state.currentGame.moveCount > initialMoveCount) {
          console.log('✅ Projection updated! Move count:', state.currentGame.moveCount)
          updated = true
          break
        }
        retries++
        console.log(`⏳ Waiting for projection update... (${retries}/${maxRetries})`)
      }

      if (!updated) {
        console.warn('⚠️ Projection update timeout')
        setMoveError('盤面の更新に時間がかかっています。ページをリロードしてください。')
      }
    } catch (err) {
      setMoveError(err instanceof Error ? err.message : '駒の移動に失敗しました')
    } finally {
      setIsMoving(false)
    }
  }

  if (loading && !currentGame) {
    return <div className="loading">読み込み中...</div>
  }

  if (error) {
    return (
      <div className="error">
        <p>エラー: {error}</p>
        <button onClick={clearError}>閉じる</button>
        <Link to="/">対局一覧に戻る</Link>
      </div>
    )
  }

  if (!currentGame) {
    return (
      <div className="empty-state">
        <p>対局が見つかりません</p>
        <Link to="/">対局一覧に戻る</Link>
      </div>
    )
  }

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString)
    return date.toLocaleString('ja-JP')
  }

  return (
    <div className="page game-detail-page">
      <div className="game-detail-header">
        <Link to="/" className="back-link">
          ← 対局一覧に戻る
        </Link>
        <h2>対局詳細</h2>
        {currentGame.moveCount > 0 && (
          <Link to={`/replay/${currentGame.gameId}`} className="replay-link">
            棋譜再生 (Replay) →
          </Link>
        )}
      </div>

      <div className="game-detail-content">
        <div className="game-info-panel">
          <div className="info-section">
            <h3>対局情報</h3>
            <dl>
              <dt>ステータス:</dt>
              <dd>{currentGame.status === 'IN_PROGRESS' ? '対局中' : '終了'}</dd>

              <dt>対局ID:</dt>
              <dd className="monospace">{currentGame.gameId}</dd>

              <dt>先手:</dt>
              <dd className="monospace">{currentGame.blackPlayerId}</dd>

              <dt>後手:</dt>
              <dd className="monospace">{currentGame.whitePlayerId}</dd>

              {currentGame.status === 'IN_PROGRESS' && (
                <>
                  <dt>現在の手番:</dt>
                  <dd>{currentGame.currentTurn === 'BLACK' ? '先手' : '後手'}</dd>
                </>
              )}

              {currentGame.winner && (
                <>
                  <dt>勝者:</dt>
                  <dd>{currentGame.winner === 'BLACK' ? '先手' : '後手'}</dd>
                </>
              )}

              <dt>手数:</dt>
              <dd>{currentGame.moveCount}</dd>

              <dt>作成日時:</dt>
              <dd>{formatDate(currentGame.createdAt)}</dd>

              <dt>更新日時:</dt>
              <dd>{formatDate(currentGame.updatedAt)}</dd>
            </dl>
          </div>

          <div className="info-section">
            <h3>持ち駒</h3>
            <div className="captured-pieces">
              <div className="captured-section">
                <h4>先手の持ち駒</h4>
                <div className="captured-list">
                  {currentGame.blackCapturedPieces.length > 0 ? (
                    currentGame.blackCapturedPieces.join(', ')
                  ) : (
                    <span className="empty">なし</span>
                  )}
                </div>
              </div>
              <div className="captured-section">
                <h4>後手の持ち駒</h4>
                <div className="captured-list">
                  {currentGame.whiteCapturedPieces.length > 0 ? (
                    currentGame.whiteCapturedPieces.join(', ')
                  ) : (
                    <span className="empty">なし</span>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="game-board-panel">
          <h3>盤面</h3>

          {currentGame.status === 'IN_PROGRESS' && (
            <div style={{
              padding: '20px',
              marginBottom: '20px',
              backgroundColor: currentGame.currentTurn === 'BLACK' ? '#fff3cd' : '#d1ecf1',
              border: `3px solid ${currentGame.currentTurn === 'BLACK' ? '#ffc107' : '#17a2b8'}`,
              borderRadius: '8px',
              textAlign: 'center'
            }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', marginBottom: '8px' }}>
                {currentGame.currentTurn === 'BLACK' ? '⚫ 先手の番' : '⚪ 後手の番'}
              </div>
              <div style={{ fontSize: '14px', color: '#666' }}>
                手数: {currentGame.moveCount}
              </div>
            </div>
          )}

          {moveError && (
            <div className="move-error" style={{
              color: '#721c24',
              backgroundColor: '#f8d7da',
              border: '1px solid #f5c6cb',
              padding: '12px',
              marginBottom: '10px',
              borderRadius: '4px'
            }}>
              エラー: {moveError}
            </div>
          )}
          {isMoving && (
            <div className="move-loading" style={{
              color: '#004085',
              backgroundColor: '#cce5ff',
              border: '1px solid #b8daff',
              padding: '12px',
              marginBottom: '10px',
              borderRadius: '4px'
            }}>
              駒を移動中...
            </div>
          )}
          <Board
            boardState={currentGame.boardState}
            onMove={handleMove}
            interactive={currentGame.status === 'IN_PROGRESS'}
            currentTurn={currentGame.currentTurn}
          />
        </div>
      </div>
    </div>
  )
}
