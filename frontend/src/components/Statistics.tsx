import { useEffect, useState } from 'react'
import { useGameStore } from '../stores/gameStore'
import { gameService } from '../services/gameService'
import type { DailyGameCountDto, PlayerRankingDto } from '../types/api'
import { GameStatusPieChart } from './charts/GameStatusPieChart'
import { DailyGamesLineChart } from './charts/DailyGamesLineChart'
import { PlayerRankingBarChart } from './charts/PlayerRankingBarChart'

export function Statistics() {
  const { statistics, loading, error, fetchStatistics, clearError } = useGameStore()
  const [dailyCounts, setDailyCounts] = useState<DailyGameCountDto[]>([])
  const [rankings, setRankings] = useState<PlayerRankingDto[]>([])
  const [chartsLoading, setChartsLoading] = useState(true)

  useEffect(() => {
    fetchStatistics()
  }, [fetchStatistics])

  useEffect(() => {
    setChartsLoading(true)
    Promise.all([
      gameService.getDailyGameCounts(),
      gameService.getPlayerRankings(),
    ])
      .then(([daily, rank]) => {
        setDailyCounts(daily)
        setRankings(rank)
      })
      .catch(() => {
        // Chart data errors are non-critical; show empty state
      })
      .finally(() => setChartsLoading(false))
  }, [])

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

      {/* Summary cards */}
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

      {/* Charts */}
      {chartsLoading ? (
        <div className="loading" style={{ marginTop: '20px' }}>グラフ読み込み中...</div>
      ) : (
        <div className="charts-grid">
          <div className="chart-card">
            <h3>対局状況 (円グラフ)</h3>
            <GameStatusPieChart statistics={statistics} />
          </div>

          <div className="chart-card">
            <h3>日別対局数 - 過去30日 (折れ線グラフ)</h3>
            <DailyGamesLineChart data={dailyCounts} />
          </div>

          <div className="chart-card chart-card--full">
            <h3>プレイヤーランキング Top10 (棒グラフ)</h3>
            <PlayerRankingBarChart rankings={rankings} />
            {rankings.length > 0 && (
              <table className="ranking-table">
                <thead>
                  <tr>
                    <th>順位</th>
                    <th>プレイヤーID</th>
                    <th>総対局</th>
                    <th>勝利</th>
                    <th>敗北</th>
                    <th>勝率</th>
                  </tr>
                </thead>
                <tbody>
                  {rankings.map((r) => (
                    <tr key={r.playerId}>
                      <td>{r.rank}</td>
                      <td className="monospace" style={{ fontSize: '12px' }}>
                        {r.playerId}
                      </td>
                      <td>{r.totalGames}</td>
                      <td style={{ color: '#4CAF50', fontWeight: 'bold' }}>{r.wins}</td>
                      <td style={{ color: '#f44336' }}>{r.losses}</td>
                      <td>{r.winRate.toFixed(1)}%</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
