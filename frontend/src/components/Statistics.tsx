import { useEffect } from 'react'
import { useGameStore } from '../stores/gameStore'

export function Statistics() {
  const { statistics, loading, error, fetchStatistics, clearError } = useGameStore()

  useEffect(() => {
    fetchStatistics()
  }, [fetchStatistics])

  if (loading && !statistics) {
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

  if (!statistics) {
    return null
  }

  return (
    <div className="statistics">
      <h2>統計情報</h2>
      <div className="statistics-grid">
        <div className="stat-card">
          <div className="stat-label">総対局数</div>
          <div className="stat-value">{statistics.totalGames}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">対局中</div>
          <div className="stat-value">{statistics.activeGames}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">終了</div>
          <div className="stat-value">{statistics.completedGames}</div>
        </div>
      </div>
    </div>
  )
}
