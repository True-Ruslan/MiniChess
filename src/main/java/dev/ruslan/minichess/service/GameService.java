package dev.ruslan.minichess.service;

import chess.mini.engine.ChessEngine;
import chess.mini.engine.core.Board;
import chess.mini.engine.core.CheckDetector;
import chess.mini.engine.data.Color;
import chess.mini.engine.data.Square;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {
    private ChessEngine chessEngine;
    private List<String> moves = new ArrayList<>();

    public GameService() {
        reset();
    }

    public Board getBoard() {
        return chessEngine.getBoard();
    }

    public Color getSideToMove() {
        return chessEngine.getSideToMove();
    }

    public List<String> getMoves() {
        return new ArrayList<>(moves);
    }

    public void reset() {
        chessEngine = new ChessEngine();
        moves.clear();
    }

    /**
     * Проверяет, атакуется ли указанная клетка фигурами заданного цвета
     */
    public boolean isSquareAttacked(Square target, Color byColor) {
        return chessEngine.isSquareAttacked(target, byColor);
    }

    /**
     * Проверяет, находится ли король заданного цвета под шахом
     */
    public boolean inCheck(Color color) {
        // Проверяем шах для указанного цвета
        return CheckDetector.inCheck(color, chessEngine.getBoard());
    }

    public List<Square> legalMovesFrom(Square from) {
        return chessEngine.getLegalMoves(from);
    }

    public void makeMove(Square from, Square to) {
        // Выполнение хода через движок
        chessEngine.makeMove(from, to);

        // Запись хода
        String moveNotation = squareToAlgebraic(from) + "-" + squareToAlgebraic(to);
        moves.add(moveNotation);
    }

    private String squareToAlgebraic(Square square) {
        char file = (char) ('a' + square.file());
        int rank = square.rank() + 1;
        return String.valueOf(file) + rank;
    }
}
