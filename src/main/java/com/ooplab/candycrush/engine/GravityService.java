package com.ooplab.candycrush.engine;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.Candy;
import com.ooplab.candycrush.domain.Position;

public final class GravityService {

    public void collapse(Board board) {
        for (int col = 0; col < board.cols(); col++) {
            int writeRow = board.rows() - 1;
            for (int row = board.rows() - 1; row >= 0; row--) {
                Position position = new Position(row, col);
                Candy candy = board.getCandy(position);
                if (candy != null) {
                    if (writeRow != row) {
                        board.setCandy(new Position(writeRow, col), candy);
                        board.setCandy(position, null);
                    }
                    writeRow--;
                }
            }
            while (writeRow >= 0) {
                board.setCandy(new Position(writeRow, col), null);
                writeRow--;
            }
        }
    }
}

