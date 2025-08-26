package dev.ruslan.minichess.controller;

import dev.ruslan.minichess.core.Board;
import dev.ruslan.minichess.data.Color;
import dev.ruslan.minichess.data.Piece;
import dev.ruslan.minichess.data.PieceType;
import dev.ruslan.minichess.data.Square;
import dev.ruslan.minichess.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BoardApiController {
    
    @Autowired
    private GameService gameService;
    
    @GetMapping("/board")
    public ResponseEntity<Map<String, Object>> getBoard() {
        Map<String, Object> response = new HashMap<>();
        response.put("sideToMove", gameService.getSideToMove().name());
        
        // Добавляем информацию о шахе
        response.put("inCheck", gameService.inCheck(gameService.getSideToMove()));
        response.put("whiteInCheck", gameService.inCheck(Color.WHITE));
        response.put("blackInCheck", gameService.inCheck(Color.BLACK));
        
        // Преобразуем доску в формат для JSON
        Piece[][] cells = gameService.getBoard().getCells();
        Object[][] boardData = new Object[8][8];
        
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Piece piece = cells[rank][file];
                if (piece != null) {
                    Map<String, String> pieceData = new HashMap<>();
                    pieceData.put("type", piece.type().name());
                    pieceData.put("color", piece.color().name());
                    boardData[rank][file] = pieceData;
                } else {
                    boardData[rank][file] = null;
                }
            }
        }
        
        response.put("cells", boardData);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/moves")
    public ResponseEntity<Map<String, Object>> getLegalMoves(@RequestParam String from) {
        try {
            Square fromSquare = parseSquare(from);
            List<Square> legalMoves = gameService.legalMovesFrom(fromSquare);
            
            Map<String, Object> response = new HashMap<>();
            response.put("from", from);
            response.put("moves", legalMoves.stream()
                .map(this::squareToAlgebraic)
                .collect(Collectors.toList()));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/move")
    public ResponseEntity<Map<String, Object>> makeMove(@RequestBody Map<String, String> moveRequest) {
        try {
            String fromStr = moveRequest.get("from");
            String toStr = moveRequest.get("to");
            
            if (fromStr == null || toStr == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Отсутствуют параметры 'from' или 'to'");
                return ResponseEntity.badRequest().body(error);
            }
            
            Square from = parseSquare(fromStr);
            Square to = parseSquare(toStr);
            
            gameService.makeMove(from, to);
            
            // Возвращаем новое состояние доски
            return getBoard();
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/move-list")
    public ResponseEntity<List<String>> getMoveList() {
        return ResponseEntity.ok(gameService.getMoves());
    }
    
    @PostMapping("/reset")
    public ResponseEntity<Void> reset() {
        gameService.reset();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    @PostMapping("/test-check")
    public ResponseEntity<Map<String, Object>> createTestCheckPosition() {
        // Создаем тестовую позицию с шахом
        Board board = gameService.getBoard();
        
        // Убираем пешку e2
        board.setPiece(1, 4, null);
        // Ставим черного ферзя на e2 (шах белому королю)
        board.setPiece(1, 4, new Piece(PieceType.QUEEN, Color.BLACK));
        
        // Возвращаем новое состояние доски
        return getBoard();
    }
    
    private Square parseSquare(String algebraic) {
        if (algebraic.length() != 2) {
            throw new IllegalArgumentException("Координата должна состоять из 2 символов");
        }
        
        char fileChar = algebraic.charAt(0);
        char rankChar = algebraic.charAt(1);
        
        if (fileChar < 'a' || fileChar > 'h') {
            throw new IllegalArgumentException("Файл должен быть от 'a' до 'h'");
        }
        
        if (rankChar < '1' || rankChar > '8') {
            throw new IllegalArgumentException("Ранг должен быть от '1' до '8'");
        }
        
        int file = fileChar - 'a';
        int rank = rankChar - '1';
        
        return new Square(file, rank);
    }
    
    private String squareToAlgebraic(Square square) {
        char file = (char) ('a' + square.file());
        int rank = square.rank() + 1;
        return String.valueOf(file) + rank;
    }
}
