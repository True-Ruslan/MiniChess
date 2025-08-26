package dev.ruslan.minichess;

import dev.ruslan.minichess.controller.ChessPageController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChessPageController.class)
class ChessPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void chessPage_ShouldReturnChessTemplate() throws Exception {
        mockMvc.perform(get("/chess"))
                .andExpect(status().isOk())
                .andExpect(view().name("chess"))
                .andExpect(content().contentType("text/html;charset=UTF-8"));
    }
}
