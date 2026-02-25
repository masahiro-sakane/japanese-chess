import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useGameStore } from '../stores/gameStore'
import Spinner from '@atlaskit/spinner'
import EmptyState from '@atlaskit/empty-state'
import SectionMessage, { SectionMessageAction } from '@atlaskit/section-message'
import Button from '@atlaskit/button/new'
import PageHeader from '@atlaskit/page-header'
import { token } from '@atlaskit/tokens'
import { GameCard } from './GameCard'
import { Pagination } from './Pagination'
import { StatusFilter } from './StatusFilter'

export function GameList() {
  const navigate = useNavigate()
  const {
    games,
    loading,
    error,
    currentPage,
    totalPages,
    totalElements,
    fetchGames,
    clearError,
  } = useGameStore()
  const [isClearing, setIsClearing] = useState(false)

  useEffect(() => {
    fetchGames()
  }, [fetchGames])

  const handleClearAll = async () => {
    if (!confirm('すべての対局履歴とデータベースをクリアします。よろしいですか？\n\n※ この操作は取り消せません')) {
      return
    }
    setIsClearing(true)
    try {
      await fetch('http://localhost:8080/api/admin/clear-database', { method: 'POST' })
      alert('データベースをクリアしました。ページをリロードします。')
      window.location.reload()
    } catch (err) {
      alert('データベースのクリアに失敗しました: ' + (err instanceof Error ? err.message : String(err)))
    } finally {
      setIsClearing(false)
    }
  }

  if (loading && games.length === 0) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: token('space.600', '48px') }}>
        <Spinner size="large" label="読み込み中..." />
      </div>
    )
  }

  const actions = (
    <div style={{ display: 'flex', alignItems: 'center', gap: token('space.100', '8px'), flexWrap: 'wrap' }}>
      <span style={{ fontSize: 14, color: token('color.text.subtle', '#6B778C') }}>全 {totalElements} 件</span>
      <StatusFilter />
      <Button
        appearance="danger"
        isDisabled={isClearing}
        onClick={handleClearAll}
      >
        {isClearing ? 'クリア中...' : 'すべてクリア'}
      </Button>
      <Button
        appearance="primary"
        onClick={() => navigate('/create')}
      >
        + 新規対局
      </Button>
    </div>
  )

  return (
    <div>
      <PageHeader actions={actions}>対局一覧</PageHeader>

      {error && (
        <div style={{ marginBottom: token('space.200', '16px') }}>
          <SectionMessage
            appearance="error"
            title="エラーが発生しました"
            actions={[
              <SectionMessageAction key="close" onClick={clearError}>閉じる</SectionMessageAction>,
            ]}
          >
            {error}
          </SectionMessage>
        </div>
      )}

      {games.length === 0 ? (
        <EmptyState
          header="対局が見つかりません"
          description="新しい対局を作成して将棋を楽しみましょう。"
          primaryAction={
            <Button appearance="primary" onClick={() => navigate('/create')}>
              + 新規対局を作成
            </Button>
          }
        />
      ) : (
        <>
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
            gap: token('space.200', '16px'),
            marginBottom: token('space.300', '24px'),
          }}>
            {games.map((game) => (
              <GameCard key={game.gameId} game={game} />
            ))}
          </div>

          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            totalElements={totalElements}
          />
        </>
      )}
    </div>
  )
}
