import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts'
import type { PlayerRankingDto } from '../../types/api'

interface Props {
  rankings: PlayerRankingDto[]
}

function shortId(playerId: string): string {
  return playerId.slice(0, 8)
}

export function PlayerRankingBarChart({ rankings }: Props) {
  if (rankings.length === 0) {
    return <div className="chart-empty">ランキングデータがありません</div>
  }

  const data = rankings.map((r) => ({
    name: shortId(r.playerId),
    勝利: r.wins,
    敗北: r.losses,
  }))

  return (
    <ResponsiveContainer width="100%" height={280}>
      <BarChart data={data} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="name" tick={{ fontSize: 11 }} />
        <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
        <Tooltip />
        <Legend />
        <Bar dataKey="勝利" fill="#4CAF50" />
        <Bar dataKey="敗北" fill="#f44336" />
      </BarChart>
    </ResponsiveContainer>
  )
}
