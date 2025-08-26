package dev.ruslan.minichess;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ruslan.minichess.controller.BoardApiController;
import dev.ruslan.minichess.core.Board;
import dev.ruslan.minichess.data.Color;
import dev.ruslan.minichess.data.Square;
import dev.ruslan.minichess.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoardApiController.class)
class BoardApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @Autowired
    private ObjectMapper objectMapper;

    private Board board;
    private Color sideToMove;

    @BeforeEach
    void setUp() {
        board = Board.initial();
        sideToMove = Color.WHITE;

        when(gameService.getBoard()).thenReturn(board);
        when(gameService.getSideToMove()).thenReturn(sideToMove);
        when(gameService.getMoves()).thenReturn(Arrays.asList("e2-e4", "e7-e5"));
    }

    @Test
    void testGetBoard() throws Exception {
        mockMvc.perform(get("/api/board"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sideToMove").value("WHITE"))
                .andExpect(jsonPath("$.cells").isArray())
                .andExpect(jsonPath("$.cells[0][0].type").value("ROOK"))
                .andExpect(jsonPath("$.cells[0][0].color").value("WHITE"))
                .andExpect(jsonPath("$.cells[1][0].type").value("PAWN"))
                .andExpect(jsonPath("$.cells[1][0].color").value("WHITE"));
    }

    @Test
    void testGetLegalMoves() throws Exception {
        Square fromSquare = new Square(4, 1); // e2
        List<Square> legalMoves = Arrays.asList(
                new Square(4, 2), // e3
                new Square(4, 3)  // e4
        );

        when(gameService.legalMovesFrom(fromSquare)).thenReturn(legalMoves);

        mockMvc.perform(get("/api/moves")
                        .param("from", "e2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("e2"))
                .andExpect(jsonPath("$.moves").isArray())
                .andExpect(jsonPath("$.moves[0]").value("e3"))
                .andExpect(jsonPath("$.moves[1]").value("e4"));
    }

    @Test
    void testGetLegalMovesInvalidFormat() throws Exception {
        mockMvc.perform(get("/api/moves")
                        .param("from", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Координата должна состоять из 2 символов"));
    }

    @Test
    void testMakeMove() throws Exception {
        Square from = new Square(4, 1); // e2
        Square to = new Square(4, 3);   // e4

        doNothing().when(gameService).makeMove(from, to);

        // Мокаем возврат нового состояния доски
        when(gameService.getBoard()).thenReturn(board);
        when(gameService.getSideToMove()).thenReturn(Color.BLACK);

        mockMvc.perform(post("/api/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"e2\",\"to\":\"e4\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sideToMove").value("BLACK"));

        verify(gameService).makeMove(from, to);
    }

    @Test
    void testMakeMoveMissingParameters() throws Exception {
        mockMvc.perform(post("/api/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"e2\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Отсутствуют параметры 'from' или 'to'"));
    }

    @Test
    void testMakeMoveInvalidCoordinates() throws Exception {
        doThrow(new IllegalArgumentException("Недопустимый ход"))
                .when(gameService).makeMove(any(), any());

        mockMvc.perform(post("/api/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":\"e2\",\"to\":\"e6\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Недопустимый ход"));
    }

    @Test
    void testGetMoveList() throws Exception {
        mockMvc.perform(get("/api/move-list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("e2-e4"))
                .andExpect(jsonPath("$[1]").value("e7-e5"));
    }

    @Test
    void testReset() throws Exception {
        doNothing().when(gameService).reset();

        mockMvc.perform(post("/api/reset"))
                .andExpect(status().isNoContent());

        verify(gameService).reset();
    }

    @Test
    void testParseSquareValid() throws Exception {
        // Тестируем через getLegalMoves
        Square fromSquare = new Square(0, 0); // a1
        when(gameService.legalMovesFrom(fromSquare)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/moves")
                        .param("from", "a1"))
                .andExpect(status().isOk());
    }

    @Test
    void testParseSquareInvalidFile() throws Exception {
        mockMvc.perform(get("/api/moves")
                        .param("from", "i1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Файл должен быть от 'a' до 'h'"));
    }

    @Test
    void testParseSquareInvalidRank() throws Exception {
        mockMvc.perform(get("/api/moves")
                        .param("from", "a9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ранг должен быть от '1' до '8'"));
    }

    @Test
    void testParseSquareTooShort() throws Exception {
        mockMvc.perform(get("/api/moves")
                        .param("from", "a"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Координата должна состоять из 2 символов"));
    }

    @Test
    void testParseSquareTooLong() throws Exception {
        mockMvc.perform(get("/api/moves")
                        .param("from", "a12"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Координата должна состоять из 2 символов"));
    }
}
