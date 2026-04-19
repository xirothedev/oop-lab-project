package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.Candy;
import com.ooplab.candycrush.domain.CandyColor;
import com.ooplab.candycrush.domain.MatchGroup;
import com.ooplab.candycrush.domain.MatchPattern;
import com.ooplab.candycrush.domain.NormalCandy;
import com.ooplab.candycrush.domain.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchDetectorTest {

    private final MatchDetector detector = new MatchDetector();

    @Test
    void detectsHorizontalLineOfFour() {
        Board board = board(
                "RRRR",
                "GBYO",
                "OPYG",
                "YBOP"
        );

        List<MatchGroup> matches = detector.findMatches(board);

        assertEquals(1, matches.size());
        assertEquals(MatchPattern.LINE_FOUR, matches.get(0).pattern());
        assertEquals(4, matches.get(0).positions().size());
    }

    @Test
    void detectsTShapeMatch() {
        Board board = board(
                "GBYOP",
                "OPRBG",
                "YRRRP",
                "GBROY",
                "POYGB"
        );

        List<MatchGroup> matches = detector.findMatches(board);

        assertEquals(1, matches.size());
        assertEquals(MatchPattern.T_OR_L, matches.get(0).pattern());
        assertTrue(matches.get(0).positions().contains(new Position(2, 2)));
    }

    private Board board(String... rows) {
        Board board = new Board(rows.length, rows[0].length());
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length(); col++) {
                board.setCandy(new Position(row, col), candy(rows[row].charAt(col)));
            }
        }
        return board;
    }

    private Candy candy(char value) {
        return new NormalCandy(CandyColor.fromSymbol(value));
    }
}
