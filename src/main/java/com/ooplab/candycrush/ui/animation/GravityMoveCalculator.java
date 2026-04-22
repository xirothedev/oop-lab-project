package com.ooplab.candycrush.ui.animation;

import com.ooplab.candycrush.domain.Board;
import com.ooplab.candycrush.domain.Candy;
import com.ooplab.candycrush.domain.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GravityMoveCalculator {

    public Map<Position, Position> calculate(Board before, Board after) {
        Map<Position, Position> moves = new HashMap<>();

        for (int col = 0; col < after.cols(); col++) {
            List<CandyInfo> beforeCandies = new ArrayList<>();
            for (int row = 0; row < before.rows(); row++) {
                Candy candy = before.getCandy(new Position(row, col));
                if (candy != null) {
                    beforeCandies.add(new CandyInfo(candy.id(), row));
                }
            }

            List<CandyInfo> afterCandies = new ArrayList<>();
            for (int row = 0; row < after.rows(); row++) {
                Candy candy = after.getCandy(new Position(row, col));
                if (candy != null) {
                    afterCandies.add(new CandyInfo(candy.id(), row));
                }
            }

            int[] usedBefore = new int[beforeCandies.size()];
            for (CandyInfo afterInfo : afterCandies) {
                for (int i = 0; i < beforeCandies.size(); i++) {
                    if (usedBefore[i] == 0 && beforeCandies.get(i).id == afterInfo.id) {
                        usedBefore[i] = 1;
                        Position oldPos = new Position(beforeCandies.get(i).row, col);
                        Position newPos = new Position(afterInfo.row, col);
                        moves.put(oldPos, newPos);
                        break;
                    }
                }
            }
        }

        return moves;
    }

    private record CandyInfo(long id, int row) {
    }
}
