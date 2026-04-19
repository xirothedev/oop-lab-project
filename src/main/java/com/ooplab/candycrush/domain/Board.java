package com.ooplab.candycrush.domain;

import java.util.ArrayList;
import java.util.List;

public final class Board {
    private final int rows;
    private final int cols;
    private final Cell[][] cells;

    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.cells = new Cell[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                cells[row][col] = new Cell(new Position(row, col));
            }
        }
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public boolean isInside(Position position) {
        return position.row() >= 0 && position.row() < rows && position.col() >= 0 && position.col() < cols;
    }

    public Cell getCell(Position position) {
        return cells[position.row()][position.col()];
    }

    public Candy getCandy(Position position) {
        return getCell(position).candy();
    }

    public void setCandy(Position position, Candy candy) {
        getCell(position).setCandy(candy);
    }

    public void setJelly(Position position, boolean jelly) {
        getCell(position).setJelly(jelly);
    }

    public boolean hasJelly(Position position) {
        return getCell(position).jelly();
    }

    public void clearJelly(Position position) {
        getCell(position).setJelly(false);
    }

    public int jellyCount() {
        int count = 0;
        for (Position position : positions()) {
            if (hasJelly(position)) {
                count++;
            }
        }
        return count;
    }

    public void swap(Position first, Position second) {
        Candy firstCandy = getCandy(first);
        setCandy(first, getCandy(second));
        setCandy(second, firstCandy);
    }

    public List<Position> positions() {
        List<Position> positions = new ArrayList<>(rows * cols);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                positions.add(new Position(row, col));
            }
        }
        return positions;
    }

    public Board copy() {
        Board copy = new Board(rows, cols);
        for (Position position : positions()) {
            Candy candy = getCandy(position);
            copy.setCandy(position, candy == null ? null : candy.copy());
            copy.setJelly(position, hasJelly(position));
        }
        return copy;
    }
}

