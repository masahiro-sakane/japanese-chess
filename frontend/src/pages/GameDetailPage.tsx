import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useGameStore } from '../stores/gameStore'
import { Board } from '../components/Board'
import { Hand } from '../components/Hand'
import { AiThinkingIndicator } from '../components/AiThinkingIndicator'
import { api } from '../services/api'
import type { GameQueryDto, PieceType } from '../types/api'
import { useGameWebSocket } from '../hooks/useGameWebSocket'
import Button from '@atlaskit/button/new'
import Spinner from '@atlaskit/spinner'
import SectionMessage, { SectionMessageAction } from '@atlaskit/section-message'
import Banner from '@atlaskit/banner'
import Lozenge from '@atlaskit/lozenge'
import { token } from '@atlaskit/tokens'
import ModalDialog, {
  ModalHeader,
  ModalTitle,
  ModalBody,
  ModalFooter,
} from '@atlaskit/modal-dialog'

const PIECE_NAMES: Record<string, string> = {
  PAWN: '歩', LANCE: '香', KNIGHT: '桂', SILVER: '銀', BISHOP: '角', ROOK: '飛',
}

export function GameDetailPage() {
  const { gameId } = useParams<{ gameId: string }>()
  const navigate = useNavigate()
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

  const canPromote = (piece: any, fromRow: number, toRow: number, owner: string): boolean => {
    const promotablePieces = ['PAWN', 'LANCE', 'KNIGHT', 'SILVER', 'BISHOP', 'ROOK']
    if (!promotablePieces.includes(piece.type)) return false
    if (owner === 'BLACK') return toRow <= 2 || fromRow <= 2
    return toRow >= 6 || fromRow >= 6
  }

  const mustPromote = (piece: any, toRow: number, owner: string): boolean => {
    if (piece.type === 'PAWN' || piece.type === 'LANCE') {
      return owner === 'BLACK' ? toRow === 0 : toRow === 8
    }
    if (piece.type === 'KNIGHT') {
      return owner === 'BLACK' ? toRow <= 1 : toRow >= 7
    }
    return false
  }

  const handleMove = async (
    from: { row: number; column: number },
    to: { row: number; column: number }
  ) => {
    if (!currentGame || !gameId) return
    setMoveError(null)
    setSelectedDropPiece(null)

    const piece = currentGame.boardState.find(p => p.row === from.row && p.column === from.column)
    if (!piece) { setMoveError('駒が見つかりません'); return }

    const playerId = currentGame.currentTurn === 'BLACK' ? currentGame.blackPlayerId : currentGame.whitePlayerId

    if (canPromote(piece, from.row, to.row, piece.owner)) {
      if (mustPromote(piece, to.row, piece.owner)) {
        await executeMove(from, to, piece, playerId, true)
      } else {
        setPendingMove({ from, to, piece, playerId })
        setShowPromotionDialog(true)
      }
    } else {
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
      await api.makeMove(gameId, { player: playerId, fromRow: from.row, fromColumn: from.column, toRow: to.row, toColumn: to.column, pieceType: piece.type, promote })
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
      await executeMove(pendingMove.from, pendingMove.to, pendingMove.piece, pendingMove.playerId, promote)
      setPendingMove(null)
    }
  }

  const handleDrop = async (to: { row: number; column: number }, pieceType: PieceType) => {
    if (!currentGame || !gameId) return
    if (pieceType === 'PAWN') {
      const hasPawnInColumn = currentGame.boardState.some(
        p => p.type === 'PAWN' && p.owner === currentGame.currentTurn && p.column === to.column
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
      const playerId = currentGame.currentTurn === 'BLACK' ? currentGame.blackPlayerId : currentGame.whitePlayerId
      await api.dropPiece(gameId, { player: playerId, toRow: to.row, toColumn: to.column, pieceType })
      await fetchGame(gameId)
    } catch (err) {
      setMoveError(err instanceof Error ? err.message : '駒を打つことに失敗しました')
    } finally {
      setIsMoving(false)
    }
  }

  const handlePieceSelect = (pieceType: PieceType) => {
    setSelectedDropPiece(prev => prev === pieceType ? null : pieceType)
  }

  const handleResign = async () => {
    if (!currentGame || !gameId) return
    setIsMoving(true)
    setMoveError(null)
    setShowResignDialog(false)
    try {
      const playerId = currentGame.currentTurn === 'BLACK' ? currentGame.blackPlayerId : currentGame.whitePlayerId
      await api.resignGame(gameId, playerId)
      await fetchGame(gameId)
    } catch (err) {
      setMoveError(err instanceof Error ? err.message : '投了に失敗しました')
    } finally {
      setIsMoving(false)
    }
  }

  const formatDate = (dateString: string): string => new Date(dateString).toLocaleString('ja-JP')

  if (loading && !currentGame) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: token('space.600', '48px') }}>
        <Spinner size="large" label="読み込み中..." />
      </div>
    )
  }

  if (error) {
    return (
      <div style={{ padding: token('space.200', '16px') }}>
        <SectionMessage
          appearance="error"
          title="エラーが発生しました"
          actions={[
            <SectionMessageAction key="close" onClick={clearError}>閉じる</SectionMessageAction>,
            <SectionMessageAction key="back" onClick={() => navigate('/')}>対局一覧に戻る</SectionMessageAction>,
          ]}
        >
          {error}
        </SectionMessage>
      </div>
    )
  }

  if (!currentGame) {
    return (
      <div style={{ padding: token('space.200', '16px') }}>
        <SectionMessage
          appearance="warning"
          title="対局が見つかりません"
          actions={[
            <SectionMessageAction key="back" onClick={() => navigate('/')}>対局一覧に戻る</SectionMessageAction>,
          ]}
        >
          指定された対局IDのデータが見つかりませんでした。
        </SectionMessage>
      </div>
    )
  }

  const isInCheck = currentGame.blackInCheck || currentGame.whiteInCheck
  const checkedPlayer = currentGame.blackInCheck ? '先手' : '後手'

  return (
    <div style={{ maxWidth: 1400 }}>
      {/* 成りダイアログ */}
      {showPromotionDialog && pendingMove && (
        <ModalDialog onClose={() => { setShowPromotionDialog(false); setPendingMove(null) }}>
          <ModalHeader>
            <ModalTitle>駒を成りますか？</ModalTitle>
          </ModalHeader>
          <ModalBody>
            <p style={{ fontSize: 16 }}>
              {PIECE_NAMES[pendingMove.piece.type] || pendingMove.piece.type}を成りますか？
            </p>
          </ModalBody>
          <ModalFooter>
            <Button appearance="subtle" onClick={() => handlePromotionChoice(false)}>
              成らない
            </Button>
            <Button appearance="primary" onClick={() => handlePromotionChoice(true)}>
              成る
            </Button>
          </ModalFooter>
        </ModalDialog>
      )}

      {/* 投了確認ダイアログ */}
      {showResignDialog && (
        <ModalDialog onClose={() => setShowResignDialog(false)}>
          <ModalHeader>
            <ModalTitle appearance="danger">投了の確認</ModalTitle>
          </ModalHeader>
          <ModalBody>
            <p style={{ fontSize: 16, lineHeight: 1.6 }}>
              本当に投了しますか？<br />
              {currentGame.currentTurn === 'BLACK' ? '先手' : '後手'}の負けとなります。
            </p>
          </ModalBody>
          <ModalFooter>
            <Button appearance="subtle" onClick={() => setShowResignDialog(false)}>
              キャンセル
            </Button>
            <Button appearance="danger" onClick={handleResign}>
              投了する
            </Button>
          </ModalFooter>
        </ModalDialog>
      )}

      {/* 王手バナー */}
      {currentGame.status === 'IN_PROGRESS' && isInCheck && (
        <Banner appearance="warning">
          王手！ {checkedPlayer}の玉が狙われています
        </Banner>
      )}

      {/* ヘッダー */}
      <div style={{ marginBottom: token('space.200', '16px') }}>
        <Button appearance="subtle" onClick={() => navigate('/')}>
          ← 対局一覧に戻る
        </Button>
        {currentGame.moveCount > 0 && (
          <Button appearance="subtle" onClick={() => navigate(`/replay/${currentGame.gameId}`)}>
            棋譜再生 →
          </Button>
        )}
      </div>

      <div style={{
        display: 'grid',
        gridTemplateColumns: '1fr 2fr',
        gap: token('space.300', '24px'),
      }}>
        {/* 左パネル: 対局情報 */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: token('space.200', '16px') }}>
          {/* 対局情報 */}
          <div style={{
            backgroundColor: '#FFFFFF',
            border: `1px solid ${token('color.border', '#DFE1E6')}`,
            borderRadius: 8,
            padding: token('space.200', '16px'),
          }}>
            <h3 style={{ marginTop: 0, marginBottom: token('space.150', '12px'), color: token('color.text', '#172B4D'), fontSize: 16 }}>
              対局情報
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: token('space.100', '8px'), fontSize: 14 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ color: token('color.text.subtle', '#6B778C') }}>ステータス</span>
                <Lozenge appearance={currentGame.status === 'IN_PROGRESS' ? 'inprogress' : 'default'}>
                  {currentGame.status === 'IN_PROGRESS' ? '対局中' : '終了'}
                </Lozenge>
              </div>
              {currentGame.aiGame && (
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ color: token('color.text.subtle', '#6B778C') }}>モード</span>
                  <Lozenge appearance="new">AI対戦</Lozenge>
                </div>
              )}
              <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                <span style={{ color: token('color.text.subtle', '#6B778C') }}>対局ID</span>
                <span style={{ fontFamily: 'monospace', fontSize: 12, wordBreak: 'break-all' }}>{currentGame.gameId}</span>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                <span style={{ color: token('color.text.subtle', '#6B778C') }}>先手</span>
                <span style={{ fontFamily: 'monospace', fontSize: 12, wordBreak: 'break-all' }}>{currentGame.blackPlayerId}</span>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                <span style={{ color: token('color.text.subtle', '#6B778C') }}>後手</span>
                <span style={{ fontFamily: 'monospace', fontSize: 12, wordBreak: 'break-all' }}>{currentGame.whitePlayerId}</span>
              </div>
              {currentGame.status === 'IN_PROGRESS' && (
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: token('color.text.subtle', '#6B778C') }}>現在の手番</span>
                  <span style={{ fontWeight: 600 }}>{currentGame.currentTurn === 'BLACK' ? '先手' : '後手'}</span>
                </div>
              )}
              {currentGame.winner && (
                <>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: token('color.text.subtle', '#6B778C') }}>勝者</span>
                    <span style={{ fontWeight: 600 }}>{currentGame.winner === 'BLACK' ? '先手' : '後手'}</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: token('color.text.subtle', '#6B778C') }}>終了理由</span>
                    <span>
                      {currentGame.endReason === 'CHECKMATE' && '詰み'}
                      {currentGame.endReason === 'RESIGNATION' && '投了'}
                      {currentGame.endReason === 'TIMEOUT' && 'タイムアップ'}
                      {!currentGame.endReason && '不明'}
                    </span>
                  </div>
                </>
              )}
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: token('color.text.subtle', '#6B778C') }}>手数</span>
                <span style={{ fontWeight: 600 }}>{currentGame.moveCount}</span>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                <span style={{ color: token('color.text.subtle', '#6B778C') }}>作成日時</span>
                <span style={{ fontSize: 12 }}>{formatDate(currentGame.createdAt)}</span>
              </div>
            </div>
          </div>

          {/* 持ち駒 */}
          <div style={{
            backgroundColor: '#FFFFFF',
            border: `1px solid ${token('color.border', '#DFE1E6')}`,
            borderRadius: 8,
            padding: token('space.200', '16px'),
          }}>
            <h3 style={{ marginTop: 0, marginBottom: token('space.150', '12px'), color: token('color.text', '#172B4D'), fontSize: 16 }}>
              持ち駒
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: token('space.150', '12px') }}>
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
                marginTop: token('space.150', '12px'),
                padding: token('space.100', '8px'),
                backgroundColor: token('color.background.warning', '#FFFAE6'),
                border: `1px solid ${token('color.border.warning', '#FF991F')}`,
                borderRadius: 4,
                textAlign: 'center',
                fontSize: 13,
              }}>
                盤面の空いているマスをクリックして駒を打ってください
              </div>
            )}
          </div>
        </div>

        {/* 右パネル: 盤面 */}
        <div>
          <h3 style={{ marginTop: 0, marginBottom: token('space.150', '12px'), color: token('color.text', '#172B4D') }}>盤面</h3>

          {/* 詰み表示 */}
          {currentGame.status === 'FINISHED' && currentGame.endReason === 'CHECKMATE' && (
            <div style={{
              padding: token('space.200', '16px'),
              marginBottom: token('space.200', '16px'),
              backgroundColor: token('color.background.success', '#E3FCEF'),
              border: `2px solid ${token('color.border.success', '#00875A')}`,
              borderRadius: 8,
              textAlign: 'center',
            }}>
              <div style={{ fontSize: 24, fontWeight: 700, marginBottom: 8, color: token('color.text.success', '#006644') }}>
                詰み! (Checkmate)
              </div>
              <div style={{ fontSize: 16, color: token('color.text.success', '#006644') }}>
                {currentGame.winner === 'BLACK' ? '先手' : '後手'}の勝利です!
              </div>
            </div>
          )}

          {/* 対局中UI */}
          {currentGame.status === 'IN_PROGRESS' && (
            <>
              {currentGame.aiGame && currentGame.currentTurn === 'WHITE' && (
                <AiThinkingIndicator difficulty={currentGame.aiDifficulty} />
              )}

              <div style={{
                padding: token('space.200', '16px'),
                marginBottom: token('space.200', '16px'),
                backgroundColor: currentGame.currentTurn === 'BLACK'
                  ? token('color.background.warning', '#FFFAE6')
                  : token('color.background.information', '#DEEBFF'),
                border: `2px solid ${currentGame.currentTurn === 'BLACK'
                  ? token('color.border.warning', '#FF991F')
                  : token('color.border.information', '#2684FF')}`,
                borderRadius: 8,
                textAlign: 'center',
              }}>
                <div style={{ fontSize: 20, fontWeight: 700, marginBottom: 4 }}>
                  {currentGame.currentTurn === 'BLACK'
                    ? '先手の番'
                    : currentGame.aiGame ? 'AIの番' : '後手の番'}
                </div>
                <div style={{ fontSize: 13, color: token('color.text.subtle', '#6B778C') }}>
                  手数: {currentGame.moveCount}
                </div>
              </div>

              <div style={{ marginBottom: token('space.200', '16px'), textAlign: 'center' }}>
                <Button
                  appearance="danger"
                  isDisabled={isMoving}
                  onClick={() => setShowResignDialog(true)}
                >
                  投了する (Resign)
                </Button>
              </div>
            </>
          )}

          {/* エラー・移動中 */}
          {moveError && (
            <div style={{ marginBottom: token('space.150', '12px') }}>
              <SectionMessage
                appearance="error"
                title="エラー"
                actions={[
                  <SectionMessageAction key="close" onClick={() => setMoveError(null)}>閉じる</SectionMessageAction>,
                ]}
              >
                {moveError}
              </SectionMessage>
            </div>
          )}

          {isMoving && (
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: token('space.100', '8px'),
              padding: token('space.150', '12px'),
              marginBottom: token('space.150', '12px'),
              backgroundColor: token('color.background.information', '#DEEBFF'),
              borderRadius: 4,
              fontSize: 14,
              color: token('color.text.information', '#0052CC'),
            }}>
              <Spinner size="small" label="移動中" />
              駒を移動中...
            </div>
          )}

          <Board
            boardState={currentGame.boardState}
            onMove={handleMove}
            onDrop={handleDrop}
            interactive={
              currentGame.status === 'IN_PROGRESS' &&
              !(currentGame.aiGame && currentGame.currentTurn === 'WHITE')
            }
            currentTurn={currentGame.currentTurn}
            dropMode={!!selectedDropPiece}
            dropPieceType={selectedDropPiece}
            invalidDropColumns={
              selectedDropPiece === 'PAWN'
                ? currentGame.boardState
                    .filter(p => p.type === 'PAWN' && p.owner === currentGame.currentTurn)
                    .map(p => p.column)
                : []
            }
          />
        </div>
      </div>
    </div>
  )
}
