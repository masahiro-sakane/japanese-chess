import { useGameStore } from '../stores/gameStore'

interface PaginationProps {
  currentPage: number
  totalPages: number
  totalElements: number
}

export function Pagination({ currentPage, totalPages, totalElements }: PaginationProps) {
  const { setPage, pageSize, setPageSize } = useGameStore()

  if (totalPages <= 1) {
    return null
  }

  const getPageNumbers = (): number[] => {
    const pages: number[] = []
    const maxVisible = 5
    let start = Math.max(0, currentPage - Math.floor(maxVisible / 2))
    const end = Math.min(totalPages, start + maxVisible)

    if (end - start < maxVisible) {
      start = Math.max(0, end - maxVisible)
    }

    for (let i = start; i < end; i++) {
      pages.push(i)
    }

    return pages
  }

  const handlePageSizeChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    setPageSize(Number(event.target.value))
  }

  return (
    <div className="pagination">
      <div className="pagination-info">
        {currentPage * pageSize + 1} - {Math.min((currentPage + 1) * pageSize, totalElements)} / {totalElements} 件
      </div>

      <div className="pagination-controls">
        <button
          onClick={() => setPage(0)}
          disabled={currentPage === 0}
          className="pagination-button"
        >
          最初
        </button>

        <button
          onClick={() => setPage(currentPage - 1)}
          disabled={currentPage === 0}
          className="pagination-button"
        >
          前へ
        </button>

        {getPageNumbers().map((pageNum) => (
          <button
            key={pageNum}
            onClick={() => setPage(pageNum)}
            className={`pagination-button ${pageNum === currentPage ? 'active' : ''}`}
          >
            {pageNum + 1}
          </button>
        ))}

        <button
          onClick={() => setPage(currentPage + 1)}
          disabled={currentPage >= totalPages - 1}
          className="pagination-button"
        >
          次へ
        </button>

        <button
          onClick={() => setPage(totalPages - 1)}
          disabled={currentPage >= totalPages - 1}
          className="pagination-button"
        >
          最後
        </button>
      </div>

      <div className="page-size-selector">
        <label htmlFor="pageSize">表示件数:</label>
        <select id="pageSize" value={pageSize} onChange={handlePageSizeChange}>
          <option value={10}>10</option>
          <option value={20}>20</option>
          <option value={50}>50</option>
          <option value={100}>100</option>
        </select>
      </div>
    </div>
  )
}
