import AtlaskitPagination from '@atlaskit/pagination'
import Select from '@atlaskit/select'
import { token } from '@atlaskit/tokens'
import { useGameStore } from '../stores/gameStore'

interface PaginationProps {
  currentPage: number
  totalPages: number
  totalElements: number
}

type PageSizeOption = { label: string; value: number }

const PAGE_SIZE_OPTIONS: PageSizeOption[] = [
  { label: '10件', value: 10 },
  { label: '20件', value: 20 },
  { label: '50件', value: 50 },
  { label: '100件', value: 100 },
]

export function Pagination({ currentPage, totalPages, totalElements }: PaginationProps) {
  const { setPage, pageSize, setPageSize } = useGameStore()

  if (totalPages <= 1) {
    return null
  }

  const pages = Array.from({ length: totalPages }, (_, i) => i + 1)

  const rangeStart = currentPage * pageSize + 1
  const rangeEnd = Math.min((currentPage + 1) * pageSize, totalElements)

  const currentSizeOption = PAGE_SIZE_OPTIONS.find(o => o.value === pageSize) ?? PAGE_SIZE_OPTIONS[0]

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      padding: `${token('space.150', '12px')} 0`,
      flexWrap: 'wrap',
      gap: token('space.150', '12px'),
    }}>
      <span style={{ fontSize: 14, color: token('color.text.subtle', '#6B778C') }}>
        {rangeStart} - {rangeEnd} / {totalElements} 件
      </span>

      <AtlaskitPagination
        pages={pages}
        selectedIndex={currentPage}
        onChange={(_e: React.SyntheticEvent, newPage: number) => setPage(newPage - 1)}
      />

      <div style={{ display: 'flex', alignItems: 'center', gap: token('space.100', '8px') }}>
        <span style={{ fontSize: 14, color: token('color.text.subtle', '#6B778C') }}>表示件数:</span>
        <div style={{ minWidth: 100 }}>
          <Select<PageSizeOption>
            inputId="page-size"
            options={PAGE_SIZE_OPTIONS}
            value={currentSizeOption}
            onChange={(opt: PageSizeOption | null) => opt && setPageSize(opt.value)}
            isSearchable={false}
            menuPlacement="top"
            aria-label="ページサイズ"
          />
        </div>
      </div>
    </div>
  )
}
