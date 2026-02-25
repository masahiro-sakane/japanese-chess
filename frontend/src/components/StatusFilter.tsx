import Select from '@atlaskit/select'
import { useGameStore } from '../stores/gameStore'

type StatusOption = { label: string; value: string }

const STATUS_OPTIONS: StatusOption[] = [
  { label: 'すべて', value: '' },
  { label: '対局中', value: 'IN_PROGRESS' },
  { label: '終了', value: 'FINISHED' },
]

export function StatusFilter() {
  const { statusFilter, setStatusFilter } = useGameStore()

  const currentValue = STATUS_OPTIONS.find(o => (o.value === '' ? statusFilter === null : o.value === statusFilter)) ?? STATUS_OPTIONS[0]

  const handleChange = (option: StatusOption | null) => {
    const value = option?.value ?? ''
    setStatusFilter(value === '' ? null : value)
  }

  return (
    <div style={{ minWidth: 140 }}>
      <Select<StatusOption>
        inputId="status-filter"
        options={STATUS_OPTIONS}
        value={currentValue}
        onChange={handleChange}
        placeholder="ステータス"
        isSearchable={false}
        aria-label="ステータスフィルター"
      />
    </div>
  )
}
