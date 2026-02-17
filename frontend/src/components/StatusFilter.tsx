import { useGameStore } from '../stores/gameStore'

export function StatusFilter() {
  const { statusFilter, setStatusFilter } = useGameStore()

  const handleChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const value = event.target.value
    setStatusFilter(value === '' ? null : value)
  }

  return (
    <div className="status-filter">
      <label htmlFor="status">ステータス:</label>
      <select
        id="status"
        value={statusFilter || ''}
        onChange={handleChange}
      >
        <option value="">すべて</option>
        <option value="IN_PROGRESS">対局中</option>
        <option value="FINISHED">終了</option>
      </select>
    </div>
  )
}
