package com.japanesechess.domain;

import java.util.*;
import java.util.stream.Collectors;

public class Board {
    private final Map<Position, Piece> pieces;
    private final Map<PlayerColor, List<PieceType>> capturedPieces;

    public Board() {
        this.pieces = new HashMap<>();
        this.capturedPieces = new EnumMap<>(PlayerColor.class);
        this.capturedPieces.put(PlayerColor.BLACK, new ArrayList<>());
        this.capturedPieces.put(PlayerColor.WHITE, new ArrayList<>());
    }

    private Board(Map<Position, Piece> pieces, Map<PlayerColor, List<PieceType>> capturedPieces) {
        this.pieces = new HashMap<>(pieces);
        this.capturedPieces = new EnumMap<>(PlayerColor.class);
        this.capturedPieces.put(PlayerColor.BLACK, new ArrayList<>(capturedPieces.get(PlayerColor.BLACK)));
        this.capturedPieces.put(PlayerColor.WHITE, new ArrayList<>(capturedPieces.get(PlayerColor.WHITE)));
    }

    public static Board createInitialBoard() {
        Board board = new Board();

        board.placePiece(new Piece(PieceType.LANCE, PlayerColor.WHITE, new Position(0, 0)));
        board.placePiece(new Piece(PieceType.KNIGHT, PlayerColor.WHITE, new Position(0, 1)));
        board.placePiece(new Piece(PieceType.SILVER, PlayerColor.WHITE, new Position(0, 2)));
        board.placePiece(new Piece(PieceType.GOLD, PlayerColor.WHITE, new Position(0, 3)));
        board.placePiece(new Piece(PieceType.KING, PlayerColor.WHITE, new Position(0, 4)));
        board.placePiece(new Piece(PieceType.GOLD, PlayerColor.WHITE, new Position(0, 5)));
        board.placePiece(new Piece(PieceType.SILVER, PlayerColor.WHITE, new Position(0, 6)));
        board.placePiece(new Piece(PieceType.KNIGHT, PlayerColor.WHITE, new Position(0, 7)));
        board.placePiece(new Piece(PieceType.LANCE, PlayerColor.WHITE, new Position(0, 8)));

        board.placePiece(new Piece(PieceType.ROOK, PlayerColor.WHITE, new Position(1, 7)));
        board.placePiece(new Piece(PieceType.BISHOP, PlayerColor.WHITE, new Position(1, 1)));

        for (int col = 0; col < 9; col++) {
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.WHITE, new Position(2, col)));
        }

        board.placePiece(new Piece(PieceType.LANCE, PlayerColor.BLACK, new Position(8, 8)));
        board.placePiece(new Piece(PieceType.KNIGHT, PlayerColor.BLACK, new Position(8, 7)));
        board.placePiece(new Piece(PieceType.SILVER, PlayerColor.BLACK, new Position(8, 6)));
        board.placePiece(new Piece(PieceType.GOLD, PlayerColor.BLACK, new Position(8, 5)));
        board.placePiece(new Piece(PieceType.KING, PlayerColor.BLACK, new Position(8, 4)));
        board.placePiece(new Piece(PieceType.GOLD, PlayerColor.BLACK, new Position(8, 3)));
        board.placePiece(new Piece(PieceType.SILVER, PlayerColor.BLACK, new Position(8, 2)));
        board.placePiece(new Piece(PieceType.KNIGHT, PlayerColor.BLACK, new Position(8, 1)));
        board.placePiece(new Piece(PieceType.LANCE, PlayerColor.BLACK, new Position(8, 0)));

        board.placePiece(new Piece(PieceType.ROOK, PlayerColor.BLACK, new Position(7, 1)));
        board.placePiece(new Piece(PieceType.BISHOP, PlayerColor.BLACK, new Position(7, 7)));

        for (int col = 0; col < 9; col++) {
            board.placePiece(new Piece(PieceType.PAWN, PlayerColor.BLACK, new Position(6, col)));
        }

        return board;
    }

    public void placePiece(Piece piece) {
        pieces.put(piece.getPosition(), piece);
    }

    public Optional<Piece> getPieceAt(Position position) {
        return Optional.ofNullable(pieces.get(position));
    }

    public void removePiece(Position position) {
        pieces.remove(position);
    }

    public Board applyMove(Move move) {
        Board newBoard = new Board(this.pieces, this.capturedPieces);

        if (move.isDrop()) {
            Piece droppedPiece = new Piece(move.getPieceType(), move.getPlayer(), move.getTo());
            newBoard.placePiece(droppedPiece);

            List<PieceType> captured = newBoard.capturedPieces.get(move.getPlayer());
            captured.remove(move.getPieceType());
        } else {
            Piece piece = newBoard.getPieceAt(move.getFrom())
                .orElseThrow(() -> new IllegalStateException("No piece at " + move.getFrom()));

            newBoard.removePiece(move.getFrom());

            Optional<Piece> capturedPiece = newBoard.getPieceAt(move.getTo());
            if (capturedPiece.isPresent()) {
                PieceType capturedType = capturedPiece.get().getType().unpromote();
                newBoard.capturedPieces.get(move.getPlayer()).add(capturedType);
            }

            Piece movedPiece = piece.withPosition(move.getTo());
            if (move.isPromote()) {
                movedPiece = movedPiece.promote();
            }

            newBoard.placePiece(movedPiece);
        }

        return newBoard;
    }

    public List<PieceType> getCapturedPieces(PlayerColor player) {
        return new ArrayList<>(capturedPieces.get(player));
    }

    public List<Piece> getAllPieces() {
        return new ArrayList<>(pieces.values());
    }

    public List<Piece> getPiecesForPlayer(PlayerColor player) {
        return pieces.values().stream()
            .filter(p -> p.getOwner() == player)
            .collect(Collectors.toList());
    }

    public Optional<Piece> findKing(PlayerColor player) {
        return pieces.values().stream()
            .filter(p -> p.getOwner() == player && p.getType() == PieceType.KING)
            .findFirst();
    }

    public Board copy() {
        return new Board(this.pieces, this.capturedPieces);
    }
}
