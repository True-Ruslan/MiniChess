package dev.ruslan.minichess;

import dev.ruslan.minichess.core.Board;
import dev.ruslan.minichess.data.Color;
import dev.ruslan.minichess.data.Piece;
import dev.ruslan.minichess.data.PieceType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {
    
    @Test
    public void testInitialBoardSetup() {
        Board board = Board.initial();
        
        // Проверяем белые фигуры на первой линии
        assertEquals(new Piece(PieceType.ROOK, Color.WHITE), board.getPiece(0, 0));
        assertEquals(new Piece(PieceType.KNIGHT, Color.WHITE), board.getPiece(0, 1));
        assertEquals(new Piece(PieceType.BISHOP, Color.WHITE), board.getPiece(0, 2));
        assertEquals(new Piece(PieceType.QUEEN, Color.WHITE), board.getPiece(0, 3));
        assertEquals(new Piece(PieceType.KING, Color.WHITE), board.getPiece(0, 4));
        assertEquals(new Piece(PieceType.BISHOP, Color.WHITE), board.getPiece(0, 5));
        assertEquals(new Piece(PieceType.KNIGHT, Color.WHITE), board.getPiece(0, 6));
        assertEquals(new Piece(PieceType.ROOK, Color.WHITE), board.getPiece(0, 7));
        
        // Проверяем белые пешки на второй линии
        for (int file = 0; file < 8; file++) {
            assertEquals(new Piece(PieceType.PAWN, Color.WHITE), board.getPiece(1, file));
        }
        
        // Проверяем черные фигуры на восьмой линии
        assertEquals(new Piece(PieceType.ROOK, Color.BLACK), board.getPiece(7, 0));
        assertEquals(new Piece(PieceType.KNIGHT, Color.BLACK), board.getPiece(7, 1));
        assertEquals(new Piece(PieceType.BISHOP, Color.BLACK), board.getPiece(7, 2));
        assertEquals(new Piece(PieceType.QUEEN, Color.BLACK), board.getPiece(7, 3));
        assertEquals(new Piece(PieceType.KING, Color.BLACK), board.getPiece(7, 4));
        assertEquals(new Piece(PieceType.BISHOP, Color.BLACK), board.getPiece(7, 5));
        assertEquals(new Piece(PieceType.KNIGHT, Color.BLACK), board.getPiece(7, 6));
        assertEquals(new Piece(PieceType.ROOK, Color.BLACK), board.getPiece(7, 7));
        
        // Проверяем черные пешки на седьмой линии
        for (int file = 0; file < 8; file++) {
            assertEquals(new Piece(PieceType.PAWN, Color.BLACK), board.getPiece(6, file));
        }
        
        // Проверяем что центральные клетки пустые
        for (int rank = 2; rank < 6; rank++) {
            for (int file = 0; file < 8; file++) {
                assertNull(board.getPiece(rank, file));
            }
        }
    }
    
    @Test
    public void testEmptyBoard() {
        Board board = new Board();
        
        // Проверяем что новая доска пустая
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                assertNull(board.getPiece(rank, file));
            }
        }
    }
}
