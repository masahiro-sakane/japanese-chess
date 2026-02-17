import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts'
import type { DailyGameCountDto } from '../../types/api'

interface Props {
  data: DailyGameCountDto[]
}

export function DailyGamesLineChart({ data }: Props) {
  if (data.length === 0) {
    return <div className="chart-empty">過去30日間の対局データがありません</div>
  }

  const formatted = data.map((d) => ({
    date: d.date.slice(5), // MM-DD
    count: d.count,
  }))

  return (
    <ResponsiveContainer width="100%" height={280}>
      <LineChart data={formatted} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="date" tick={{ fontSize: 12 }} />
        <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
        <Tooltip
          formatter={(value) => [`${value}局`, '対局数']}
          labelFormatter={(label) => `日付: ${label}`}
        />
        <Line
          type="monotone"
          dataKey="count"
          stroke="#8884d8"
          strokeWidth={2}
          dot={{ r: 3 }}
          activeDot={{ r: 6 }}
        />
      </LineChart>
    </ResponsiveContainer>
  )
}
