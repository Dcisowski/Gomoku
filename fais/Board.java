import fais.zti.oramus.gomoku.Mark;
import fais.zti.oramus.gomoku.Move;
import fais.zti.oramus.gomoku.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class Board {
    private final int size;
    private Cell[][] grid;

    public Board(int size) {
        this.size = size;
        this.grid = new Cell[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                grid[i][j] = new Cell(i, j);
    }

    public int getSize() { return size; }

    public Cell getCell(int x, int y) {
        if (x < 0 || y < 0 || x >= size || y >= size) return null;
        return grid[x][y];
    }

    public void setCell(int x, int y, Mark mark) {
        grid[x][y].setSymbol(mark);
    }

    public List<Line> getAllLines() {
        List<Line> lines = new ArrayList<>();
        // poziome, pionowe, ukośne
        for (int i = 0; i < size; i++) {
            Line row = new Line();
            Line col = new Line();
            for (int j = 0; j < size; j++) {
                row.add(grid[i][j]);
                col.add(grid[j][i]);
            }
            lines.add(row);
            lines.add(col);
        }
        // diagonale
        for (int k = 0; k <= 2 * (size - 1); k++) {
            Line d1 = new Line();
            Line d2 = new Line();
            for (int i = 0; i < size; i++) {
                int j = k - i;
                if (j >= 0 && j < size) d1.add(grid[i][j]);
                int jj = size - 1 - k + i;
                if (jj >= 0 && jj < size) d2.add(grid[i][jj]);
            }
            if (d1.getCells().size() >= 5)
                lines.add(d1);
            if (d2.getCells().size() >= 5)
                lines.add(d2);
        }
        return lines;
    }

    public void initalizeBoard(Set<Move> moves) {
        for (Move move : moves) {
            Position pos = move.position();
            int x = pos.row();
            int y = pos.col();
            setCell(x, y, move.mark());
        }
    }
}