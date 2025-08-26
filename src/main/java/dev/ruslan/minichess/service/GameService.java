package dev.ruslan.minichess.service;

import dev.ruslan.minichess.core.Board;
import dev.ruslan.minichess.data.Color;
import dev.ruslan.minichess.data.Piece;
import dev.ruslan.minichess.data.PieceType;
import dev.ruslan.minichess.data.Square;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {
    private Board board;
    private Color sideToMove = Color.WHITE;
    private List<String> moves = new ArrayList<>();

    public GameService() {
        reset();
    }

    public Board getBoard() {
        return board;
    }

    public Color getSideToMove() {
        return sideToMove;
    }

    public List<String> getMoves() {
        return new ArrayList<>(moves);
    }

    public void reset() {
        board = Board.initial();
        sideToMove = Color.WHITE;
        moves.clear();
    }

    /**
     * Проверяет, атакуется ли указанная клетка фигурами заданного цвета
     */
    public boolean isSquareAttacked(Square target, Color byColor) {
        // Проверяем все фигуры заданного цвета
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Piece piece = board.getPiece(rank, file);
                if (piece != null && piece.color() == byColor) {
                    if (canPieceAttackSquare(new Square(file, rank), target, piece.type(), piece.color())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Проверяет, может ли фигура заданного типа атаковать указанную клетку
     */
    private boolean canPieceAttackSquare(Square from, Square target, PieceType pieceType, Color color) {
        return switch (pieceType) {
            case PAWN -> canPawnAttackSquare(from, target, color);
            case ROOK -> canRookAttackSquare(from, target);
            case BISHOP -> canBishopAttackSquare(from, target);
            case KNIGHT -> canKnightAttackSquare(from, target);
            case QUEEN -> canQueenAttackSquare(from, target);
            case KING -> canKingAttackSquare(from, target);
        };
    }

    private boolean canPawnAttackSquare(Square from, Square target, Color color) {
        int direction = (color == Color.WHITE) ? 1 : -1;

        // Проверяем диагональные атаки пешки
        for (int fileOffset : new int[]{-1, 1}) {
            int newFile = from.file() + fileOffset;
            int newRank = from.rank() + direction;

            if (newFile == target.file() && newRank == target.rank()) {
                return true;
            }
        }
        return false;
    }

    private boolean canRookAttackSquare(Square from, Square target) {
        // Проверяем горизонтальные и вертикальные линии
        if (from.file() != target.file() && from.rank() != target.rank()) {
            return false;
        }

        return isPathClear(from, target);
    }

    private boolean canBishopAttackSquare(Square from, Square target) {
        // Проверяем диагональные линии
        if (Math.abs(from.file() - target.file()) != Math.abs(from.rank() - target.rank())) {
            return false;
        }

        return isPathClear(from, target);
    }

    private boolean canKnightAttackSquare(Square from, Square target) {
        int rankDiff = Math.abs(from.rank() - target.rank());
        int fileDiff = Math.abs(from.file() - target.file());
        return (rankDiff == 2 && fileDiff == 1) || (rankDiff == 1 && fileDiff == 2);
    }

    private boolean canQueenAttackSquare(Square from, Square target) {
        // Ферзь может ходить как ладья или слон
        return canRookAttackSquare(from, target) || canBishopAttackSquare(from, target);
    }

    private boolean canKingAttackSquare(Square from, Square target) {
        int rankDiff = Math.abs(from.rank() - target.rank());
        int fileDiff = Math.abs(from.file() - target.file());
        return rankDiff <= 1 && fileDiff <= 1;
    }

    /**
     * Проверяет, свободен ли путь между двумя клетками
     */
    private boolean isPathClear(Square from, Square to) {
        int rankStep = Integer.compare(to.rank(), from.rank());
        int fileStep = Integer.compare(to.file(), from.file());

        int currentRank = from.rank() + rankStep;
        int currentFile = from.file() + fileStep;

        while (currentRank != to.rank() || currentFile != to.file()) {
            if (board.getPiece(currentRank, currentFile) != null) {
                return false; // Путь заблокирован
            }
            currentRank += rankStep;
            currentFile += fileStep;
        }

        return true;
    }

    /**
     * Проверяет, находится ли король заданного цвета под шахом
     */
    public boolean inCheck(Color color) {
        // Находим короля заданного цвета
        Square kingSquare = findKing(color);
        if (kingSquare == null) {
            return false; // Король не найден
        }

        // Проверяем, атакуется ли клетка короля фигурами противника
        Color opponentColor = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
        return isSquareAttacked(kingSquare, opponentColor);
    }

    /**
     * Находит короля заданного цвета на доске
     */
    private Square findKing(Color color) {
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Piece piece = board.getPiece(rank, file);
                if (piece != null && piece.type() == PieceType.KING && piece.color() == color) {
                    return new Square(file, rank);
                }
            }
        }
        return null;
    }

    /**
     * Проверяет, оставляет ли ход короля под боем
     */
    private boolean wouldMoveLeaveKingInCheck(Square from, Square to) {
        // Сохраняем текущее состояние
        Piece fromPiece = board.getPiece(from.rank(), from.file());
        Piece toPiece = board.getPiece(to.rank(), to.file());

        // Виртуально выполняем ход
        board.setPiece(from.rank(), from.file(), null);
        board.setPiece(to.rank(), to.file(), fromPiece);

        // Проверяем, под шахом ли король
        boolean inCheck = inCheck(sideToMove);

        // Восстанавливаем состояние
        board.setPiece(from.rank(), from.file(), fromPiece);
        board.setPiece(to.rank(), to.file(), toPiece);

        return inCheck;
    }

    public List<Square> legalMovesFrom(Square from) {
        List<Square> legalMoves = new ArrayList<>();
        Piece piece = board.getPiece(from.rank(), from.file());

        if (piece == null || piece.color() != sideToMove) {
            return legalMoves;
        }

        // Получаем все возможные ходы для фигуры
        List<Square> allMoves = new ArrayList<>();
        switch (piece.type()) {
            case PAWN:
                addPawnMoves(from, piece.color(), allMoves);
                break;
            case ROOK:
                addRookMoves(from, piece.color(), allMoves);
                break;
            case BISHOP:
                addBishopMoves(from, piece.color(), allMoves);
                break;
            case KNIGHT:
                addKnightMoves(from, piece.color(), allMoves);
                break;
            case QUEEN:
                addQueenMoves(from, piece.color(), allMoves);
                break;
            case KING:
                addKingMoves(from, piece.color(), allMoves);
                break;
        }

        // Фильтруем ходы, оставляющие короля под боем
        for (Square move : allMoves) {
            if (!wouldMoveLeaveKingInCheck(from, move)) {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }

    private void addPawnMoves(Square from, Color color, List<Square> legalMoves) {
        int direction = (color == Color.WHITE) ? 1 : -1;
        int startRank = (color == Color.WHITE) ? 1 : 6;

        // Ход вперед на 1 клетку
        int newRank = from.rank() + direction;
        if (newRank >= 0 && newRank < 8 && board.getPiece(newRank, from.file()) == null) {
            legalMoves.add(new Square(from.file(), newRank));

            // Первый ход на 2 клетки
            if (from.rank() == startRank) {
                int doubleRank = from.rank() + 2 * direction;
                if (doubleRank >= 0 && doubleRank < 8 && board.getPiece(doubleRank, from.file()) == null) {
                    legalMoves.add(new Square(from.file(), doubleRank));
                }
            }
        }

        // Взятие по диагонали
        for (int fileOffset : new int[]{-1, 1}) {
            int newFile = from.file() + fileOffset;
            int newRankCapture = from.rank() + direction;

            if (newFile >= 0 && newFile < 8 && newRankCapture >= 0 && newRankCapture < 8) {
                Piece targetPiece = board.getPiece(newRankCapture, newFile);
                if (targetPiece != null && targetPiece.color() != color) {
                    legalMoves.add(new Square(newFile, newRankCapture));
                }
            }
        }
    }

    private void addRookMoves(Square from, Color color, List<Square> legalMoves) {
        // Горизонтальные и вертикальные ходы
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        addSlidingMoves(from, color, legalMoves, directions);
    }

    private void addBishopMoves(Square from, Color color, List<Square> legalMoves) {
        // Диагональные ходы
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        addSlidingMoves(from, color, legalMoves, directions);
    }

    private void addKnightMoves(Square from, Color color, List<Square> legalMoves) {
        int[][] knightMoves = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        for (int[] move : knightMoves) {
            int newRank = from.rank() + move[0];
            int newFile = from.file() + move[1];

            if (newRank >= 0 && newRank < 8 && newFile >= 0 && newFile < 8) {
                Piece targetPiece = board.getPiece(newRank, newFile);
                if (targetPiece == null || targetPiece.color() != color) {
                    legalMoves.add(new Square(newFile, newRank));
                }
            }
        }
    }

    private void addQueenMoves(Square from, Color color, List<Square> legalMoves) {
        // Комбинация ладьи и слона
        addRookMoves(from, color, legalMoves);
        addBishopMoves(from, color, legalMoves);
    }

    private void addKingMoves(Square from, Color color, List<Square> legalMoves) {
        // Ходы на 1 клетку во всех направлениях
        for (int rankOffset = -1; rankOffset <= 1; rankOffset++) {
            for (int fileOffset = -1; fileOffset <= 1; fileOffset++) {
                if (rankOffset == 0 && fileOffset == 0) continue;

                int newRank = from.rank() + rankOffset;
                int newFile = from.file() + fileOffset;

                if (newRank >= 0 && newRank < 8 && newFile >= 0 && newFile < 8) {
                    Piece targetPiece = board.getPiece(newRank, newFile);
                    if (targetPiece == null || targetPiece.color() != color) {
                        legalMoves.add(new Square(newFile, newRank));
                    }
                }
            }
        }
    }

    private void addSlidingMoves(Square from, Color color, List<Square> legalMoves, int[][] directions) {
        for (int[] direction : directions) {
            int rankOffset = direction[0];
            int fileOffset = direction[1];

            for (int step = 1; step < 8; step++) {
                int newRank = from.rank() + step * rankOffset;
                int newFile = from.file() + step * fileOffset;

                if (newRank < 0 || newRank >= 8 || newFile < 0 || newFile >= 8) {
                    break;
                }

                Piece targetPiece = board.getPiece(newRank, newFile);
                if (targetPiece == null) {
                    legalMoves.add(new Square(newFile, newRank));
                } else {
                    if (targetPiece.color() != color) {
                        legalMoves.add(new Square(newFile, newRank));
                    }
                    break;
                }
            }
        }
    }

    public void makeMove(Square from, Square to) {
        // Валидация
        List<Square> legalMoves = legalMovesFrom(from);
        if (!legalMoves.contains(to)) {
            throw new IllegalArgumentException("Недопустимый ход");
        }

        // Выполнение хода
        Piece piece = board.getPiece(from.rank(), from.file());
        board.setPiece(from.rank(), from.file(), null);
        board.setPiece(to.rank(), to.file(), piece);

        // Запись хода
        String moveNotation = squareToAlgebraic(from) + "-" + squareToAlgebraic(to);
        moves.add(moveNotation);

        // Смена стороны
        sideToMove = (sideToMove == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private String squareToAlgebraic(Square square) {
        char file = (char) ('a' + square.file());
        int rank = square.rank() + 1;
        return String.valueOf(file) + rank;
    }
}
