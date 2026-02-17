export type Player = 'BLACK' | 'WHITE'

export type PieceType =
  | 'PAWN'
  | 'LANCE'
  | 'KNIGHT'
  | 'SILVER'
  | 'GOLD'
  | 'BISHOP'
  | 'ROOK'
  | 'KING'
  | 'PROMOTED_PAWN'
  | 'PROMOTED_LANCE'
  | 'PROMOTED_KNIGHT'
  | 'PROMOTED_SILVER'
  | 'PROMOTED_BISHOP'
  | 'PROMOTED_ROOK'

export interface PiecePosition {
  row: number
  column: number
  type: string
  owner: Player
  promoted: boolean
}

export interface GameQueryDto {
  gameId: string
  blackPlayerId: string
  whitePlayerId: string
  status: string
  currentTurn: Player
  winner: Player | null
  boardState: PiecePosition[]
  blackCapturedPieces: string[]
  whiteCapturedPieces: string[]
  moveCount: number
  createdAt: string
  updatedAt: string
  blackInCheck: boolean
  whiteInCheck: boolean
}

export interface MoveHistoryDto {
  id: number
  moveNumber: number
  player: string
  fromRow: number | null
  fromColumn: number | null
  toRow: number
  toColumn: number
  pieceType: string
  promoted: boolean
  isDrop: boolean
  capturedPiece: string | null
  timestamp: string
}

export interface GameStatistics {
  totalGames: number
  activeGames: number
  completedGames: number
}

export interface PlayerStatistics {
  playerId: string
  totalGames: number
  activeGames: number
  wins: number
  losses: number
}

export interface PageResponse<T> {
  content: T[]
  pageable: {
    pageNumber: number
    pageSize: number
    offset: number
    paged: boolean
    unpaged: boolean
    sort: {
      sorted: boolean
      unsorted: boolean
      empty: boolean
    }
  }
  totalPages: number
  totalElements: number
  last: boolean
  size: number
  number: number
  sort: {
    sorted: boolean
    unsorted: boolean
    empty: boolean
  }
  numberOfElements: number
  first: boolean
  empty: boolean
}

export interface GameResponse {
  gameId: string
  status: string
  message: string
}

export interface CreateGameRequest {
  blackPlayerId: string
  whitePlayerId: string
}

export interface MovePieceRequest {
  gameId: string
  playerId: string
  fromRow: number
  fromColumn: number
  toRow: number
  toColumn: number
  promote: boolean
}

export interface DropPieceRequest {
  gameId: string
  playerId: string
  pieceType: string
  toRow: number
  toColumn: number
}

export interface ResignGameRequest {
  gameId: string
  playerId: string
}

export interface PiecePositionDto {
  row: number
  column: number
  pieceType: string
  owner: Player
  promoted: boolean
}

export interface BoardStateDto {
  pieces: PiecePositionDto[]
  blackCapturedPieces: string[]
  whiteCapturedPieces: string[]
  moveNumber: number
  currentPlayer: Player
}
