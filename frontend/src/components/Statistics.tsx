import { useEffect, useState } from 'react'
import { useGameStore } from '../stores/gameStore'
import { gameService } from '../services/gameService'
import type { DailyGameCountDto, PlayerRankingDto } from '../types/api'
import Spinner from '@atlaskit/spinner'
import SectionMessage, { SectionMessageAction } from '@atlaskit/section-message'
import DynamicTable from '@atlaskit/dynamic-table'
import PageHeader from '@atlaskit/page-header'
import { token } from '@atlaskit/tokens'
import { GameStatusPieChart } from './charts/GameStatusPieChart'
import { DailyGamesLineChart } from './charts/DailyGamesLineChart'
import { PlayerRankingBarChart } from './charts/PlayerRankingBarChart'

const RANKING_HEAD = {
  cells: [
    { key: 'rank', content: '順位', width: 8 },
    { key: 'playerId', content: 'プレイヤーID' },
    { key: 'totalGames', content: '総対局', width: 10 },
    { key: 'wins', content: '勝利', width: 10 },
    { key: 'losses', content: '敗北', width: 10 },
    { key: 'winRate', content: '勝率', width: 10 },
  ],
}

function StatCard({ label, value }: { label: string; value: number }) {
  return (
    <div style={{
      backgroundColor: '#FFFFFF',
      border: `1px solid ${token('color.border', '#DFE1E6')}`,
      borderRadius: 8,
      padding: token('space.200', '16px'),
      textAlign: 'center',
      flex: 1,
    }}>
      <div style={{
        fontSize: 13,
        color: token('color.text.subtle', '#6B778C'),
        marginBottom: token('space.075', '6px'),
      }}>
        {label}
      </div>
      <div style={{
        fontSize: 36,
        fontWeight: 700,
        color: token('color.text', '#172B4D'),
      }}>
        {value}
      </div>
    </div>
  )
}

function ChartCard({ title, children, fullWidth = false }: { title: string; children: React.ReactNode; fullWidth?: boolean }) {
  return (
    <div style={{
      backgroundColor: '#FFFFFF',
      border: `1px solid ${token('color.border', '#DFE1E6')}`,
      borderRadius: 8,
      padding: token('space.200', '16px'),
      gridColumn: fullWidth ? '1 / -1' : undefined,
    }}>
      <h3 style={{
        margin: `0 0 ${token('space.150', '12px')} 0`,
        fontSize: 14,
        fontWeight: 600,
        color: token('color.text', '#172B4D'),
      }}>
        {title}
      </h3>
      {children}
    </div>
  )
}

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
    Promise.all([gameService.getDailyGameCounts(), gameService.getPlayerRankings()])
      .then(([daily, rank]) => {
        setDailyCounts(daily)
        setRankings(rank)
      })
      .catch(() => { /* non-critical */ })
      .finally(() => setChartsLoading(false))
  }, [])

  const rankingRows = rankings.map(r => ({
    key: r.playerId,
    cells: [
      { key: 'rank', content: r.rank },
      {
        key: 'playerId',
        content: (
          <span style={{ fontFamily: 'monospace', fontSize: 12 }}>{r.playerId}</span>
        ),
      },
      { key: 'totalGames', content: r.totalGames },
      {
        key: 'wins',
        content: (
          <span style={{ color: token('color.text.success', '#006644'), fontWeight: 600 }}>
            {r.wins}
          </span>
        ),
      },
      {
        key: 'losses',
        content: (
          <span style={{ color: token('color.text.danger', '#AE2A19') }}>
            {r.losses}
          </span>
        ),
      },
      { key: 'winRate', content: `${r.winRate.toFixed(1)}%` },
    ],
  }))

  if (loading && !statistics) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: token('space.600', '48px') }}>
        <Spinner size="large" label="読み込み中..." />
      </div>
    )
  }

  if (error) {
    return (
      <SectionMessage
        appearance="error"
        title="エラーが発生しました"
        actions={[
          <SectionMessageAction key="close" onClick={clearError}>閉じる</SectionMessageAction>,
        ]}
      >
        {error}
      </SectionMessage>
    )
  }

  if (!statistics) return null

  return (
    <div>
      <PageHeader>統計情報</PageHeader>

      {/* サマリーカード */}
      <div style={{ display: 'flex', gap: token('space.200', '16px'), marginBottom: token('space.300', '24px'), flexWrap: 'wrap' }}>
        <StatCard label="総対局数" value={statistics.totalGames} />
        <StatCard label="対局中" value={statistics.activeGames} />
        <StatCard label="終了" value={statistics.completedGames} />
      </div>

      {/* グラフ */}
      {chartsLoading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: token('space.400', '32px') }}>
          <Spinner size="medium" label="グラフ読み込み中..." />
        </div>
      ) : (
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))',
          gap: token('space.200', '16px'),
        }}>
          <ChartCard title="対局状況 (円グラフ)">
            <GameStatusPieChart statistics={statistics} />
          </ChartCard>

          <ChartCard title="日別対局数 - 過去30日 (折れ線グラフ)">
            <DailyGamesLineChart data={dailyCounts} />
          </ChartCard>

          <ChartCard title="プレイヤーランキング Top10 (棒グラフ)" fullWidth>
            <PlayerRankingBarChart rankings={rankings} />
            {rankings.length > 0 && (
              <div style={{ marginTop: token('space.200', '16px') }}>
                <DynamicTable
                  head={RANKING_HEAD}
                  rows={rankingRows}
                  isFixedSize
                />
              </div>
            )}
          </ChartCard>
        </div>
      )}
    </div>
  )
}
