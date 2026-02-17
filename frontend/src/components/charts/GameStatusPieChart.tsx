import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts'
import type { GameStatistics } from '../../types/api'

interface Props {
  statistics: GameStatistics
}

const COLORS = ['#4CAF50', '#2196F3', '#9E9E9E']

export function GameStatusPieChart({ statistics }: Props) {
  const data = [
    { name: '対局中', value: statistics.activeGames },
    { name: '終了', value: statistics.completedGames },
  ].filter((d) => d.value > 0)

  if (data.length === 0) {
    return <div className="chart-empty">データがありません</div>
  }

  return (
    <ResponsiveContainer width="100%" height={280}>
      <PieChart>
        <Pie
          data={data}
          cx="50%"
          cy="50%"
          labelLine={false}
          label={({ name, percent }) =>
            `${name} ${((percent ?? 0) * 100).toFixed(0)}%`
          }
          outerRadius={90}
          dataKey="value"
        >
          {data.map((_, index) => (
            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
          ))}
        </Pie>
        <Tooltip formatter={(value) => [`${value}局`, '']} />
        <Legend />
      </PieChart>
    </ResponsiveContainer>
  )
}
