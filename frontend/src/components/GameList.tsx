import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useGameStore } from '../stores/gameStore'
import { GameCard } from './GameCard'
import { Pagination } from './Pagination'
import { StatusFilter } from './StatusFilter'

export function GameList() {
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
      // PostgreSQLのクリア
      await fetch('http://localhost:8080/api/admin/clear-database', {
        method: 'POST',
      })

      alert('データベースをクリアしました。ページをリロードします。')
      window.location.reload()
    } catch (err) {
      alert('データベースのクリアに失敗しました: ' + (err instanceof Error ? err.message : String(err)))
    } finally {
      setIsClearing(false)
    }
  }

  if (loading && games.length === 0) {
    return <div className="loading">読み込み中...</div>
  }

  if (error) {
    return (
      <div className="error">
        <p>エラー: {error}</p>
        <button onClick={clearError}>閉じる</button>
      </div>
    )
  }

  return (
    <div className="game-list-container">
      <div className="game-list-header">
        <h2>対局一覧</h2>
        <div className="game-list-info">
          <span>全 {totalElements} 件</span>
          <StatusFilter />
          <button
            onClick={handleClearAll}
            disabled={isClearing}
            style={{
              padding: '8px 16px',
              backgroundColor: isClearing ? '#ccc' : '#dc3545',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              fontWeight: 'bold',
              cursor: isClearing ? 'not-allowed' : 'pointer',
            }}
          >
            {isClearing ? 'クリア中...' : 'すべてクリア'}
          </button>
          <Link
            to="/create-ai"
            style={{
              padding: '8px 16px',
              backgroundColor: '#17a2b8',
              color: 'white',
              textDecoration: 'none',
              borderRadius: '4px',
              fontWeight: 'bold',
            }}
          >
            AI対局
          </Link>
          <Link
            to="/create"
            style={{
              padding: '8px 16px',
              backgroundColor: '#28a745',
              color: 'white',
              textDecoration: 'none',
              borderRadius: '4px',
              fontWeight: 'bold',
            }}
          >
            + 新規対局
          </Link>
        </div>
      </div>

      {games.length === 0 ? (
        <div className="empty-state">
          <p>対局が見つかりません</p>
        </div>
      ) : (
        <>
          <div className="game-list">
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
