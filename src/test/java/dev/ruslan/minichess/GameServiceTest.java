package dev.ruslan.minichess;

import chess.mini.engine.core.Board;
import chess.mini.engine.data.Color;
import chess.mini.engine.data.Piece;
import chess.mini.engine.data.PieceType;
import chess.mini.engine.data.Square;
import dev.ruslan.minichess.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {

    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService();
    }

    @Test
    void testInitialPosition() {
        assertEquals(Color.WHITE, gameService.getSideToMove());
        assertEquals(0, gameService.getMoves().size());

        Board board = gameService.getBoard();
        assertNotNull(board);

        // Проверяем белые фигуры
        assertEquals(PieceType.ROOK, board.getPiece(0, 0).type());
        assertEquals(Color.WHITE, board.getPiece(0, 0).color());
        assertEquals(PieceType.PAWN, board.getPiece(1, 0).type());
        assertEquals(Color.WHITE, board.getPiece(1, 0).color());

        // Проверяем черные фигуры
        assertEquals(PieceType.ROOK, board.getPiece(7, 0).type());
        assertEquals(Color.BLACK, board.getPiece(7, 0).color());
        assertEquals(PieceType.PAWN, board.getPiece(6, 0).type());
        assertEquals(Color.BLACK, board.getPiece(6, 0).color());
    }

    @Test
    void testPawnMoves() {
        // Тест для белой пешки e2
        Square e2 = new Square(4, 1); // e2
        List<Square> moves = gameService.legalMovesFrom(e2);

        assertEquals(2, moves.size());
        assertTrue(moves.contains(new Square(4, 2))); // e3
        assertTrue(moves.contains(new Square(4, 3))); // e4

        // Тест для черной пешки e7
        gameService.makeMove(new Square(4, 1), new Square(4, 3)); // e2-e4
        assertEquals(Color.BLACK, gameService.getSideToMove());

        Square e7 = new Square(4, 6); // e7
        List<Square> blackMoves = gameService.legalMovesFrom(e7);

        assertEquals(2, blackMoves.size());
        assertTrue(blackMoves.contains(new Square(4, 5))); // e6
        assertTrue(blackMoves.contains(new Square(4, 4))); // e5
    }

    @Test
    void testRookMoves() {
        // Тест для белой ладьи a1 (заблокирована пешкой)
        Square a1 = new Square(0, 0);
        List<Square> moves = gameService.legalMovesFrom(a1);
        assertEquals(0, moves.size());

        // Тест для белой ладьи h1 (заблокирована пешкой)
        Square h1 = new Square(7, 0);
        moves = gameService.legalMovesFrom(h1);
        assertEquals(0, moves.size());
    }

    @Test
    void testBishopMoves() {
        // Тест для белого слона c1 (заблокирован пешкой)
        Square c1 = new Square(2, 0);
        List<Square> moves = gameService.legalMovesFrom(c1);
        assertEquals(0, moves.size());

        // Тест для белого слона f1 (заблокирован пешкой)
        Square f1 = new Square(5, 0);
        moves = gameService.legalMovesFrom(f1);
        assertEquals(0, moves.size());
    }

    @Test
    void testKnightMoves() {
        // Тест для белого коня b1
        Square b1 = new Square(1, 0);
        List<Square> moves = gameService.legalMovesFrom(b1);

        assertEquals(2, moves.size());
        assertTrue(moves.contains(new Square(0, 2))); // a3
        assertTrue(moves.contains(new Square(2, 2))); // c3

        // Тест для белого коня g1
        Square g1 = new Square(6, 0);
        moves = gameService.legalMovesFrom(g1);

        assertEquals(2, moves.size());
        assertTrue(moves.contains(new Square(5, 2))); // f3
        assertTrue(moves.contains(new Square(7, 2))); // h3
    }

    @Test
    void testQueenMoves() {
        // Тест для белого ферзя d1 (заблокирован пешкой)
        Square d1 = new Square(3, 0);
        List<Square> moves = gameService.legalMovesFrom(d1);
        assertEquals(0, moves.size());
    }

    @Test
    void testKingMoves() {
        // Тест для белого короля e1 (заблокирован пешкой)
        Square e1 = new Square(4, 0);
        List<Square> moves = gameService.legalMovesFrom(e1);
        assertEquals(0, moves.size());
    }

    @Test
    void testMakeMove() {
        // Выполняем ход e2-e4
        Square from = new Square(4, 1); // e2
        Square to = new Square(4, 3);   // e4

        gameService.makeMove(from, to);

        // Проверяем, что фигура переместилась
        Board board = gameService.getBoard();
        assertNull(board.getPiece(1, 4)); // e2 пуста (rank=1, file=4)
        assertNotNull(board.getPiece(3, 4)); // e4 содержит пешку (rank=3, file=4)
        assertEquals(PieceType.PAWN, board.getPiece(3, 4).type());
        assertEquals(Color.WHITE, board.getPiece(3, 4).color());

        // Проверяем, что сторона сменилась
        assertEquals(Color.BLACK, gameService.getSideToMove());

        // Проверяем, что ход записан
        List<String> moves = gameService.getMoves();
        assertEquals(1, moves.size());
        assertEquals("e2-e4", moves.get(0));
    }

    @Test
    void testMakeMoveValidation() {
        // Попытка сделать недопустимый ход
        Square from = new Square(4, 1); // e2
        Square to = new Square(4, 5);   // e6 (недопустимо)

        assertThrows(IllegalArgumentException.class, () -> {
            gameService.makeMove(from, to);
        });
    }

    @Test
    void testReset() {
        // Делаем несколько ходов
        gameService.makeMove(new Square(4, 1), new Square(4, 3)); // e2-e4
        gameService.makeMove(new Square(4, 6), new Square(4, 4)); // e7-e5

        assertEquals(2, gameService.getMoves().size());
        assertEquals(Color.WHITE, gameService.getSideToMove());

        // Сбрасываем игру
        gameService.reset();

        assertEquals(0, gameService.getMoves().size());
        assertEquals(Color.WHITE, gameService.getSideToMove());

        // Проверяем, что доска вернулась к начальной позиции
        Board board = gameService.getBoard();
        assertEquals(PieceType.PAWN, board.getPiece(1, 4).type()); // e2
        assertEquals(Color.WHITE, board.getPiece(1, 4).color());
        assertEquals(PieceType.PAWN, board.getPiece(6, 4).type()); // e7
        assertEquals(Color.BLACK, board.getPiece(6, 4).color());
    }

    @Test
    void testPawnCapture() {
        // Подготавливаем позицию для взятия
        gameService.makeMove(new Square(4, 1), new Square(4, 3)); // e2-e4
        gameService.makeMove(new Square(3, 6), new Square(3, 4)); // d7-d5

        // Тест взятия белой пешкой
        Square e4 = new Square(4, 3);
        List<Square> moves = gameService.legalMovesFrom(e4);

        assertTrue(moves.contains(new Square(3, 4))); // dxe5
        assertTrue(moves.contains(new Square(4, 4))); // e5
    }

    // ========== ТЕСТЫ ДЛЯ ПРОВЕРКИ ШАХА ==========

    @Test
    void testIsSquareAttacked() {
        // В начальной позиции e4 не атакуется
        Square e4 = new Square(4, 3);
        assertFalse(gameService.isSquareAttacked(e4, Color.BLACK));
        assertFalse(gameService.isSquareAttacked(e4, Color.WHITE));

        // После e2-e4, e4 не атакуется черными пешками (они слишком далеко)
        gameService.makeMove(new Square(4, 1), new Square(4, 3)); // e2-e4
        assertFalse(gameService.isSquareAttacked(e4, Color.BLACK)); // d6 и f6 не могут атаковать e4
        assertFalse(gameService.isSquareAttacked(e4, Color.WHITE));
    }

    @Test
    void testInCheck() {
        // Сбрасываем игру в начале теста
        gameService.reset();

        // В начальной позиции никто не под шахом
        assertFalse(gameService.inCheck(Color.WHITE));
        assertFalse(gameService.inCheck(Color.BLACK));

        // Создаем позицию с шахом: белый король под шахом от черного ферзя
        // Сначала делаем ход e2-e4, чтобы освободить e2
        gameService.makeMove(new Square(4, 1), new Square(4, 3)); // e2-e4

        // Делаем ответный ход черных, чтобы вернуть ход белым
        gameService.makeMove(new Square(4, 6), new Square(4, 4)); // e7-e5

        // Теперь ставим черного ферзя на e2 через прямой доступ к доске
        Board board = gameService.getBoard();
        board.setPiece(1, 4, new Piece(PieceType.QUEEN, Color.BLACK));

        // Теперь белый король под шахом
        assertTrue(gameService.inCheck(Color.WHITE));
        assertFalse(gameService.inCheck(Color.BLACK));
    }

    @Test
    void testBasicCheckDetection() {
        // Создаем простую позицию с шахом
        // Сначала делаем ход e2-e4, чтобы освободить e2
        gameService.makeMove(new Square(4, 1), new Square(4, 3)); // e2-e4

        // Делаем ответный ход черных, чтобы вернуть ход белым
        gameService.makeMove(new Square(4, 6), new Square(4, 4)); // e7-e5

        // Теперь ставим черного ферзя на e2 (шах белому королю)
        Board board = gameService.getBoard();
        board.setPiece(1, 4, new Piece(PieceType.QUEEN, Color.BLACK));

        // Теперь белый король под шахом
        assertTrue(gameService.inCheck(Color.WHITE));

        // Проверяем, что у белого короля есть легальные ходы (может уйти)
        Square e1 = new Square(4, 0); // e1
        List<Square> kingMoves = gameService.legalMovesFrom(e1);
        assertTrue(kingMoves.size() > 0); // Король может уйти от шаха
    }
}
