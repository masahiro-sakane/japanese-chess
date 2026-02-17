import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useGameStore } from '../stores/gameStore'
import { Board } from '../components/Board'
import { Hand } from '../components/Hand'
import { api } from '../services/api'
import type { GameQueryDto, PieceType } from '../types/api'
import { useGameWebSocket } from '../hooks/useGameWebSocket'

export function GameDetailPage() {
  const { gameId } = useParams<{ gameId: string }>()
  const { currentGame, loading, error, fetchGame, clearError } = useGameStore()
  const [moveError, setMoveError] = useState<string | null>(null)
  const [isMoving, setIsMoving] = useState(false)
  const [selectedDropPiece, setSelectedDropPiece] = useState<PieceType | null>(null)
  const [showResignDialog, setShowResignDialog] = useState(false)
  const [showPromotionDialog, setShowPromotionDialog] = useState(false)
  const [pendingMove, setPendingMove] = useState<{
    from: { row: number; column: number }
    to: { row: number; column: number }
    piece: any
    playerId: string
  } | null>(null)

  useEffect(() => {
    if (gameId) {
      fetchGame(gameId)
    }
  }, [gameId, fetchGame])

  useGameWebSocket(gameId, (game: GameQueryDto) => {
    useGameStore.setState({ currentGame: game })
  })

  // 駒が成れるかチェック
  const canPromote = (piece: any, fromRow: number, toRow: number, owner: string): boolean => {
    const promotablePieces = ['PAWN', 'LANCE', 'KNIGHT', 'SILVER', 'BISHOP', 'ROOK']
    if (!promotablePieces.includes(piece.type)) {
      return false
    }

    if (owner === 'BLACK') {
      // 先手: 敵陣（0-2行目）に入るか、敵陣から出る
      return toRow <= 2 || fromRow <= 2
    } else {
      // 後手: 敵陣（6-8行目）に入るか、敵陣から出る
      return toRow >= 6 || fromRow >= 6
    }
  }

  // 必ず成らなければならないかチェック
  const mustPromote = (piece: any, toRow: number, owner: string): boolean => {
    if (piece.type === 'PAWN' || piece.type === 'LANCE') {
      if (owner === 'BLACK') return toRow === 0
      else return toRow === 8
    }
    if (piece.type === 'KNIGHT') {
      if (owner === 'BLACK') return toRow <= 1
      else return toRow >= 7
    }
    return false
  }

  const handleMove = async (
    from: { row: number; column: number },
    to: { row: number; column: number }
  ) => {
    if (!currentGame || !gameId) return

    setMoveError(null)
    setSelectedDropPiece(null) // Clear drop selection

    const piece = currentGame.boardState.find(
      (p) => p.row === from.row && p.column === from.column
    )

    if (!piece) {
      setMoveError('駒が見つかりません')
      return
    }

    const playerId =
      currentGame.currentTurn === 'BLACK'
        ? currentGame.blackPlayerId
        : currentGame.whitePlayerId

    // 成り判定
    const owner = piece.owner
    if (canPromote(piece, from.row, to.row, owner)) {
      if (mustPromote(piece, to.row, owner)) {
        // 必ず成る
        await executeMove(from, to, piece, playerId, true)
      } else {
        // 選択させる
        setPendingMove({ from, to, piece, playerId })
        setShowPromotionDialog(true)
      }
    } else {
      // 成れない
      await executeMove(from, to, piece, playerId, false)
    }
  }

  const executeMove = async (
    from: { row: number; column: number },
    to: { row: number; column: number },
    piece: any,
    playerId: string,
    promote: boolean
  ) => {
    if (!gameId) return

    setIsMoving(true)

    try {
      await api.makeMove(gameId, {
        player: playerId,
        fromRow: from.row,
        fromColumn: from.column,
        toRow: to.row,
        toColumn: to.column,
        pieceType: piece.type,
        promote,
      })
      // Fallback fetch in case WebSocket is unavailable
      await fetchGame(gameId)
    } catch (err) {
      setMoveError(err instanceof Error ? err.message : '駒の移動に失敗しました')
    } finally {
      setIsMoving(false)
    }
  }

  const handlePromotionChoice = async (promote: boolean) => {
    setShowPromotionDialog(false)
    if (pendingMove) {
      await executeMove(
        pendingMove.from,
        pendingMove.to,
        pendingMove.piece,
        pendingMove.playerId,
        promote
      )
      setPendingMove(null)
    }
  }

  const handleDrop = async (
    to: { row: number; column: number },
    pieceType: PieceType
  ) => {
    if (!currentGame || !gameId) return

    // 二歩チェック: 歩兵を打つ場合、同じ列に自分の歩兵がないか確認
    if (pieceType === 'PAWN') {
      const currentPlayer = currentGame.currentTurn
      const hasPawnInColumn = currentGame.boardState.some(
        (p) => p.type === 'PAWN' && p.owner === currentPlayer && p.column === to.column
      )
      if (hasPawnInColumn) {
        setMoveError('二歩です。同じ列に歩兵がある場所には歩兵を打てません。')
        setSelectedDropPiece(null)
        return
      }
    }

    setIsMoving(true)
    setMoveError(null)
    setSelectedDropPiece(null)

    try {
      // 現在のターンに基づいてプレイヤーIDを取得
      const playerId =
        currentGame.currentTurn === 'BLACK'
          ? currentGame.blackPlayerId
          : currentGame.whitePlayerId

      await api.dropPiece(gameId, {
        player: playerId,
        toRow: to.row,
        toColumn: to.column,
        pieceType,
      })
      // Fallback fetch in case WebSocket is unavailable
      await fetchGame(gameId)
    } catch (err) {
      setMoveError(err instanceof Error ? err.message : '駒を打つことに失敗しました')
    } finally {
      setIsMoving(false)
    }
  }

  const handlePieceSelect = (pieceType: PieceType) => {
    if (selectedDropPiece === pieceType) {
      setSelectedDropPiece(null) // Deselect
    } else {
      setSelectedDropPiece(pieceType)
    }
  }

  const handleResign = async () => {
    if (!currentGame || !gameId) return

    setIsMoving(true)
    setMoveError(null)
    setShowResignDialog(false)

    try {
      // 現在のターンに基づいてプレイヤーIDを取得
      const playerId =
        currentGame.currentTurn === 'BLACK'
          ? currentGame.blackPlayerId
          : currentGame.whitePlayerId

      await api.resignGame(gameId, playerId)
      // Fallback fetch in case WebSocket is unavailable
      await fetchGame(gameId)
    } catch (err) {
      setMoveError(err instanceof Error ? err.message : '投了に失敗しました')
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
      {showPromotionDialog && pendingMove && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '30px',
            borderRadius: '8px',
            maxWidth: '400px',
            width: '90%',
            boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
          }}>
            <h3 style={{ marginTop: 0, marginBottom: '20px' }}>駒を成りますか？</h3>
            <p style={{ marginBottom: '20px', lineHeight: '1.6' }}>
              {pendingMove.piece.type === 'PAWN' && '歩'}
              {pendingMove.piece.type === 'LANCE' && '香'}
              {pendingMove.piece.type === 'KNIGHT' && '桂'}
              {pendingMove.piece.type === 'SILVER' && '銀'}
              {pendingMove.piece.type === 'BISHOP' && '角'}
              {pendingMove.piece.type === 'ROOK' && '飛'}
              を成りますか？
            </p>
            <div style={{ display: 'flex', gap: '10px', justifyContent: 'center' }}>
              <button
                onClick={() => handlePromotionChoice(false)}
                style={{
                  padding: '12px 24px',
                  fontSize: '16px',
                  backgroundColor: '#6c757d',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  minWidth: '120px'
                }}
              >
                成らない
              </button>
              <button
                onClick={() => handlePromotionChoice(true)}
                style={{
                  padding: '12px 24px',
                  fontSize: '16px',
                  backgroundColor: '#007bff',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  minWidth: '120px'
                }}
              >
                成る
              </button>
            </div>
          </div>
        </div>
      )}

      {showResignDialog && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '30px',
            borderRadius: '8px',
            maxWidth: '400px',
            width: '90%',
            boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
          }}>
            <h3 style={{ marginTop: 0, marginBottom: '20px' }}>投了の確認</h3>
            <p style={{ marginBottom: '20px', lineHeight: '1.6' }}>
              本当に投了しますか？<br />
              {currentGame?.currentTurn === 'BLACK' ? '先手' : '後手'}の負けとなります。
            </p>
            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
              <button
                onClick={() => setShowResignDialog(false)}
                style={{
                  padding: '8px 16px',
                  fontSize: '14px',
                  backgroundColor: '#6c757d',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                キャンセル
              </button>
              <button
                onClick={handleResign}
                style={{
                  padding: '8px 16px',
                  fontSize: '14px',
                  backgroundColor: '#dc3545',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                投了する
              </button>
            </div>
          </div>
        </div>
      )}

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
                  <dt>終了理由:</dt>
                  <dd>
                    {currentGame.endReason === 'CHECKMATE' && '詰み (Checkmate)'}
                    {currentGame.endReason === 'RESIGNATION' && '投了 (Resignation)'}
                    {currentGame.endReason === 'TIMEOUT' && 'タイムアップ (Timeout)'}
                    {!currentGame.endReason && '不明'}
                  </dd>
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
              <Hand
                pieces={currentGame.blackCapturedPieces}
                playerColor="BLACK"
                isActive={currentGame.status === 'IN_PROGRESS' && currentGame.currentTurn === 'BLACK'}
                onPieceSelect={handlePieceSelect}
                selectedPiece={selectedDropPiece}
              />
              <Hand
                pieces={currentGame.whiteCapturedPieces}
                playerColor="WHITE"
                isActive={currentGame.status === 'IN_PROGRESS' && currentGame.currentTurn === 'WHITE'}
                onPieceSelect={handlePieceSelect}
                selectedPiece={selectedDropPiece}
              />
            </div>
            {selectedDropPiece && (
              <div style={{
                marginTop: '10px',
                padding: '10px',
                backgroundColor: '#fff3cd',
                border: '1px solid #ffc107',
                borderRadius: '4px',
                textAlign: 'center',
                fontSize: '14px'
              }}>
                💡 盤面の空いているマスをクリックして駒を打ってください
              </div>
            )}
          </div>
        </div>

        <div className="game-board-panel">
          <h3>盤面</h3>

          {currentGame.status === 'FINISHED' && currentGame.endReason === 'CHECKMATE' && (
            <div style={{
              padding: '20px',
              marginBottom: '20px',
              backgroundColor: '#d4edda',
              border: '3px solid #28a745',
              borderRadius: '8px',
              textAlign: 'center'
            }}>
              <div style={{ fontSize: '28px', fontWeight: 'bold', marginBottom: '8px' }}>
                🎉 詰み! (Checkmate!)
              </div>
              <div style={{ fontSize: '18px', color: '#155724' }}>
                {currentGame.winner === 'BLACK' ? '先手' : '後手'}の勝利です!
              </div>
            </div>
          )}

          {currentGame.status === 'IN_PROGRESS' && (
            <>
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
                {(currentGame.blackInCheck || currentGame.whiteInCheck) && (
                  <div style={{
                    marginTop: '10px',
                    padding: '10px',
                    backgroundColor: '#f8d7da',
                    border: '2px solid #dc3545',
                    borderRadius: '4px',
                    color: '#721c24',
                    fontWeight: 'bold',
                    fontSize: '18px'
                  }}>
                    ⚠️ 王手! ({currentGame.blackInCheck ? '先手' : '後手'}の玉が狙われています)
                  </div>
                )}
              </div>

              <div style={{ marginBottom: '20px', textAlign: 'center' }}>
                <button
                  onClick={() => setShowResignDialog(true)}
                  disabled={isMoving}
                  style={{
                    padding: '10px 20px',
                    fontSize: '16px',
                    backgroundColor: '#dc3545',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: isMoving ? 'not-allowed' : 'pointer',
                    opacity: isMoving ? 0.6 : 1
                  }}
                >
                  投了する (Resign)
                </button>
              </div>
            </>
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
            onDrop={handleDrop}
            interactive={currentGame.status === 'IN_PROGRESS'}
            currentTurn={currentGame.currentTurn}
            dropMode={!!selectedDropPiece}
            dropPieceType={selectedDropPiece}
            invalidDropColumns={
              selectedDropPiece === 'PAWN'
                ? currentGame.boardState
                    .filter(
                      (p) =>
                        p.type === 'PAWN' &&
                        p.owner === currentGame.currentTurn
                    )
                    .map((p) => p.column)
                : []
            }
          />
        </div>
      </div>
    </div>
  )
}
