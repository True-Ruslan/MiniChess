package dev.ruslan.minichess.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChessPageController {

    @GetMapping("/chess")
    public String chessPage() {
        return "chess";
    }
}
