package dev.ruslan.minichess.core;

import dev.ruslan.minichess.data.Color;
import dev.ruslan.minichess.data.Piece;
import dev.ruslan.minichess.data.PieceType;

public class Board {
    private Piece[][] cells = new Piece[8][8];

    public Board() {
    }

    public static Board initial() {
        Board board = new Board();

        // Расставляем белые фигуры
        board.cells[0][0] = new Piece(PieceType.ROOK, Color.WHITE);
        board.cells[0][1] = new Piece(PieceType.KNIGHT, Color.WHITE);
        board.cells[0][2] = new Piece(PieceType.BISHOP, Color.WHITE);
        board.cells[0][3] = new Piece(PieceType.QUEEN, Color.WHITE);
        board.cells[0][4] = new Piece(PieceType.KING, Color.WHITE);
        board.cells[0][5] = new Piece(PieceType.BISHOP, Color.WHITE);
        board.cells[0][6] = new Piece(PieceType.KNIGHT, Color.WHITE);
        board.cells[0][7] = new Piece(PieceType.ROOK, Color.WHITE);

        // Белые пешки
        for (int file = 0; file < 8; file++) {
            board.cells[1][file] = new Piece(PieceType.PAWN, Color.WHITE);
        }

        // Расставляем черные фигуры
        board.cells[7][0] = new Piece(PieceType.ROOK, Color.BLACK);
        board.cells[7][1] = new Piece(PieceType.KNIGHT, Color.BLACK);
        board.cells[7][2] = new Piece(PieceType.BISHOP, Color.BLACK);
        board.cells[7][3] = new Piece(PieceType.QUEEN, Color.BLACK);
        board.cells[7][4] = new Piece(PieceType.KING, Color.BLACK);
        board.cells[7][5] = new Piece(PieceType.BISHOP, Color.BLACK);
        board.cells[7][6] = new Piece(PieceType.KNIGHT, Color.BLACK);
        board.cells[7][7] = new Piece(PieceType.ROOK, Color.BLACK);

        // Черные пешки
        for (int file = 0; file < 8; file++) {
            board.cells[6][file] = new Piece(PieceType.PAWN, Color.BLACK);
        }

        return board;
    }

    public Piece getPiece(int rank, int file) {
        return cells[rank][file];
    }

    public void setPiece(int rank, int file, Piece piece) {
        cells[rank][file] = piece;
    }

    public Piece[][] getCells() {
        return cells;
    }
}
